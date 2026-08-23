package com.InfoLink.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.InfoLink.dto.LogResponse;
import com.InfoLink.dto.PagedResponse;
import com.InfoLink.model.Log;
import com.InfoLink.repository.LogRepository;
import com.InfoLink.repository.LogsRepository;

@Service
public class LogsService {

    private final LogsRepository logRepository;

    public LogsService(LogsRepository logRepository) {
        this.logRepository = logRepository;
    }

    public PagedResponse<LogResponse> getLogs(Pageable pageable) {
        Page<Log> logPage = logRepository.findAll(pageable);

        List<LogResponse> content = logPage.getContent()
                .stream()
                .map(log -> new LogResponse(
                        log.getLogID(),
                        log.getUser().getUsername(),
                        log.getCollection(),
                        log.getSearchKeyword(),
                        log.getSearchDate(),
                        log.getIpAddress(),
                        log.getStatus()
                ))
                .collect(Collectors.toList());

        return new PagedResponse<>(content,
                logPage.getNumber(),
                logPage.getSize(),
                logPage.getTotalElements());
    }

    public PagedResponse<LogResponse> searchLogs(String keyword, Pageable pageable) {
    Page<Log> logPage = logRepository.searchLogs(keyword, pageable);

    List<LogResponse> content = logPage.getContent()
            .stream()
            .map(log -> new LogResponse(
                    log.getLogID(),
                    log.getUser().getUsername(),
                    log.getCollection(),
                    log.getSearchKeyword(),
                    log.getSearchDate(),
                    log.getIpAddress(),
                    log.getStatus()
            ))
            .collect(Collectors.toList());

    return new PagedResponse<>(content,
            logPage.getNumber(),
            logPage.getSize(),
            logPage.getTotalElements());
    }

}

