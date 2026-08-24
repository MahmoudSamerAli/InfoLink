package com.InfoLink.dto;

import org.bson.Document;

public class DeepSearchResult {

    private final String collectionName;
    private final Document document;

    public DeepSearchResult(String collectionName, Document document) {
        this.collectionName = collectionName;
        this.document = document;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public Document getDocument() {
        return document;
    }
}