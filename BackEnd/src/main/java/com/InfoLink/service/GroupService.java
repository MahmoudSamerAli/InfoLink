package com.InfoLink.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.InfoLink.dto.AddGroupRequest;
import com.InfoLink.dto.GroupsResponse;
import com.InfoLink.dto.PagedResponse;
import com.InfoLink.model.Groups;
import com.InfoLink.repository.GroupRepository;

@Service
public class GroupService {
    private final GroupRepository groupRepository;
    public GroupService(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }
    public PagedResponse<GroupsResponse> getGroups(Pageable pageable) {
        Page<Groups> groupPage = groupRepository.findAll(pageable);

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
    public Groups save(AddGroupRequest group) {
        Groups newGroup = new Groups();
        newGroup.setGroupName(group.getGroupName());
        newGroup.setGroupDescription(group.getGroupDescription());
        newGroup.setIsActive(group.getIsActive());
        return groupRepository.save(newGroup);
    }
    public Groups updateGroup(Long id, AddGroupRequest request) {
        Optional<Groups> existing = groupRepository.findById(id);
        if (existing.isPresent()) {
            Groups editGroup = existing.get();
            editGroup.setGroupName(request.getGroupName());
            editGroup.setGroupDescription(request.getGroupDescription());
            editGroup.setIsActive(request.getIsActive());
            return groupRepository.save(editGroup);
        } else {
            throw new RuntimeException("Group not found with id: " + id);
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
