package com.InfoLink.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.InfoLink.dto.AddUserRequest;
import com.InfoLink.dto.PatchUserRequest;
import com.InfoLink.dto.UsersResponse;
import com.InfoLink.model.Groups;
import com.InfoLink.model.User;
import com.InfoLink.repository.GroupRepository;
import com.InfoLink.repository.UserRepository;

@Service
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;

    public UserService(PasswordEncoder passwordEncoder,
                       UserRepository userRepository,
                       GroupRepository groupRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
    }
    public List<UsersResponse> getUsers() {
        return userRepository.findAll()
                .stream()
                .map(user -> new UsersResponse(
                    user.getUserID(),
                    user.getUsername(),
                    user.getFullName(),
                    user.getGroup().getGroupID(),
                    user.getRole(),
                    user.getIsActive(),
                    user.getCreatedDate()
                ))
                .collect(Collectors.toList());
    }

    public UsersResponse getUser(int id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return new UsersResponse(
            user.getUserID(),
            user.getUsername(),
            user.getFullName(),
            user.getGroup().getGroupID(),
            user.getRole(),
            user.getIsActive(),
            user.getCreatedDate()
        );
    }

    public User addUser(AddUserRequest request) {
        request.setUsername(request.getUsername().trim());
        request.setFullName(request.getFullName().trim());
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists: " + request.getUsername());
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists: " + request.getUsername());
        }
        Groups group = groupRepository.findById(request.getGroupID())
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + request.getGroupID()));
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setFullName(request.getFullName());
        newUser.setPassword(hashedPassword);
        newUser.setGroup(group);
        newUser.setRole(request.getRole());
        newUser.setIsActive(true);
        return userRepository.save(newUser);
    }
    public User updateUser(PatchUserRequest request) {
        return userRepository.findById(request.getUserID())
            .map(user -> {
                if (request.getUsername() != null) {
                    user.setUsername(request.getUsername().trim());
                }
                if (request.getFullName() != null) {
                    user.setFullName(request.getFullName().trim());
                }
                if (request.getPassword() != null && !request.getPassword().isBlank()) {
                    user.setPassword(passwordEncoder.encode(request.getPassword()));
                }
                if (request.getGroupID() != null) {
                    Groups group = groupRepository.findById(request.getGroupID())
                        .orElseThrow(() -> new RuntimeException("Group not found with id: " + request.getGroupID()));
                    user.setGroup(group);
                }
                if (request.getRole() != null) {
                    user.setRole(request.getRole());
                }
                if (request.getIsActive() != null) {
                    user.setIsActive(request.getIsActive());
                }
                return userRepository.save(user);
        })
        .orElseThrow(() -> new RuntimeException("User not found with id: " + request.getUserID()));
    }
    
    public void deleteUser(int id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }
    public Optional<User> findByUsername(String username){
        return userRepository.findByUsername(username);
    }
}
