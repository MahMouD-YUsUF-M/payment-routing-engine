package com.fawry.paymentroutingengine.dto.response;

import com.fawry.paymentroutingengine.constant.Status;
import com.fawry.paymentroutingengine.constant.Urgency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for transaction details
 *
 * Example JSON:
 * {
 *
 *   "code": "TXN_20250123_001",
 *   "billerCode": "BILL_12345",
 *   "gatewayCode": "gateway_1",
 *   "gatewayName": "Vodafone Cash",
 *   "amount": 1000.00,
 *   "commission": 17.00,
 *   "urgency": "INSTANT",
 *   "status": "COMPLETED",
 *   "processingTime": "Instant",
 *   "failureReason": null,
 *   "createdAt": "2025-01-23T14:30:00",
 *   "completedAt": "2025-01-23T14:30:05"
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {


    private String code;

    private String billerCode;

    private String gatewayCode;

    private String gatewayName;

    private BigDecimal amount;

    private BigDecimal commission;

    private Urgency urgency;

    private Status status;

    private LocalDateTime createdAt;

    private LocalDateTime completedAt;
}
