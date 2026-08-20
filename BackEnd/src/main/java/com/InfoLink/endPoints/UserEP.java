package com.InfoLink.endPoints;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.InfoLink.dto.AddUserRequest;
import com.InfoLink.dto.PatchUserRequest;
import com.InfoLink.dto.UsersResponse;
import com.InfoLink.service.UserService;

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
    public ResponseEntity<List<UsersResponse>> getUsers() {
        List<UsersResponse> users = userService.getUsers();
        return ResponseEntity.ok(users);
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
            @PathVariable String id,
            @Valid @RequestBody PatchUserRequest request) {

        userService.updateUser(request);

        return ResponseEntity.ok("User updated successfully");
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteUser(
            @PathVariable int id) {

        userService.deleteUser(id);

        return ResponseEntity.ok("User deleted successfully");
    }
}
