package com.securecode.ai.controller;

import com.securecode.ai.dto.ChangePasswordRequest;
import com.securecode.ai.dto.SubmissionHistoryResponse;
import com.securecode.ai.dto.StudentAnalyticsResponse;
import com.securecode.ai.dto.UpdateProfileRequest;
import com.securecode.ai.dto.UserSummaryResponse;
import com.securecode.ai.entity.User;
import com.securecode.ai.repository.UserRepository;
import com.securecode.ai.service.StudentDataService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/student")
public class StudentController {
    private final StudentDataService studentData;
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    public StudentController(StudentDataService studentData, UserRepository users, PasswordEncoder passwordEncoder) { this.studentData = studentData; this.users = users; this.passwordEncoder = passwordEncoder; }

    @GetMapping("/summary") public UserSummaryResponse summary(Authentication auth) { return studentData.summary(auth.getName()); }
    @GetMapping("/history") public List<SubmissionHistoryResponse> history(Authentication auth) { return studentData.history(auth.getName()); }
    @GetMapping("/analytics") public StudentAnalyticsResponse analytics(Authentication auth) { return studentData.analytics(auth.getName()); }
    @PutMapping("/profile") public UserSummaryResponse updateProfile(Authentication auth, @Valid @RequestBody UpdateProfileRequest request) {
        User user = current(auth); user.changeName(request.name().trim()); users.save(user); return studentData.summary(auth.getName());
    }
    @PutMapping("/password") public void changePassword(Authentication auth, @Valid @RequestBody ChangePasswordRequest request) {
        User user = current(auth);
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is incorrect");
        user.changePasswordHash(passwordEncoder.encode(request.newPassword())); users.save(user);
    }
    private User current(Authentication auth) { return users.findByEmail(auth.getName()).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found")); }
}
