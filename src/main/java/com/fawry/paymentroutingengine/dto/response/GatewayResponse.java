package com.fawry.paymentroutingengine.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fawry.paymentroutingengine.constant.DayType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Response DTO for gateway details
 *
 * Example JSON:
 * {
 *
 *   "code": "gateway_1",
 *   "name": "Vodafone Cash",
 *   "description": "Mobile wallet instant payments",
 *   "commissionFixed": 2.00,
 *   "commissionPercentage": 0.0150,
 *   "minTransaction": 10.00,
 *   "maxTransaction": 5000.00,
 *   "dailyLimit": 50000.00,
 *   "processingTime": "Instant",
 *   "isActive" : "True",
 *   "availability": [...],
 *   "createdAt": "2025-01-20T10:00:00",
 *   "updatedAt": "2025-01-23T15:30:00"
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GatewayResponse {


    private String code;

    private String name;


    private BigDecimal commissionFixed;

    private BigDecimal commissionPercentage;

    private BigDecimal minTransaction;

    private BigDecimal maxTransaction;

    private BigDecimal dailyLimit;

    private BigDecimal processingTime;

    private Boolean isActive;

    private List<AvailabilitySchedule> availability;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /**
     * Nested DTO for availability schedules
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AvailabilitySchedule {
        private DayType dayOfWeek;
        private LocalTime startTime;
        private LocalTime endTime;
        private Boolean is24_7;
    }
}
