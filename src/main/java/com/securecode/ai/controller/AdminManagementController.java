package com.securecode.ai.controller;

import com.securecode.ai.dto.AdminUserRequest;
import com.securecode.ai.dto.AdminUserResponse;
import com.securecode.ai.service.AdminManagementService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** The API boundary for the isolated administrative area. */
@RestController
@RequestMapping("/api/admin/users")
public class AdminManagementController {
    private final AdminManagementService adminManagement;

    public AdminManagementController(AdminManagementService adminManagement) {
        this.adminManagement = adminManagement;
    }

    @GetMapping
    public List<AdminUserResponse> users() { return adminManagement.users(); }

    @PostMapping
    public ResponseEntity<AdminUserResponse> create(@Valid @RequestBody AdminUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminManagement.createUser(request));
    }

    @PutMapping("/{id}")
    public AdminUserResponse update(@PathVariable Long id, @Valid @RequestBody AdminUserRequest request, Authentication authentication) {
        return adminManagement.updateUser(id, request, authentication.getName());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        adminManagement.deleteUser(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
