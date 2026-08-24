package com.InfoLink.endPoints;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.InfoLink.dto.AddGroupRequest;
import com.InfoLink.dto.GroupsResponse;
import com.InfoLink.dto.PagedResponse;
import com.InfoLink.model.Groups;
import com.InfoLink.service.GroupService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/group")
@SecurityRequirement(name = "bearerAuth")
public class GroupEP {
    private final GroupService groupService;

    public GroupEP(GroupService groupService) {
        this.groupService = groupService;
    }

    @GetMapping
    public PagedResponse<GroupsResponse> getGroups(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean active,
            Pageable pageable) {
        return groupService.getGroups(keyword, active, pageable);
    }

    @PostMapping
    public ResponseEntity<Groups> createGroup(@Valid @RequestBody AddGroupRequest group) {
        groupService.save(group);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Groups> updateGroup(@PathVariable Long id,
            @Valid @RequestBody AddGroupRequest request) {
        return ResponseEntity.ok(groupService.updateGroup(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteGroup(@PathVariable Long id) {
        boolean deleted = groupService.deleteGroup(id);
        if (deleted) {
            return ResponseEntity.ok("Group deleted successfully");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Group not found");
        }
    }
}