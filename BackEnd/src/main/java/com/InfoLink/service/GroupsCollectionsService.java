package com.InfoLink.service;
import java.util.List;
import org.springframework.stereotype.Service;

import com.InfoLink.model.Groups;
import com.InfoLink.model.GroupsCollections;
import com.InfoLink.repository.GroupsCollectionsRepository;

@Service
public class GroupsCollectionsService {

    private final GroupsCollectionsRepository repo;

    public GroupsCollectionsService(GroupsCollectionsRepository repo) {
        this.repo = repo;
    }

    public List<GroupsCollections> getCollectionsForGroup(Long groupId) {
        return repo.findByGroup_GroupID(groupId);
    }

    public GroupsCollections getCollectionForGroup(String collectionName, Groups group) {
        return repo.findByCollectionNameAndGroup(collectionName, group)
                   .orElseThrow(() -> new RuntimeException("Collection not accessible for this group"));
    }
}
