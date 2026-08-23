package com.InfoLink.dto;

import java.time.LocalDateTime;

public class LogResponse {
    private Long id;
    private String username;
    private String collection;
    private String searchKeyword;
    private LocalDateTime searchDate;
    private String ipAddress;
    private Boolean status;

    public LogResponse(Long id, String username, String collection,
                       String searchKeyword, LocalDateTime searchDate,
                       String ipAddress, Boolean status) {
        this.id = id;
        this.username = username;
        this.collection = collection;
        this.searchKeyword = searchKeyword;
        this.searchDate = searchDate;
        this.ipAddress = ipAddress;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    public String getSearchKeyword() {
        return searchKeyword;
    }

    public void setSearchKeyword(String searchKeyword) {
        this.searchKeyword = searchKeyword;
    }

    public LocalDateTime getSearchDate() {
        return searchDate;
    }

    public void setSearchDate(LocalDateTime searchDate) {
        this.searchDate = searchDate;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    
}

