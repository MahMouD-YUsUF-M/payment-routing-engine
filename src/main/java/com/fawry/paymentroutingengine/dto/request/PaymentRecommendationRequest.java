package com.fawry.paymentroutingengine.dto.request;

import com.fawry.paymentroutingengine.constent.Urgency;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for payment gateway recommendation
 *
 * Example JSON:
 * {
 *   "billerCode": "BILL_12345",
 *   "amount": 1000.00,
 *   "urgency": "INSTANT"
 * }
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRecommendationRequest {

    @NotBlank(message = "biller code is required")
    private String billerCode;

    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.1" , message = "min amount is .01")
    @Positive(message = "amount required be positive")
    private BigDecimal amount;

    @NotNull(message = "urgency is required ")
    private Urgency urgency;
}
