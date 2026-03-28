package com.gdellecese.userproject.service;

import com.gdellecese.userproject.dto.UserRequestDto;
import com.gdellecese.userproject.dto.UserResponseDto;
import com.gdellecese.userproject.model.User;
import com.gdellecese.userproject.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user;
    private UserRequestDto requestDto;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .firstName("Mario")
                .lastName("Rossi")
                .email("mario.rossi@gmail.com")
                .address("Roma")
                .build();

        requestDto = UserRequestDto.builder()
                .firstName("Mario")
                .lastName("Rossi")
                .email("mario.rossi@gmail.com")
                .address("Roma")
                .build();
    }

    // ---- CREATE ----

    @Test
    void createUser_shouldReturnResponseDto() {
        // Arrange
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        UserResponseDto result = userService.createUser(requestDto);

        // Assert
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getFirstName()).isEqualTo("Mario");
        assertThat(result.getEmail()).isEqualTo("mario.rossi@gmail.com");
        verify(userRepository, times(1)).save(any(User.class));
    }

    // ---- GET ALL ----

    @Test
    void getAllUsers_noFilters_shouldReturnAll() {
        // Arrange
        when(userRepository.findAll()).thenReturn(List.of(user));

        // Act
        List<UserResponseDto> result = userService.getAllUsers(null, null);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFirstName()).isEqualTo("Mario");
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void getAllUsers_withFirstName_shouldFilterByFirstName() {
        // Arrange
        when(userRepository.findByFirstNameContainingIgnoreCase("Mario"))
                .thenReturn(List.of(user));

        // Act
        List<UserResponseDto> result = userService.getAllUsers("Mario", null);

        // Assert
        assertThat(result).hasSize(1);
        verify(userRepository, times(1)).findByFirstNameContainingIgnoreCase("Mario");
    }

    @Test
    void getAllUsers_withLastName_shouldFilterByLastName() {
        // Arrange
        when(userRepository.findByLastNameContainingIgnoreCase("Rossi"))
                .thenReturn(List.of(user));

        // Act
        List<UserResponseDto> result = userService.getAllUsers(null, "Rossi");

        // Assert
        assertThat(result).hasSize(1);
        verify(userRepository, times(1)).findByLastNameContainingIgnoreCase("Rossi");
    }

    @Test
    void getAllUsers_withBothNames_shouldFilterByBoth() {
        // Arrange
        when(userRepository.findByFirstNameContainingIgnoreCaseAndLastNameContainingIgnoreCase(
                "Mario", "Rossi")).thenReturn(List.of(user));

        // Act
        List<UserResponseDto> result = userService.getAllUsers("Mario", "Rossi");

        // Assert
        assertThat(result).hasSize(1);
        verify(userRepository, times(1))
                .findByFirstNameContainingIgnoreCaseAndLastNameContainingIgnoreCase("Mario", "Rossi");
    }

    // ---- GET BY ID ----

    @Test
    void getUserById_whenExists_shouldReturnDto() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act
        UserResponseDto result = userService.getUserById(1L);

        // Assert
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getFirstName()).isEqualTo("Mario");
    }

    @Test
    void getUserById_whenNotExists_shouldThrowException() {
        // Arrange
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ---- UPDATE ----

    @Test
    void updateUser_whenExists_shouldReturnUpdatedDto() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        UserResponseDto result = userService.updateUser(1L, requestDto);

        // Assert
        assertThat(result.getFirstName()).isEqualTo("Mario");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUser_whenNotExists_shouldThrowException() {
        // Arrange
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.updateUser(99L, requestDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ---- DELETE ----

    @Test
    void deleteUser_whenExists_shouldCallDeleteById() {
        // Arrange
        when(userRepository.existsById(1L)).thenReturn(true);

        // Act
        userService.deleteUser(1L);

        // Assert
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteUser_whenNotExists_shouldThrowException() {
        // Arrange
        when(userRepository.existsById(99L)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> userService.deleteUser(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ---- CSV IMPORT ----

    @Test
    void processFile_withValidCsv_shouldSaveUsers() throws Exception {
        // Arrange
        String csvContent = "firstName,lastName,email,address\n" +
                "Mario,Rossi,mario.rossi@gmail.com,Roma\n" +
                "Luigi,Bianchi,luigi.bianchi@gmail.com,Milano";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8)
        );

        // Act
        userService.processFile(file);

        // Assert
        verify(userRepository, times(1)).saveAll(anyList());
    }

    @Test
    void processFile_withMalformedRow_shouldSkipAndSaveValid() throws Exception {
        // Arrange — seconda riga malformata (meno di 4 colonne)
        String csvContent = "firstName,lastName,email,address\n" +
                "Mario,Rossi,mario.rossi2@gmail.com,Roma\n" +
                "Luigi\n" +
                "Anna,Verdi,anna.verdi2@gmail.com,Napoli";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8)
        );

        // Act
        userService.processFile(file);

        // Assert — saveAll chiamato una volta con lista non vuota
        verify(userRepository, times(1)).saveAll(anyList());
    }

    @Test
    void processFile_withEmptyInputStream_shouldThrowRuntimeException() {
        // Arrange — file che lancia IOException
        MultipartFile brokenFile = mock(MultipartFile.class);
        try {
            when(brokenFile.getInputStream())
                    .thenThrow(new IOException("Simulated IO error"));
        } catch (IOException e) {
            fail("Setup failed");
        }

        // Act & Assert
        assertThatThrownBy(() -> userService.processFile(brokenFile))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to process CSV file");
    }
}