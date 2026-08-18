package com.InfoLink.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "Group_Collections")
public class GroupsCollections {

    @Id
    @Column(name = "Collection_ID", nullable = false)
    private Long table_id;

    @Column(name = "Collection_Name", nullable = false)
    private String collectionName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Group_ID", referencedColumnName = "Group_ID", insertable = false, updatable = false)
    private Groups group;

    public GroupsCollections() {}

    public GroupsCollections(Long collectionID,String collectionName ,Groups group) {
        this.table_id = collectionID;
        this.collectionName = collectionName;
        this.group = group;
    }

    public Long getCollectionID() { return table_id; }
    public void setCollectionID(Long collectionID) { this.table_id = collectionID; }

    public String getCollection() { return collectionName; }
    public void setCollection(String collectionName) { this.collectionName = collectionName; }

    public Groups getGroup() { return group; }
    public void setGroup(Groups group) { this.group = group; }
}