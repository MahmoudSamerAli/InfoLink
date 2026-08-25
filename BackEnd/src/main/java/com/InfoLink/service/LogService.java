package com.InfoLink.service;

import org.springframework.stereotype.Service;
import com.InfoLink.model.Log;
import com.InfoLink.repository.LogRepository;

@Service
public class LogService {

    private final LogRepository repo;

    public LogService(LogRepository repo) {
        this.repo = repo;
    }

    public Log saveLog(Log log) {
        return repo.save(log);
    }
}
