package org.example.repository;

import org.example.entities.UserInfo;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRepository extends CrudRepository<UserInfo, String> {
    Optional<UserInfo> findByUsername(String username);
    Optional<UserInfo> findByEmail(String email);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
