package com.InfoLink.repository;

import org.springframework.data.jpa.repository.JpaRepository;


import com.InfoLink.model.User;
import java.util.Optional;


public interface UserRepository extends  JpaRepository<User, Integer> {
    public boolean existsByUsername(String username);
    public Optional<User> findByUsername(String username);
}
