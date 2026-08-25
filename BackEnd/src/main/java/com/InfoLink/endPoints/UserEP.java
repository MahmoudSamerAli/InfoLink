package com.InfoLink.endPoints;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.InfoLink.dto.AddUserRequest;
import com.InfoLink.dto.ChangePasswordRequest;
import com.InfoLink.dto.PagedResponse;
import com.InfoLink.dto.PatchUserRequest;
import com.InfoLink.dto.ProfileResponse;
import com.InfoLink.dto.UsersResponse;
import com.InfoLink.model.User;
import com.InfoLink.model.Role;
import com.InfoLink.security.CustomUserDetails;
import com.InfoLink.utils.service.UserService;

import org.springframework.data.domain.Pageable;


import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;



@RestController
@RequestMapping("/users")
@SecurityRequirement(name = "bearerAuth")
public class UserEP {

    private final UserService userService;

    public UserEP(UserService userService) {
        this.userService = userService;
    }
    @GetMapping
    public PagedResponse<UsersResponse> getUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) Long groupId,
            @RequestParam(required = false) Boolean active,
            Pageable pageable) {
        return userService.getUsers(keyword, role, groupId, active, pageable);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<UsersResponse> getUser(@PathVariable int id) {
        UsersResponse user = userService.getUser(id);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/add")
    public ResponseEntity<String> addUser(
            @Valid @RequestBody AddUserRequest request) {

        userService.addUser(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("User added successfully");
    }

    @PatchMapping("/{id}")
    public ResponseEntity<String> updateUser(
            @PathVariable int id,
            @Valid @RequestBody PatchUserRequest request) {

        userService.updateUser(request, id);

        return ResponseEntity.ok("User updated successfully");
    }

    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {

        userService.changePassword(request);

        return ResponseEntity.ok("Password changed successfully");
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteUser(
            @PathVariable int id) {

        userService.deleteUser(id);

        return ResponseEntity.ok("User deleted successfully");
    }
    @GetMapping("/profile")
    public ProfileResponse getProfile() {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();

        User user = userDetails.getUser();

        return new ProfileResponse(
                user.getUserID(),
                user.getUsername(),
                user.getFullName(),
                user.getGroup().getGroupID(),
                user.getGroup().getGroupName(),
                user.getRole().name(),
                user.getIsActive(),
                user.getCreatedDate()
        );
    }
}
