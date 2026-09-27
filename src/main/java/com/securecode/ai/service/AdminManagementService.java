package com.securecode.ai.service;

import com.securecode.ai.dto.AdminUserRequest;
import com.securecode.ai.dto.AdminUserResponse;
import com.securecode.ai.entity.Role;
import com.securecode.ai.entity.User;
import com.securecode.ai.repository.UserRepository;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Admin-only user management, intentionally separate from student profile operations. */
@Service
public class AdminManagementService {
    private final UserRepository users;
    private final PasswordEncoder passwords;

    public AdminManagementService(UserRepository users, PasswordEncoder passwords) {
        this.users = users;
        this.passwords = passwords;
    }

    @Transactional(readOnly = true)
    public java.util.List<AdminUserResponse> users() {
        return users.findAll().stream().map(this::response).toList();
    }

    @Transactional
    public AdminUserResponse createUser(AdminUserRequest request) {
        String email = email(request.email());
        if (users.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists");
        }
        if (request.password() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A password is required when creating a user");
        }
        return response(users.save(new User(request.name().trim(), email, passwords.encode(request.password()), request.role())));
    }

    @Transactional
    public AdminUserResponse updateUser(Long id, AdminUserRequest request, String actingEmail) {
        User user = user(id);
        String requestedEmail = email(request.email());
        if (!user.getEmail().equals(requestedEmail)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User email cannot be changed");
        }
        ensureAdminRemains(user, request.role(), actingEmail);
        user.changeName(request.name().trim());
        user.changeRole(request.role());
        if (request.password() != null && !request.password().isBlank()) {
            user.changePasswordHash(passwords.encode(request.password()));
        }
        return response(user);
    }

    @Transactional
    public void deleteUser(Long id, String actingEmail) {
        User user = user(id);
        if (user.getEmail().equalsIgnoreCase(actingEmail)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot delete your own account");
        }
        if (user.getRole() == Role.ADMIN && users.countByRole(Role.ADMIN) <= 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one administrator account must remain");
        }
        users.delete(user);
    }

    private void ensureAdminRemains(User user, Role requestedRole, String actingEmail) {
        if (user.getRole() == Role.ADMIN && requestedRole != Role.ADMIN && users.countByRole(Role.ADMIN) <= 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one administrator account must remain");
        }
        if (user.getEmail().equalsIgnoreCase(actingEmail) && requestedRole != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot remove your own administrator access");
        }
    }

    private User user(Long id) {
        return users.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private AdminUserResponse response(User user) {
        return new AdminUserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole().name(), user.getCreatedAt());
    }

    private String email(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
