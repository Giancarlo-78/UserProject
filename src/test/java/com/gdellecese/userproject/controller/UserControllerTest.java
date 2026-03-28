package com.gdellecese.userproject.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdellecese.userproject.dto.UserRequestDto;
import com.gdellecese.userproject.dto.UserResponseDto;
import com.gdellecese.userproject.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    private UserRequestDto requestDto;
    private UserResponseDto responseDto;

    @BeforeEach
    void setUp() {
        requestDto = UserRequestDto.builder()
                .firstName("Mario")
                .lastName("Rossi")
                .email("mario.rossi@gmail.com")
                .address("Roma")
                .build();

        responseDto = UserResponseDto.builder()
                .id(1L)
                .firstName("Mario")
                .lastName("Rossi")
                .email("mario.rossi@gmail.com")
                .address("Roma")
                .build();
    }

    // ---- POST ----

    @Test
    void createUser_shouldReturn201WithBody() throws Exception {
        // Arrange
        when(userService.createUser(any(UserRequestDto.class))).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.firstName").value("Mario"))
                .andExpect(jsonPath("$.email").value("mario.rossi@gmail.com"));
    }

    // ---- GET ALL ----

    @Test
    void getAllUsers_noFilters_shouldReturn200WithList() throws Exception {
        // Arrange
        when(userService.getAllUsers(null, null)).thenReturn(List.of(responseDto));

        // Act & Assert
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].firstName").value("Mario"));
    }

    @Test
    void getAllUsers_withFirstName_shouldReturn200() throws Exception {
        // Arrange
        when(userService.getAllUsers("Mario", null)).thenReturn(List.of(responseDto));

        // Act & Assert
        mockMvc.perform(get("/api/users")
                        .param("firstName", "Mario"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    // ---- GET BY ID ----

    @Test
    void getUserById_whenExists_shouldReturn200() throws Exception {
        // Arrange
        when(userService.getUserById(1L)).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.firstName").value("Mario"));
    }

    @Test
    void getUserById_whenNotExists_shouldReturn404() throws Exception {
        // Arrange
        when(userService.getUserById(99L))
                .thenThrow(new EntityNotFoundException("User not found with id: 99"));

        // Act & Assert
        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("User not found with id: 99"));
    }

    // ---- PUT ----

    @Test
    void updateUser_whenExists_shouldReturn200() throws Exception {
        // Arrange
        when(userService.updateUser(eq(1L), any(UserRequestDto.class))).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Mario"));
    }

    @Test
    void updateUser_whenNotExists_shouldReturn404() throws Exception {
        // Arrange
        when(userService.updateUser(eq(99L), any(UserRequestDto.class)))
                .thenThrow(new EntityNotFoundException("User not found with id: 99"));

        // Act & Assert
        mockMvc.perform(put("/api/users/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // ---- DELETE ----

    @Test
    void deleteUser_whenExists_shouldReturn204() throws Exception {
        // Arrange — niente, il service non lancia eccezioni

        // Act & Assert
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUser_whenNotExists_shouldReturn404() throws Exception {
        // Arrange
        doThrow(new EntityNotFoundException("User not found with id: 99"))
                .when(userService).deleteUser(99L);

        // Act & Assert
        mockMvc.perform(delete("/api/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // ---- CSV UPLOAD ----

    @Test
    void uploadCsv_withValidFile_shouldReturn200() throws Exception {
        // Arrange
        String csvContent = "firstName,lastName,email,address\n" +
                "Mario,Rossi,mario.rossi@gmail.com,Roma";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8)
        );

        // Act & Assert
        mockMvc.perform(multipart("/api/users/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(content().string("File accepted. Processing completed."));
    }

    @Test
    void uploadCsv_withEmptyFile_shouldReturn400() throws Exception {
        // Arrange
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.csv",
                "text/csv",
                new byte[0]  // 0 byte — file vuoto
        );

        // Act & Assert
        mockMvc.perform(multipart("/api/users/upload").file(emptyFile))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("File is empty"));
    }

    @Test
    void uploadCsv_withNonCsvFile_shouldReturn400() throws Exception {
        // Arrange
        MockMultipartFile txtFile = new MockMultipartFile(
                "file",
                "test.txt",  // estensione sbagliata
                "text/plain",
                "some content".getBytes()
        );

        // Act & Assert
        mockMvc.perform(multipart("/api/users/upload").file(txtFile))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Only CSV files are allowed"));
    }
}