package com.fawry.paymentroutingengine.controller;

import com.fawry.paymentroutingengine.dto.request.BillerCreateRequest;
import com.fawry.paymentroutingengine.dto.response.ApiResponse;
import com.fawry.paymentroutingengine.dto.response.BillerResponse;
import com.fawry.paymentroutingengine.service.BillerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Biller Management Controller
 * Handles CRUD operations for billers
 */
@RestController
@RequestMapping("/api/admin/billers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Biller Management", description = "Admin endpoints for managing billers")
@SecurityRequirement(name = "bearerAuth")
public class BillerController {

    private final BillerService billerService;


    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create biller", description = "Create a new biller account")
    public ResponseEntity<ApiResponse<BillerResponse>> createBiller(
            @Valid @RequestBody BillerCreateRequest request) {
        log.info("Create biller request received for code: {}", request.getCodeBiller());

        BillerResponse response = billerService.createBiller(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Biller created successfully"));
    }




    @GetMapping("/{code}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLER')")
    @Operation(summary = "Get biller by code", description = "Retrieve biller details by code")
    public ResponseEntity<ApiResponse<BillerResponse>> getBillerByCode(@PathVariable String code) {
        log.info("Get biller request received for code: {}", code);

        BillerResponse response = billerService.getBillerByCode(code);

        return ResponseEntity.ok(
                ApiResponse.success(response, "Biller retrieved successfully")
        );
    }
}