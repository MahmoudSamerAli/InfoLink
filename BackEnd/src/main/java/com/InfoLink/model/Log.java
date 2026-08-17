package com.InfoLink.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "Logs")
public class Log {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LogID", nullable = false, unique = true)
    private Long logID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", referencedColumnName = "UserID")
    private User user;

    @Column(name = "collection_name", nullable = false)
    private String collection_name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", referencedColumnName = "GroupID")
    private Groups group;

    @Column(name = "SearchKeyword", length = 255)
    private String searchKeyword;

    @Column(name = "SearchDate", nullable = false)
    private LocalDateTime searchDate;

    @Column(name = "IpAddress", length = 45)
    private String ipAddress;

    @Column(name = "Status", nullable = false)
    private Boolean status;


    public Log(Long logID, User user, String collection_name,Groups group, String searchKeyword, LocalDateTime searchDate,
            String ipAddress, Boolean status) {
        this.logID = logID;
        this.user = user;
        this.collection_name = collection_name;
        this.group = group;
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

    public Groups getGroups(){
        return this.group;
    }

    public void setGroups(Groups group){
        this.group = group;
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