package com.securecode.ai.service;

import com.securecode.ai.dto.AuthenticationResponse;
import com.securecode.ai.dto.LoginRequest;
import com.securecode.ai.dto.RegisterRequest;
import com.securecode.ai.entity.Role;
import com.securecode.ai.entity.User;
import com.securecode.ai.repository.UserRepository;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       SecurityContextRepository securityContextRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
    }

    @Transactional
    public AuthenticationResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists");
        }

        User user = userRepository.save(new User(email, passwordEncoder.encode(request.password()), Role.STUDENT));
        return authenticationResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthenticationResponse login(LoginRequest request, HttpServletRequest httpRequest,
                                        HttpServletResponse httpResponse) {
        String email = normalizeEmail(request.email());
        try {
            Authentication authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(email, request.password()));
            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(authentication);
            SecurityContextHolder.setContext(securityContext);
            securityContextRepository.saveContext(securityContext, httpRequest, httpResponse);
        } catch (org.springframework.security.core.AuthenticationException exception) {
            throw invalidCredentials();
        }
        User user = userRepository.findByEmail(email).orElseThrow(this::invalidCredentials);
        return authenticationResponse(user, "Login successful.");
    }

    private AuthenticationResponse authenticationResponse(User user) {
        return authenticationResponse(user, "User registered successfully.");
    }

    private AuthenticationResponse authenticationResponse(User user, String message) {
        return new AuthenticationResponse(
                message,
                user.getEmail(),
                user.getRole().name());
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private ResponseStatusException invalidCredentials() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
    }
}
