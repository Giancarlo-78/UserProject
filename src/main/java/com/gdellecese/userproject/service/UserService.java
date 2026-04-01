package com.gdellecese.userproject.service;

import com.gdellecese.userproject.dto.UserRequestDto;
import com.gdellecese.userproject.dto.UserResponseDto;
import com.gdellecese.userproject.mapper.UserMapper;
import com.gdellecese.userproject.model.User;
import com.gdellecese.userproject.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // CREATE
    public UserResponseDto createUser(UserRequestDto dto) {
        User user = UserMapper.toEntity(dto);
        User saved = userRepository.save(user);
        return UserMapper.toDto(saved);
    }

    // READ ALL

    /**
     **
     * Retrieves all users, with optional filtering by first name and/or last name.
     *
     * <p>This implementation deliberately adopts the simplest approach that satisfies
     * the current requirements (two optional filter attributes), in line with the
     * TDD principle of writing the minimum code needed to make the tests pass.
     * {@code QueryDSL} predicate-based approach
     * @param firstName
     * @param lastName
     * @return a {@link List} of {@link UserResponseDto} matching the provided filters, or all users if no filters are provided.
     */
    public List<UserResponseDto> getAllUsers(String firstName, String lastName) {
        List<User> users;

        if (firstName != null && lastName != null) {
            users = userRepository
                    .findByFirstNameContainingIgnoreCaseAndLastNameContainingIgnoreCase(firstName, lastName);
        } else if (firstName != null) {
            users = userRepository.findByFirstNameContainingIgnoreCase(firstName);
        } else if (lastName != null) {
            users = userRepository.findByLastNameContainingIgnoreCase(lastName);
        } else {
            users = userRepository.findAll();
        }

        return users.stream()
                .map(UserMapper::toDto)
                .toList();
    }

    // READ ONE
    public UserResponseDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        return UserMapper.toDto(user);
    }

    // UPDATE
    public UserResponseDto updateUser(Long id, UserRequestDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));

        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());
        user.setAddress(dto.getAddress());

        User updated = userRepository.save(user);
        return UserMapper.toDto(updated);
    }

    // DELETE
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    // CSV IMPORT
    public void processFile(MultipartFile file) {
        try (
                Reader reader = new BufferedReader(
                        new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))
        ) {
            CSVReader csvReader = new CSVReaderBuilder(reader)
                    .withSkipLines(1) // Skip the header row
                    .build();

            List<String[]> rows = csvReader.readAll();
            List<User> users = new ArrayList<>(rows.size());

            for (String[] row : rows) {
                if (row.length < 4) continue; // Skip malformed rows

                User user = User.builder()
                        .firstName(row[0].trim())
                        .lastName(row[1].trim())
                        .email(row[2].trim())
                        .address(row[3].trim())
                        .build();

                users.add(user);
            }

            userRepository.saveAll(users);

        } catch (Exception e) {
            throw new RuntimeException("Failed to process CSV file: " + e.getMessage());
        }
    }
}