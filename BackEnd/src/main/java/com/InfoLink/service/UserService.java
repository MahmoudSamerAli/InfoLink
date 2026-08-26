package com.InfoLink.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.InfoLink.dto.AddUserRequest;
import com.InfoLink.dto.ChangePasswordRequest;
import com.InfoLink.dto.PatchUserRequest;
import com.InfoLink.dto.UsersResponse;
import com.InfoLink.model.Groups;
import com.InfoLink.model.Role;
import com.InfoLink.model.User;
import com.InfoLink.repository.GroupRepository;
import com.InfoLink.repository.UserRepository;
import com.InfoLink.security.CustomUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import com.InfoLink.dto.PagedResponse;

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
    @Transactional(readOnly = true)
    public PagedResponse<UsersResponse> getUsers(Pageable pageable) {
        Page<User> userPage = userRepository.findAll(pageable);
        List<UsersResponse> content = userPage.getContent()
            .stream()
            .map(user -> new UsersResponse(
                user.getUserID(),
                user.getUsername(),
                user.getFullName(),
                user.getGroup().getGroupID(),
                user.getGroup().getGroupName(),
                user.getRole(),
                user.getIsActive(),
                user.getCreatedDate()
            ))
            .collect(Collectors.toList());

        return new PagedResponse<>(
            content,
            userPage.getNumber(),
            userPage.getSize(),
            userPage.getTotalElements()
        );
    }

    @Transactional(readOnly = true)
    public PagedResponse<UsersResponse> getUsers(String keyword, Role role, Long groupId,
                             Boolean active, Pageable pageable) {
        Page<User> userPage = userRepository.search(
            keyword == null || keyword.isBlank() ? null : keyword.trim(),
            role, groupId, active, pageable);
        List<UsersResponse> content = userPage.getContent()
            .stream()
            .map(user -> new UsersResponse(
                user.getUserID(),
                user.getUsername(),
                user.getFullName(),
                user.getGroup().getGroupID(),
                user.getGroup().getGroupName(),
                user.getRole(),
                user.getIsActive(),
                user.getCreatedDate()
            ))
            .collect(Collectors.toList());

        return new PagedResponse<>(
            content,
            userPage.getNumber(),
            userPage.getSize(),
            userPage.getTotalElements()
        );
        }


    @Transactional(readOnly = true)
    public UsersResponse getUser(int id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return new UsersResponse(
            user.getUserID(),
            user.getUsername(),
            user.getFullName(),
            user.getGroup().getGroupID(),
            user.getGroup().getGroupName(),
            user.getRole(),
            user.getIsActive(),
            user.getCreatedDate()
        );
    }

    public User addUser(AddUserRequest request) {
        ensureCanManageRole(request.getRole());
        request.setUsername(request.getUsername().trim());
        request.setFullName(request.getFullName().trim());
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists: " + request.getUsername());
        }
        Groups group;
        if (request.getRole() == Role.ADMIN) {
            if (request.getGroupID() != null) {
                Groups requestedGroup = groupRepository.findById(request.getGroupID())
                    .orElseThrow(() -> new RuntimeException("Group not found with id: " + request.getGroupID()));
                if (!isAdminGroup(requestedGroup)) {
                    throw new IllegalArgumentException("Admin users must belong to the Admin group");
                }
            }
            group = groupRepository.findByGroupNameContainingIgnoreCase("Admin")
                .orElseThrow(() -> new RuntimeException("Admin group not found"));
        } else {
            if (request.getGroupID() == null) {
                throw new IllegalArgumentException("GroupID is required for non-admin users");
            }
            group = groupRepository.findById(request.getGroupID())
                    .orElseThrow(() -> new RuntimeException("Group not found with id: " + request.getGroupID()));
            if (isAdminGroup(group)) {
                throw new IllegalArgumentException("User accounts cannot belong to the Admin group");
            }
        }
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setFullName(request.getFullName());
        newUser.setPassword(hashedPassword);
        newUser.setGroup(group);
        newUser.setRole(request.getRole());
        newUser.setIsActive(request.getIsActive());
        return userRepository.save(newUser);
    }
    public User updateUser(PatchUserRequest request, int id) {
        return userRepository.findById(id)
            .map(user -> {
                ensureCanManageUser(user);
                if (request.getRole() != null) {
                    ensureCanManageRole(request.getRole());
                }
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
                    if (request.getRole() == Role.ADMIN) {
                        Groups adminGroup = groupRepository.findByGroupNameContainingIgnoreCase("Admin")
                                .orElseThrow(() -> new RuntimeException("Admin group not found"));
                        user.setGroup(adminGroup);
                    }
                }
                if (request.getIsActive() != null) {
                    user.setIsActive(request.getIsActive());
                }
                if (user.getRole() == Role.ADMIN) {
                    Groups adminGroup = groupRepository.findByGroupNameContainingIgnoreCase("Admin")
                            .orElseThrow(() -> new RuntimeException("Admin group not found"));
                    user.setGroup(adminGroup);
                }
                return userRepository.save(user);
        })
        .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }
    
    public void deleteUser(int id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        ensureCanManageUser(user);
        userRepository.deleteById(id);
    }

    public void changePassword(ChangePasswordRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getUser().getUserID())
            .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadCredentialsException("Current password is incorrect");
        }
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("New password and confirmation do not match");
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new IllegalArgumentException("New password must be different from the current password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    private void ensureCanManageUser(User user) {
        if ((isAdmin() && isPrivileged(user.getRole())) ||
            (isSysAdmin() && user.getRole() == Role.SYSADMIN)) {
            throw new AccessDeniedException("Admins cannot manage other admins or sysadmins");
        }
    }

    private void ensureCanManageRole(Role role) {
        if (role == Role.SYSADMIN) {
            throw new AccessDeniedException("Sysadmin accounts can only be added directly in the database");
        }
        if (isAdmin() && role == Role.ADMIN) {
            throw new AccessDeniedException("Admins cannot assign admin or sysadmin roles");
        }
    }

    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
            .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }

    private boolean isSysAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
            .anyMatch(authority -> "ROLE_SYSADMIN".equals(authority.getAuthority()));
    }

    private boolean isAdminGroup(Groups group) {
        return group.getGroupName() != null
            && group.getGroupName().toLowerCase().contains("admin");
    }

    private boolean isPrivileged(Role role) {
        return role == Role.ADMIN || role == Role.SYSADMIN;
    }

    public Optional<User> findByUsername(String username){
        return userRepository.findByUsername(username);
    }
}