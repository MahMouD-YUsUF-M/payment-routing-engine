package com.fawry.paymentroutingengine.controller;

import com.fawry.paymentroutingengine.dto.request.LoginRequest;
import com.fawry.paymentroutingengine.dto.response.ApiResponse;
import com.fawry.paymentroutingengine.dto.response.LoginResponse;
import com.fawry.paymentroutingengine.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Authentication endpoints for login and token generation")
public class AuthController {

    private final AuthService authService;


    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return JWT token")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request received for username: {}", request.getUsername());

        LoginResponse response = authService.login(request);

        return ResponseEntity.ok(
                ApiResponse.success(response, "Login successful")
        );
    }


    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if auth service is running")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(
                ApiResponse.success("Auth service is running", "Service healthy")
        );
    }
}