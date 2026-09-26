package com.securecode.ai.service;

import com.securecode.ai.dto.AuthenticationResponse;
import com.securecode.ai.dto.LoginRequest;
import com.securecode.ai.dto.RegisterRequest;
import com.securecode.ai.entity.Role;
import com.securecode.ai.entity.User;
import com.securecode.ai.repository.UserRepository;
import com.securecode.ai.security.AuthTokenService;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final AuthTokenService tokens;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager, AuthTokenService tokens) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokens = tokens;
    }

    @Transactional
    public AuthenticationResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists");
        }

        String name = request.name() == null || request.name().isBlank() ? email.substring(0, email.indexOf('@')) : request.name().trim();
        User user = userRepository.save(new User(name, email, passwordEncoder.encode(request.password()), Role.STUDENT));
        return authenticationResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthenticationResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        try {
            Authentication authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(email, request.password()));
        } catch (org.springframework.security.core.AuthenticationException exception) {
            throw invalidCredentials();
        }
        User user = userRepository.findByEmail(email).orElseThrow(this::invalidCredentials);
        return authenticationResponse(user, "Login successful.", tokens.issue(user.getEmail()));
    }

    @Transactional(readOnly = true)
    public AuthenticationResponse currentUser(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(this::invalidCredentials);
        return authenticationResponse(user, "Authenticated user.", null);
    }

    private AuthenticationResponse authenticationResponse(User user) {
        return authenticationResponse(user, "User registered successfully.", null);
    }

    private AuthenticationResponse authenticationResponse(User user, String message, String token) {
        return new AuthenticationResponse(
                message,
                user.getName(),
                user.getEmail(),
                user.getRole().name(), token);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private ResponseStatusException invalidCredentials() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
    }
}
