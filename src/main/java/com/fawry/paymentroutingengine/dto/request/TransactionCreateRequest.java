package com.fawry.paymentroutingengine.dto.request;

import com.fawry.paymentroutingengine.constent.Urgency;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for creating/logging a transaction
 *
 * Example JSON:
 * {
*    "biilerCode": "biller_1",
 *   "gatewayCode": "gateway_1",
 *   "amount": 1000.00,
 *   "urgency": "INSTANT"
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionCreateRequest {

    @NotBlank(message = "Gateway code is required")
    private String gateWayCode;

    @NotBlank(message = "Gateway code is required")
    private String billerCode;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotNull(message = "commission is required")
    @PositiveOrZero(message = "commission must be greater than or equal 0")
    private BigDecimal commission;

    @NotNull(message = "Urgency is required")
    private Urgency urgency;
}