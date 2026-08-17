package com.InfoLink.service;
import java.util.List;
import org.springframework.stereotype.Service;
import com.InfoLink.model.GroupsCollections;
import com.InfoLink.repository.GroupsCollectionsRepository;

@Service
public class GroupsCollectionsService {

    private final GroupsCollectionsRepository repo;

    public GroupsCollectionsService(GroupsCollectionsRepository repo) {
        this.repo = repo;
    }

    public List<GroupsCollections> getCollectionsForGroup(Long groupId) {
        return repo.findByGroupID(groupId);
    }

    public GroupsCollections getCollectionForGroup(String collectionName, Long groupId) {
        return repo.findByCollectionNameAndGroupID(collectionName, groupId)
                   .orElseThrow(() -> new RuntimeException("Collection not accessible for this group"));
    }
}
