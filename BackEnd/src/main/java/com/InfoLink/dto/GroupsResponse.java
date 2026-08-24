package com.InfoLink.dto;

public class GroupsResponse {
    private Long groupID;
    private String groupName;
    private String description;
    private Boolean isActive;

    public GroupsResponse(Long groupID, String groupName, String description, Boolean isActive) {
        this.groupID = groupID;
        this.groupName = groupName;
        this.description = description;
        this.isActive = isActive;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    
}
