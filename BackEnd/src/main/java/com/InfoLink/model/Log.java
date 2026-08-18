package com.InfoLink.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "Logs")
public class Log {

    @Id
    @Column(name = "Log_ID", nullable = false, unique = true)
    private Long logID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "User_Idd", referencedColumnName = "UserID")
    private User user;

    @Column(name = "Collection_Name", nullable = false)
    private String collection_name;

    @Column(name = "Search_keyword", length = 255)
    private String searchKeyword;

    @Column(name = "Search_date", nullable = false)
    private LocalDateTime searchDate;

    @Column(name = "IP_address", length = 45)
    private String ipAddress;

    @Column(name = "Statuss", nullable = false)
    private Boolean status;


    public Log(Long logID, User user, String collection_name, String searchKeyword, LocalDateTime searchDate,
            String ipAddress, Boolean status) {
        this.logID = logID;
        this.user = user;
        this.collection_name = collection_name;
        this.searchKeyword = searchKeyword;
        this.searchDate = searchDate;
        this.ipAddress = ipAddress;
        this.status = status;
    }

    public Log() {
    }

    public Long getLogID() {
        return logID;
    }

    public void setLogID(Long logID) {
        this.logID = logID;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getCollection() {
        return collection_name;
    }

    public void setCollection(String collection_name) {
        this.collection_name = collection_name;
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