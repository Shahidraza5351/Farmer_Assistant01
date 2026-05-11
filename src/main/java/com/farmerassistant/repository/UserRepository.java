package com.farmerassistant.repository;

import com.farmerassistant.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for User entity CRUD operations.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find an active user by email address.
     */
    Optional<User> findByEmailAndIsActiveTrue(String email);

    /**
     * Find any user by email (including inactive).
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if an email already exists in the system.
     */
    boolean existsByEmail(String email);
}
