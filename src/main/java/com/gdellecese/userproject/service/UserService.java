package com.gdellecese.userproject.service;

import com.gdellecese.userproject.dto.UserRequestDto;
import com.gdellecese.userproject.dto.UserResponseDto;
import com.gdellecese.userproject.model.User;
import com.gdellecese.userproject.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // CREATE
    public UserResponseDto createUser(UserRequestDto dto) {
        User user = toEntity(dto);
        User saved = userRepository.save(user);
        return toDto(saved);
    }

    // READ ALL (con filtri opzionali)

    /**
     * questa la soluzione piu semplice per sopportarela ricerca per solo due attribiti opzionalei
     * fare una ricerca su spring data
     * @param firstName
     * @param lastName
     * @param age
     * @return
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
                .map(this::toDto)
                .toList();
    }

    // READ ONE
    public UserResponseDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        return toDto(user);
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
        return toDto(updated);
    }

    // DELETE
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    // --- Metodi di conversione privati ---

    private User toEntity(UserRequestDto dto) {
        return User.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .address(dto.getAddress())
                .build();
    }

    private UserResponseDto toDto(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .address(user.getAddress())
                .build();
    }
}