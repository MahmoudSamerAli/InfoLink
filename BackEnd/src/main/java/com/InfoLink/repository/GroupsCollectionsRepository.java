package com.InfoLink.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.InfoLink.model.GroupsCollections;

public interface GroupsCollectionsRepository extends JpaRepository<GroupsCollections, Long> {
    List<GroupsCollections> findByGroupID(Long groupId);
    Optional<GroupsCollections> findByCollectionNameAndGroupID(String collectionName, Long groupId);
}
