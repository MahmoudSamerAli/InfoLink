package com.InfoLink.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.InfoLink.model.Log;

public interface LogsRepository extends JpaRepository<Log, Long> {

    @Query("SELECT l FROM Log l " +
           "WHERE LOWER(l.searchKeyword) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "   OR LOWER(l.user.username) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "   OR LOWER(l.collection) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "   OR LOWER(l.ipAddress) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Log> searchLogs(@Param("keyword") String keyword, Pageable pageable);
}
