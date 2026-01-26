package com.fawry.paymentroutingengine.controller;


import com.fawry.paymentroutingengine.dto.request.GatewayCreateRequest;
import com.fawry.paymentroutingengine.dto.request.GatewayUpdateRequest;
import com.fawry.paymentroutingengine.dto.response.ApiResponse;
import com.fawry.paymentroutingengine.dto.response.GatewayResponse;
import com.fawry.paymentroutingengine.service.GatewayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api/admin/gateways")
@AllArgsConstructor
@Slf4j
@Tag(name = "Gateway Management", description = "Admin endpoints for managing payment gateways")
@SecurityRequirement(name = "bearerAuth")
public class GatewayController {

    private GatewayService gatewayService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create gateway", description = "Create a new payment gateway (code auto-generated)")
    public ResponseEntity<GatewayResponse> createGateway(
            @Valid @RequestBody GatewayCreateRequest request
    ) {
        log.info("Create gateway request received for: {}", request.getName());
        GatewayResponse response = gatewayService.createGateway(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response , "Gateway created successfully with code: " + response.getCode()).getData());
    }

    @PutMapping("/{code}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update gateway", description = "Update Gateway")
    public ResponseEntity<GatewayResponse> updateGateway(
            @PathVariable String code,
            @Valid @RequestBody GatewayUpdateRequest request
    )
    {
        log.info("Update gateway request received for: {}", request.getName());
        GatewayResponse response = gatewayService.updateGateway(code ,request);

        return ResponseEntity.ok(
                ApiResponse.success(response , "Gateway updated successfully with code: " + response.getCode()).getData()
        );
    }


    @GetMapping("/{code}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Get gateway by code", description = "Retrieve gateway details by code")
    public ResponseEntity<ApiResponse<GatewayResponse>> getGatewayByCode(@PathVariable String code) {
        log.info("Get gateway request received for code: {}", code);

        GatewayResponse response = gatewayService.getGatewayByCode(code);

        return ResponseEntity.ok(
                ApiResponse.success(response, "Gateway retrieved successfully")
        );
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Get all gateways", description = "Retrieve all payment gateways")
    public ResponseEntity<ApiResponse<List<GatewayResponse>>> getAllGateways() {
        log.info("Get all gateways request received");

        List<GatewayResponse> response = gatewayService.getAllGateways();

        return ResponseEntity.ok(
                ApiResponse.success(response, "Retrieved " + response.size() + " gateways")
        );
    }

}
