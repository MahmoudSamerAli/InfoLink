package com.InfoLink.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "Groups")
public class Groups {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Group_ID", nullable = false, unique = true)
    private Long groupID;

    @Column(name = "Group_Name", nullable = false, length = 100)
    private String groupName;

    @Column(name = "Descriptionn", length = 255)
    private String groupDescription;

    @Column(name = "Is_Active", nullable = false)
    private Boolean isActive;

    public Groups() {}

    public Groups(Long groupID, String groupName, String groupDescription, Boolean isActive) {
        this.groupID = groupID;
        this.groupName = groupName;
        this.groupDescription = groupDescription;
        this.isActive = isActive;
    }

    public Long getGroupID() { return groupID; }
    public void setGroupID(Long groupID) { this.groupID = groupID; }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public String getGroupDescription() { return groupDescription; }
    public void setGroupDescription(String groupDescription) { this.groupDescription = groupDescription; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}