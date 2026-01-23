package com.fawry.paymentroutingengine.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Response DTO for transaction summary (for Angular dashboard)
 *
 * Example JSON:
 * {
 *   "billerCode": "BILL_12345",
 *   "period": {
 *     "startDate": "2025-01-01",
 *     "endDate": "2025-01-23"
 *   },
 *   "totalTransactions": 150,
 *   "totalAmount": 450000.00,
 *   "totalCommission": 8500.00,
 *   "byGateway": [
 *     {
 *       "gatewayCode": "gateway_1",
 *       "gatewayName": "Vodafone Cash",
 *       "transactionCount": 80,
 *       "totalAmount": 200000.00,
 *       "totalCommission": 5000.00,
 *       "averageCommission": 62.50
 *     }
 *   ],
 *   "byStatus": {
 *     "COMPLETED": 145,
 *     "FAILED": 3,
 *     "PENDING": 2
 *   }
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionSummaryResponse {

    private String billerCode;

    private Period period;

    private Integer totalTransactions;

    private BigDecimal totalAmount;

    private BigDecimal totalCommission;

    private List<GatewaySummary> byGateway;

    private Map<String, Integer> byStatus;

    /**
     * Nested DTO for period
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Period {
        private LocalDate startDate;
        private LocalDate endDate;
    }

    /**
     * Nested DTO for gateway summary
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GatewaySummary {
        private String gatewayCode;
        private String gatewayName;
        private Integer transactionCount;
        private BigDecimal totalAmount;
        private BigDecimal totalCommission;
        private BigDecimal averageCommission;
    }
}
