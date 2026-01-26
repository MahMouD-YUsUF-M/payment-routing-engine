package com.fawry.paymentroutingengine.service;

import com.fawry.paymentroutingengine.dto.request.LoginRequest;
import com.fawry.paymentroutingengine.dto.response.LoginResponse;
import com.fawry.paymentroutingengine.entity.User;
import com.fawry.paymentroutingengine.exception.AuthenticationException;
import com.fawry.paymentroutingengine.repository.UserRepository;
import com.fawry.paymentroutingengine.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt for username: {}", request.getUsername());

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AuthenticationException("Invalid username or password"));

        if (!user.getIsActive()) {
            throw new AuthenticationException("User account is inactive");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AuthenticationException("Invalid username or password");
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());

        log.info("Login successful for user: {}", user.getUsername());

        return LoginResponse.builder()
                .token(token)
                .type("Bearer")
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}