package com.InfoLink.dto;

import java.time.LocalDateTime;
import com.InfoLink.model.Role;

public class UsersResponse {

    private String userID;
    private String username;
    private String fullName;
    private Long groupID;
    private Role role;
    private Boolean isActive;
    private LocalDateTime createdDate;

    public UsersResponse() {
    }

    public UsersResponse(String userID, String username, String fullName,
                         Long groupID, Role role,
                         Boolean isActive, LocalDateTime createdDate) {

        this.userID = userID;
        this.username = username;
        this.fullName = fullName;
        this.groupID = groupID;
        this.role = role;
        this.isActive = isActive;
        this.createdDate = createdDate;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
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

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
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
