package com.InfoLink.dto;

import java.time.LocalDateTime;

public class ProfileResponse {
    private int userID;
    private String username;
    private String fullName;
    private Long groupID;
    private String groupName;
    private String role;
    private Boolean isActive;
    private LocalDateTime createdDate;

    public ProfileResponse(int userID, String username, String fullName,
                           Long groupID, String groupName, String role,
                           Boolean isActive, LocalDateTime createdDate) {
        this.userID = userID;
        this.username = username;
        this.fullName = fullName;
        this.groupID = groupID;
        this.groupName = groupName;
        this.role = role;
        this.isActive = isActive;
        this.createdDate = createdDate;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Long getGroupID() {
        return groupID;
    }

    public void setGroupID(Long groupID) {
        this.groupID = groupID;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    
}
