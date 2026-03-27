package com.gdellecese.userproject.repository;

import com.gdellecese.userproject.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        userRepository.save(User.builder()
                .firstName("Mario")
                .lastName("Rossi")
                .email("mario.rossi@gmail.com")
                .address("Roma")
                .build());

        userRepository.save(User.builder()
                .firstName("Luigi")
                .lastName("Rossi")
                .email("luigi.rossi@gmail.com")
                .address("Milano")
                .build());

        userRepository.save(User.builder()
                .firstName("Anna")
                .lastName("Verdi")
                .email("anna.verdi@gmail.com")
                .address("Napoli")
                .build());
    }

    // ---- CRUD base ----

    @Test
    void save_shouldPersistUser() {
        User user = User.builder()
                .firstName("Carlo")
                .lastName("Bianchi")
                .email("carlo.bianchi@gmail.com")
                .build();

        User saved = userRepository.save(user);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getFirstName()).isEqualTo("Carlo");
    }

    @Test
    void findById_whenExists_shouldReturnUser() {
        User mario = userRepository.findAll().stream()
                .filter(u -> u.getEmail().equals("mario.rossi@gmail.com"))
                .findFirst().orElseThrow();

        Optional<User> result = userRepository.findById(mario.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getFirstName()).isEqualTo("Mario");
    }

    @Test
    void delete_shouldRemoveUser() {
        User mario = userRepository.findAll().stream()
                .filter(u -> u.getEmail().equals("mario.rossi@gmail.com"))
                .findFirst().orElseThrow();

        userRepository.deleteById(mario.getId());

        assertThat(userRepository.findById(mario.getId())).isEmpty();
    }

    // ---- Query custom ----

    @Test
    void findByFirstNameContainingIgnoreCase_shouldReturnMatches() {
        List<User> result = userRepository.findByFirstNameContainingIgnoreCase("mario");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFirstName()).isEqualTo("Mario");
    }

    @Test
    void findByFirstNameContainingIgnoreCase_partialMatch_shouldWork() {
        List<User> result = userRepository.findByFirstNameContainingIgnoreCase("ar");

        // "Mario" contiene "ar"
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFirstName()).isEqualTo("Mario");
    }

    @Test
    void findByLastNameContainingIgnoreCase_shouldReturnMatches() {
        List<User> result = userRepository.findByLastNameContainingIgnoreCase("rossi");

        // Mario Rossi e Luigi Rossi
        assertThat(result).hasSize(2);
    }

    @Test
    void findByFirstNameAndLastName_shouldReturnExactMatch() {
        List<User> result = userRepository
                .findByFirstNameContainingIgnoreCaseAndLastNameContainingIgnoreCase("Mario", "Rossi");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("mario.rossi@gmail.com");
    }

    @Test
    void findByFirstNameAndLastName_noMatch_shouldReturnEmpty() {
        List<User> result = userRepository
                .findByFirstNameContainingIgnoreCaseAndLastNameContainingIgnoreCase("Mario", "Verdi");

        assertThat(result).isEmpty();
    }
}