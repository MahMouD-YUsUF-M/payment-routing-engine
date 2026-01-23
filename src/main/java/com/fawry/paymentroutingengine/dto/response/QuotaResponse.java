package com.fawry.paymentroutingengine.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Response DTO for quota information
 *
 * Example JSON:
 * {
 *   "billerCode": "BILL_12345",
 *   "quotaDate": "2025-01-23",
 *   "quotas": [
 *     {
 *       "gatewayCode": "gateway_1",
 *       "gatewayName": "Vodafone Cash",
 *       "dailyLimit": 50000.00,
 *       "usedAmount": 35000.00,
 *       "remainingAmount": 15000.00,
 *       "transactionCount": 25,
 *       "utilizationPercentage": 70.0
 *     }
 *   ]
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuotaResponse {

    private String billerCode;

    private LocalDate quotaDate;

    private List<GatewayQuota> quotas;

    /**
     * Nested DTO for individual gateway quota
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GatewayQuota {

        private String gatewayCode;

        private String gatewayName;

        private BigDecimal dailyLimit;

        private BigDecimal usedAmount;

        private BigDecimal remainingAmount;

        private Integer transactionCount;

        private Double utilizationPercentage;
    }
}