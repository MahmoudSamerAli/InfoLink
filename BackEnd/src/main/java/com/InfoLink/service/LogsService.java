package com.InfoLink.service;

import java.util.List;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional(readOnly = true)
    public PagedResponse<LogResponse> getLogs(Pageable pageable) {
        Page<Log> logPage = logRepository.findAllByOrderBySearchDateDescLogIDDesc(pageable);

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

    @Transactional(readOnly = true)
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

        @Transactional(readOnly = true)
        public PagedResponse<LogResponse> getCurrentUserLogs(String username, Pageable pageable) {
                Page<Log> logPage = logRepository.findByUser_UsernameOrderBySearchDateDescLogIDDesc(username, pageable);
                return toResponse(logPage);
        }

        public long getTodayCount() {
                LocalDate today = LocalDate.now();
                return logRepository.countBySearchDateBetween(today.atStartOfDay(), today.plusDays(1).atStartOfDay());
        }

        public long getCurrentUserTodayCount(String username) {
                LocalDate today = LocalDate.now();
                return logRepository.countByUser_UsernameAndSearchDateBetween(
                                username, today.atStartOfDay(), today.plusDays(1).atStartOfDay());
        }

        public long getCurrentUserSuccessCount(String username) {
                return logRepository.countByUser_UsernameAndStatusTrue(username);
        }

        private PagedResponse<LogResponse> toResponse(Page<Log> logPage) {
                List<LogResponse> content = logPage.getContent().stream()
                                .map(log -> new LogResponse(
                                                log.getLogID(), log.getUser().getUsername(), log.getCollection(),
                                                log.getSearchKeyword(), log.getSearchDate(), log.getIpAddress(), log.getStatus()))
                                .collect(Collectors.toList());
                return new PagedResponse<>(content, logPage.getNumber(), logPage.getSize(), logPage.getTotalElements());
        }

}