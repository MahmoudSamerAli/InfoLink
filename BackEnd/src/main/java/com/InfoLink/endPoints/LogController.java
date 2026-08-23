package com.InfoLink.endPoints;

import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.InfoLink.dto.LogResponse;
import com.InfoLink.dto.PagedResponse;
import com.InfoLink.service.LogsService;
@RestController
@RequestMapping("/logs")
public class LogController {
    private final LogsService logService;
    public LogController(LogsService logService) {
        this.logService = logService;
    }
    @GetMapping
    public PagedResponse<LogResponse> getLogs(Pageable pageable) {
        return logService.getLogs(pageable);
    }
    @GetMapping("/search")
    public PagedResponse<LogResponse> searchLogs(@RequestParam String keyword, Pageable pageable) {
        return logService.searchLogs(keyword, pageable);
    }   
}
