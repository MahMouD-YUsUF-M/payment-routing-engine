package com.fawry.paymentroutingengine.controller;

import com.fawry.paymentroutingengine.dto.request.PaymentRecommendationRequest;
import com.fawry.paymentroutingengine.dto.request.PaymentSplitRequest;
import com.fawry.paymentroutingengine.dto.response.ApiResponse;
import com.fawry.paymentroutingengine.dto.response.GatewayRecommendationResponse;
import com.fawry.paymentroutingengine.service.TransactionService;
import com.fawry.paymentroutingengine.dto.response.PaymentSplitResponse;
import com.fawry.paymentroutingengine.service.RoutingAlgorithmService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Payment Routing Controller
 * Handles gateway recommendation and payment splitting
 */
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payment Routing", description = "Payment routing and gateway recommendation endpoints")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private final RoutingAlgorithmService routingAlgorithmService;

    @PostMapping("/recommend")
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLER', 'USER')")
    @Operation(summary = "Recommend gateway",
            description = "Get optimal gateway recommendation based on amount, urgency, and availability")
    public ResponseEntity<ApiResponse<GatewayRecommendationResponse>> recommendGateway(
            @Valid @RequestBody PaymentRecommendationRequest request) {
        log.info("Gateway recommendation request for biller: {}, amount: {}, urgency: {}",
                request.getBillerCode(), request.getAmount(), request.getUrgency());

        GatewayRecommendationResponse response = routingAlgorithmService.recommendGateway(request);

        return ResponseEntity.ok(
                ApiResponse.success(response, "Gateway recommendation generated successfully")
        );
    }

}