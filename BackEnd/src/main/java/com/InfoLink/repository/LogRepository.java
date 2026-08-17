package com.InfoLink.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.InfoLink.model.Log;
public interface LogRepository extends JpaRepository<Log, Long> {}
