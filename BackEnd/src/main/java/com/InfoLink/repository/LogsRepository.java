package com.InfoLink.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.InfoLink.model.Log;
import java.time.LocalDateTime;

public interface LogsRepository extends JpaRepository<Log, Long> {

    Page<Log> findByUser_Username(String username, Pageable pageable);

    long countBySearchDateBetween(LocalDateTime start, LocalDateTime end);

    long countByUser_UsernameAndSearchDateBetween(String username, LocalDateTime start, LocalDateTime end);

    long countByUser_UsernameAndStatusTrue(String username);

    @Query("SELECT l FROM Log l " +
           "WHERE LOWER(l.searchKeyword) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "   OR LOWER(l.user.username) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "   OR LOWER(l.collection_name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "   OR LOWER(l.ipAddress) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Log> searchLogs(@Param("keyword") String keyword, Pageable pageable);
}
