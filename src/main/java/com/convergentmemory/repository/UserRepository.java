package com.convergentmemory.repository;

import com.convergentmemory.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByApiToken(String apiToken);
    boolean existsByUsername(String username);
}
