package com.InfoLink.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "Users")
public class User {

    @Id
    @Column(name = "UserID", nullable = false, unique = true, length = 36)
    private String userID;

    @Column(name = "Username", nullable = false, length = 50, unique = true)
    private String username;

    @Column(name = "Password", nullable = false, length = 255)
    private String password;

    @Column(name = "FullName", length = 100)
    private String fullName;

    @ManyToOne(fetch = FetchType.LAZY) // lazy load group to save memory
    @JoinColumn(name = "GroupID", referencedColumnName = "GroupID")
    private Groups group;

    @Enumerated(EnumType.STRING)
    @Column(name = "Role", nullable = false, length = 20)
    private Role role;

    @Column(name = "IsActive", nullable = false)
    private Boolean isActive;

    @Column(name = "CreatedDate", nullable = false)
    private LocalDateTime createdDate;
    public User() {}

    public User(String userID, String username, String password, String fullName,
                Groups group, Role role, Boolean isActive, LocalDateTime createdDate) {
        this.userID = userID;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.group = group;
        this.role = role;
        this.isActive = isActive;
        this.createdDate = createdDate;
    }
    public String getUserID() { return userID; }
    public void setUserID(String userID) { this.userID = userID; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public Groups getGroup() { return group; }
    public void setGroup(Groups group) { this.group = group; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

}