package com.InfoLink.dto;

public class CollectionGroupResponse {
    private final Long groupID;
    private final String groupName;

    public CollectionGroupResponse(Long groupID, String groupName) {
        this.groupID = groupID;
        this.groupName = groupName;
    }

    public Long getGroupID() {
        return groupID;
    }

    public String getGroupName() {
        return groupName;
    }
}
