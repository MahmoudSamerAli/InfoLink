package com.InfoLink.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import com.InfoLink.model.Groups;
import com.InfoLink.model.GroupsCollections;

public interface GroupsCollectionsRepository extends JpaRepository<GroupsCollections, Long> {List<GroupsCollections> findByGroup_GroupID(Long groupId);
    Optional<GroupsCollections> findByCollectionNameAndGroup(String collectionName, Groups group);
    List<GroupsCollections> findByCollectionName(String collectionName);
    void deleteByCollectionNameAndGroup_GroupID(String collectionName, Long groupId);
    boolean existsByCollectionNameAndGroup_GroupID(String collectionName, Long groupId);
}
