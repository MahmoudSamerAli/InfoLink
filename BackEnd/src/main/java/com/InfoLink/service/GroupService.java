package com.InfoLink.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import com.InfoLink.dto.AddGroupRequest;
import com.InfoLink.dto.GroupsResponse;
import com.InfoLink.dto.PagedResponse;
import com.InfoLink.model.Groups;
import com.InfoLink.repository.GroupRepository;

@Service
public class GroupService {
    private final GroupRepository groupRepository;
    private final GroupsCollectionsService groupsCollectionsService;
    public GroupService(GroupRepository groupRepository, GroupsCollectionsService groupsCollectionsService) {
        this.groupRepository = groupRepository;
        this.groupsCollectionsService = groupsCollectionsService;
    }
    public PagedResponse<GroupsResponse> getGroups(Pageable pageable) {
        return getGroups(null, null, pageable);
    }

    public PagedResponse<GroupsResponse> getGroups(String keyword, Pageable pageable) {
        return getGroups(keyword, null, pageable);
    }

    public PagedResponse<GroupsResponse> getGroups(String keyword, Boolean active, Pageable pageable) {
        Page<Groups> groupPage = groupRepository.search(
                keyword == null || keyword.isBlank() ? null : keyword.trim(), active, pageable);

        List<GroupsResponse> content = groupPage.getContent()
                .stream()
                .map(group -> new GroupsResponse(
                        group.getGroupID(),
                        group.getGroupName(),
                        group.getGroupDescription(),
                        group.getIsActive()
                ))
                .collect(Collectors.toList());

        return new PagedResponse<>(
                content,
                groupPage.getNumber(),
                groupPage.getSize(),
                groupPage.getTotalElements()
        );
    }
    @Transactional
    public Groups save(AddGroupRequest group) {
        Groups newGroup = new Groups();
        newGroup.setGroupName(group.getGroupName());
        newGroup.setGroupDescription(group.getGroupDescription());
        newGroup.setIsActive(group.getIsActive());
        Groups savedGroup = groupRepository.save(newGroup);
        if (group.getCollections() != null) {
            groupsCollectionsService.synchronizeGroupCollections(savedGroup, group.getCollections());
        }
        return savedGroup;
    }
    @Transactional
    public Groups updateGroup(Long id, AddGroupRequest request) {
        Optional<Groups> existing = groupRepository.findById(id);
        if (existing.isPresent()) {
            Groups editGroup = existing.get();
            editGroup.setGroupName(request.getGroupName());
            editGroup.setGroupDescription(request.getGroupDescription());
            editGroup.setIsActive(request.getIsActive());
            Groups updatedGroup = groupRepository.save(editGroup);
            if (request.getCollections() != null) {
                groupsCollectionsService.synchronizeGroupCollections(updatedGroup, request.getCollections());
            }
            return updatedGroup;
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found with id: " + id);
        }
    }
    public boolean deleteGroup(Long id) {
        if (groupRepository.existsById(id)) {
            groupRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
