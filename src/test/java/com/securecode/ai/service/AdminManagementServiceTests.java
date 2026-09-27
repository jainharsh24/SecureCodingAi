package com.securecode.ai.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.securecode.ai.dto.AdminUserRequest;
import com.securecode.ai.entity.Role;
import com.securecode.ai.entity.User;
import com.securecode.ai.repository.UserRepository;
import java.lang.reflect.Field;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

class AdminManagementServiceTests {
    private final UserRepository users = Mockito.mock(UserRepository.class);
    private final AdminManagementService service = new AdminManagementService(users, new BCryptPasswordEncoder());

    @Test
    void createsAndUpdatesUsersWithoutExposingPasswordHash() throws Exception {
        User user = user(7L, "Student", "student@example.test", Role.STUDENT);
        when(users.existsByEmail("student@example.test")).thenReturn(false);
        when(users.save(any(User.class))).thenReturn(user);

        var created = service.createUser(new AdminUserRequest("Student", "student@example.test", Role.STUDENT, "long-enough-password"));
        assertThat(created.email()).isEqualTo("student@example.test");
        assertThat(created.role()).isEqualTo("STUDENT");

        when(users.findById(7L)).thenReturn(Optional.of(user));
        var updated = service.updateUser(7L, new AdminUserRequest("Updated", "student@example.test", Role.ADMIN, null), "admin@example.test");
        assertThat(updated.name()).isEqualTo("Updated");
        assertThat(updated.role()).isEqualTo("ADMIN");
    }

    @Test
    void protectsTheLastAdministrator() throws Exception {
        User admin = user(1L, "Admin", "admin@example.test", Role.ADMIN);
        when(users.findById(1L)).thenReturn(Optional.of(admin));
        when(users.countByRole(Role.ADMIN)).thenReturn(1L);

        assertThatThrownBy(() -> service.deleteUser(1L, "other@example.test"))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(error -> ((ResponseStatusException) error).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    private User user(Long id, String name, String email, Role role) throws Exception {
        User user = new User(name, email, "hash", role);
        Field idField = User.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(user, id);
        return user;
    }
}
