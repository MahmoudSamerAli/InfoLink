package com.InfoLink.endPoints;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.InfoLink.dto.AddUserRequest;
import com.InfoLink.dto.PatchUserRequest;
import com.InfoLink.dto.UsersResponse;
import com.InfoLink.service.UserService;

import jakarta.validation.Valid;



@RestController
@RequestMapping("/users")
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
