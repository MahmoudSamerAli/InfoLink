package com.InfoLink.endPoints;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.InfoLink.model.GroupsCollections;
import com.InfoLink.service.GroupsCollectionsService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/collections")
@SecurityRequirement(name = "bearerAuth")
public class CollectionController {

    private final GroupsCollectionsService groupsCollectionsService;

    public CollectionController(GroupsCollectionsService groupsCollectionsService) {
        this.groupsCollectionsService = groupsCollectionsService;
    }

    private boolean isValidName(String name) {
        return name.matches("^[a-zA-Z][a-zA-Z0-9_]{0,63}$");
    }

    @PostMapping
    public ResponseEntity<String> createCollection(
            @RequestParam String name,
            @RequestParam(required = false) Long groupId) {

        if (!isValidName(name)) {
            return ResponseEntity.badRequest()
                    .body("Invalid collection name. Use letters, numbers, underscores; must start with a letter.");
        }
        try {
            groupsCollectionsService.createCollection(name, groupId);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body("Collection '" + name + "' created");
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<String> deleteCollection(@PathVariable String name) {
        try {
            groupsCollectionsService.deleteCollection(name);
        } catch (IllegalStateException e) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok("Collection '" + name + "' deleted");
    }

    @PostMapping("/{name}/groups/{groupId}")
    public ResponseEntity<String> grantGroupAccess(
            @PathVariable String name, @PathVariable Long groupId) {
        try {
            groupsCollectionsService.grantAccess(name, groupId);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        return ResponseEntity.ok("Group " + groupId + " granted access to '" + name + "'");
    }

    @DeleteMapping("/{name}/groups/{groupId}")
    public ResponseEntity<String> revokeGroupAccess(
            @PathVariable String name, @PathVariable Long groupId) {
        groupsCollectionsService.revokeAccess(name, groupId);
        return ResponseEntity.ok("Group " + groupId + " access to '" + name + "' revoked");
    }

    @GetMapping("/{name}/groups")
    public ResponseEntity<List<GroupsCollections>> listGroupsForCollection(@PathVariable String name) {
        return ResponseEntity.ok(groupsCollectionsService.getGroupsForCollection(name));
    }
    @PostMapping(value = "/{name}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public ResponseEntity<String> uploadData(
        @PathVariable String name,
        @RequestParam("file") MultipartFile file) {
    try {
        int count = groupsCollectionsService.uploadData(name, file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(count + " document(s) uploaded to '" + name + "'");
    } catch (IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    } catch (IOException e) {
        return ResponseEntity.internalServerError().body("Failed to read uploaded file");
    }
}
}