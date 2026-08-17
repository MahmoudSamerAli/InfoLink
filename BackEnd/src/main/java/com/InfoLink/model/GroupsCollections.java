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
@Table(name = "GroupsCollections")
public class GroupsCollections {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY )
    @Column(name = "id", nullable = false)
    private Long table_id;

    @Column(name = "collection_name", nullable = false)
    private String collectionName;

    @Column(name = "GroupID", nullable = false)
    private Long groupID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "GroupID", referencedColumnName = "GroupID", insertable = false, updatable = false)
    private Groups group;

    public GroupsCollections() {}

    public GroupsCollections(Long collectionID, Long groupID) {
        this.table_id = collectionID;
        this.groupID = groupID;
    }

    public Long getCollectionID() { return table_id; }
    public void setCollectionID(Long collectionID) { this.table_id = collectionID; }

    public Long getGroupID() { return groupID; }
    public void setGroupID(Long groupID) { this.groupID = groupID; }

    public String getCollection() { return collectionName; }
    public void setCollection(String collectionName) { this.collectionName = collectionName; }

    public Groups getGroup() { return group; }
    public void setGroup(Groups group) { this.group = group; }
}