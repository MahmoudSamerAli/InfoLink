package com.InfoLink.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

public class AddGroupRequest {
    @NotBlank
    @Pattern(regexp = "^[a-zA-Z_]+$")
    @Size(min = 2, max = 30)
    private String groupName;

    @NotBlank
    @Size(min = 2, max = 100)
    @Pattern(regexp = "^[a-zA-Z\\s]+$")
    private String groupDescription;

    @NotNull
    private Boolean isActive;
    private List<String> collections;

    

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupDescription() {
        return groupDescription;
    }

    public void setGroupDescription(String groupDescription) {
        this.groupDescription = groupDescription;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public List<String> getCollections() { return collections; }
    public void setCollections(List<String> collections) { this.collections = collections; }

        
}
