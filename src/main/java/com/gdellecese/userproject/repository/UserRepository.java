package com.gdellecese.userproject.repository;

import com.gdellecese.userproject.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Search by firstName
    List<User> findByFirstNameContainingIgnoreCase(String firstName);

    // Search by lastName
    List<User> findByLastNameContainingIgnoreCase(String lastName);

    // Search by firstName and lastName
    List<User> findByFirstNameContainingIgnoreCaseAndLastNameContainingIgnoreCase(
            String firstName, String lastName
    );
}
