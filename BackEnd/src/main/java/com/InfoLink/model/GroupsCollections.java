package com.InfoLink.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "Group_Collections")
public class GroupsCollections {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Collection_ID", nullable = false)
    private Long collectionId;

    @Column(name = "Collection_Name", nullable = false)
    private String collectionName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Group_ID", referencedColumnName = "Group_ID")
    private Groups group;

    public GroupsCollections() {}

    public GroupsCollections(Long collectionID,String collectionName ,Groups group) {
        this.collectionId = collectionID;
        this.collectionName = collectionName;
        this.group = group;
    }

    public Long getCollectionID() { return collectionId; }
    public void setCollectionID(Long collectionID) { this.collectionId = collectionID; }

    public String getCollectionName() { return collectionName; }
    public void setCollectionName(String collectionName) { this.collectionName = collectionName; }

    public Groups getGroup() { return group; }
    public void setGroup(Groups group) { this.group = group; }
}