package com.InfoLink.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import com.InfoLink.model.User;
import com.InfoLink.model.Role;
import java.util.Optional;


public interface UserRepository extends  JpaRepository<User, Integer> {
    public boolean existsByUsername(String username);
    public Optional<User> findByUsername(String username);
        @Query("SELECT u FROM User u JOIN FETCH u.group WHERE u.username = :username")
        Optional<User> findByUsernameWithGroup(String username);
        @Query("SELECT u FROM User u WHERE " +
            "(:keyword IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:role IS NULL OR u.role = :role) " +
            "AND (:groupId IS NULL OR u.group.groupID = :groupId) " +
            "AND (:active IS NULL OR u.isActive = :active)")
        Page<User> search(@Param("keyword") String keyword, @Param("role") Role role,
                  @Param("groupId") Long groupId, @Param("active") Boolean active,
                  Pageable pageable);
}
