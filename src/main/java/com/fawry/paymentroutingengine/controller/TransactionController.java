package com.fawry.paymentroutingengine.controller;

import com.fawry.paymentroutingengine.dto.request.TransactionCreateRequest;
import com.fawry.paymentroutingengine.dto.response.ApiResponse;
import com.fawry.paymentroutingengine.dto.response.TransactionResponse;
import com.fawry.paymentroutingengine.dto.response.TransactionSummaryResponse;
import com.fawry.paymentroutingengine.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Transaction Controller
 * Handles transaction creation and history queries
 */
@RestController
@RequestMapping("/api/billers/{billerCode}/transactions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Transactions", description = "Transaction management and history endpoints")
@SecurityRequirement(name = "bearerAuth")
public class TransactionController {

    private final TransactionService transactionService;


    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLER')")
    @Operation(summary = "Create transaction", description = "Create a new payment transaction")
    public ResponseEntity<ApiResponse<TransactionResponse>> createTransaction(
            @PathVariable String billerCode,
            @Valid @RequestBody TransactionCreateRequest request) {
        log.info("Create transaction request for biller: {}, amount: {}", billerCode, request.getAmount());

        TransactionResponse response = transactionService.createTransaction(billerCode, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Transaction created successfully"));
    }


    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLER')")
    @Operation(summary = "Get transactions", description = "Get transaction history for a biller, optionally filtered by date")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getTransactions(
            @PathVariable String billerCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("Get transactions request for biller: {}, date: {}", billerCode, date);

        List<TransactionResponse> response = transactionService.getTransactionsByBiller(billerCode, date);

        return ResponseEntity.ok(
                ApiResponse.success(response, "Retrieved " + response.size() + " transactions")
        );
    }


    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLER')")
    @Operation(summary = "Get transaction summary",
            description = "Get aggregated transaction summary by gateway and status")
    public ResponseEntity<ApiResponse<TransactionSummaryResponse>> getTransactionSummary(
            @PathVariable String billerCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("Get transaction summary for biller: {}, period: {} to {}",
                billerCode, startDate, endDate);

        TransactionSummaryResponse response = transactionService.getTransactionSummary(
                billerCode, startDate, endDate);

        return ResponseEntity.ok(
                ApiResponse.success(response, "Transaction summary generated successfully")
        );
    }
}