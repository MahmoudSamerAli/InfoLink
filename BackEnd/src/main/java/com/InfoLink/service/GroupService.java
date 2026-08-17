package com.InfoLink.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.InfoLink.dto.AddGroupRequest;
import com.InfoLink.model.Groups;
import com.InfoLink.repository.GroupRepository;

@Service
public class GroupService {
    private final GroupRepository groupRepository;
    public GroupService(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }
    public List<Groups> getGroups() {
        return groupRepository.findAll();
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
