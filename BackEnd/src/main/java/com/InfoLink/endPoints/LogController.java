package com.InfoLink.endPoints;

import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.context.SecurityContextHolder;
import com.InfoLink.security.CustomUserDetails;
import com.InfoLink.utils.service.LogsService;
import com.InfoLink.dto.LogResponse;
import com.InfoLink.dto.PagedResponse;
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

    @GetMapping("/me")
    public PagedResponse<LogResponse> getCurrentUserLogs(Pageable pageable) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        return logService.getCurrentUserLogs(userDetails.getUsername(), pageable);
    }

    @GetMapping("/me/today/count")
    public long getCurrentUserTodayCount() {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        return logService.getCurrentUserTodayCount(userDetails.getUsername());
    }

    @GetMapping("/me/success/count")
    public long getCurrentUserSuccessCount() {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        return logService.getCurrentUserSuccessCount(userDetails.getUsername());
    }

    @GetMapping("/today/count")
    public long getTodayCount() {
        return logService.getTodayCount();
    }
}
