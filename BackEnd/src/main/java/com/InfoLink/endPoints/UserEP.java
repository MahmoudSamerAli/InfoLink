package com.InfoLink.endPoints;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.InfoLink.dto.AddUserRequest;
import com.InfoLink.dto.PagedResponse;
import com.InfoLink.dto.PatchUserRequest;
import com.InfoLink.dto.ProfileResponse;
import com.InfoLink.dto.UsersResponse;
import com.InfoLink.model.User;
import com.InfoLink.security.CustomUserDetails;
import com.InfoLink.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;


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
    public PagedResponse<UsersResponse> getUsers(Pageable pageable) {
        return userService.getUsers(pageable);
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
