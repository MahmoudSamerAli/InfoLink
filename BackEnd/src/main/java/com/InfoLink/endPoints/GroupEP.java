package com.InfoLink.endPoints;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.InfoLink.dto.AddGroupRequest;
import com.InfoLink.model.Groups;
import com.InfoLink.service.GroupService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/group")
@SecurityRequirement(name = "bearerAuth")
public class GroupEP {
    private final GroupService groupService;

    public GroupEP(GroupService groupService) {
        this.groupService = groupService;
    }
    @GetMapping
    public ResponseEntity<List<Groups>> getGroups() {
        return ResponseEntity.ok(groupService.getGroups());
    }
    @PostMapping
    public ResponseEntity<Groups> createGroup(@Valid @RequestBody AddGroupRequest group) {
        groupService.save(group);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    @PutMapping("/{id}")
    public ResponseEntity<Groups> updateGroup(@PathVariable Long id,
                                              @Valid @RequestBody AddGroupRequest request) {
        try {
            Groups updated = groupService.updateGroup(id, request);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
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