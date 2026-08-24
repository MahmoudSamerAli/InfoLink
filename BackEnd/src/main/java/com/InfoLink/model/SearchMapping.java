package com.InfoLink.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "Search_Mapping")
public class SearchMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Mapping_ID", nullable = false)
    private Long mappingId;

    @Column(name = "Collection_Name", nullable = false)
    private String collectionName;

    @Column(name = "Field_Name", nullable = false)
    private String fieldName;

    @Column(name = "is_common", nullable = false)
    private Boolean common;

    public Long getMappingId() {
        return mappingId;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Boolean isCommon() {
        return common;
    }
}