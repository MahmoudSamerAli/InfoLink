package com.InfoLink.dto;

import java.util.Map;

import jakarta.validation.constraints.NotEmpty;

public class DeepSearchRequest {

    @NotEmpty(message = "At least one search key is required")
    private Map<String, String> searchKeys;

    public Map<String, String> getSearchKeys() {
        return searchKeys;
    }

    public void setSearchKeys(Map<String, String> searchKeys) {
        this.searchKeys = searchKeys;
    }
}