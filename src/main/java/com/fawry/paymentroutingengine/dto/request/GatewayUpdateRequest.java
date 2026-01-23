package com.fawry.paymentroutingengine.dto.request;

import jakarta.validation.Valid;
import com.fawry.paymentroutingengine.constent.DayType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

/**
 * UNIFIED Request DTO for updating ANY gateway property
 *
 * You can update:
 * - Commission (fixed + percentage)
 * - Limits (min, max, daily)
 * - Processing time
 * - Availability schedules
 * - All of the above at once!
 *
 * All fields are OPTIONAL - only send what you want to update
 *
 * Example 1 - Update commission only:
 * {
 *   "commissionFixed": 3.00,
 *   "commissionPercentage": 0.0180
 * }
 *
 * Example 2 - Update limits only:
 * {
 *   "minTransaction": 15.00,
 *   "maxTransaction": 6000.00,
 *   "dailyLimit": 60000.00
 * }
 *
 * Example 3 - Update everything at once:
 * {
 *   "name": "Vodafone Cash Updated",
 *   "description": "New description",
 *   "commissionFixed": 3.00,
 *   "commissionPercentage": 0.0180,
 *   "minTransaction": 15.00,
 *   "maxTransaction": 6000.00,
 *   "dailyLimit": 60000.00,
 *   "processingTime": "2 Hours",
 *   "availability": [
 *     {
 *       "dayOfWeek": "MON",
 *       "startTime": "08:00:00",
 *       "endTime": "20:00:00",
 *       "is24_7": false
 *     }
 *   ]
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GatewayUpdateRequest {

    @Size(max = 255, message = "Gateway name must not exceed 255 characters")
    private String name;

    @PositiveOrZero(message = "Commission fixed must be zero or positive")
    private BigDecimal commissionFixed;

    @DecimalMin(value = "0.0000", message = "Commission percentage must be zero or positive")
    @DecimalMax(value = "1.0000", message = "Commission percentage must not exceed 1 (100%)")
    private BigDecimal commissionPercentage;

    @PositiveOrZero(message = "Minimum transaction must be zero or positive")
    private BigDecimal minTransaction;

    @PositiveOrZero(message = "Maximum transaction must be zero or positive")
    private BigDecimal maxTransaction;

    @Positive(message = "Daily limit must be greater than 0")
    private BigDecimal dailyLimit;

    @PositiveOrZero(message = "processing time must be zero or positive")
    private BigDecimal processingTime;

    Boolean isActive;


    @Valid
    private List<GatewayCreateRequest.AvailabilitySchedule> availability;


    /*
        DTO For availability schedule
     */

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AvailabilitySchedule {

        private DayType dayOfWeek;

        private LocalTime startTime;

        private LocalTime endTime;

        private Boolean is24_7;
    }

    /**
     * Helper method to check if any field is being updated
     */
    public boolean hasUpdates() {
        return name != null ||
                commissionFixed != null ||
                commissionPercentage != null ||
                minTransaction != null ||
                maxTransaction != null ||
                dailyLimit != null ||
                processingTime != null ||
                isActive != null ||
                (availability != null && !availability.isEmpty());
    }


    public boolean hasCommissionUpdate() {
        return commissionFixed != null || commissionPercentage != null;
    }


    public boolean hasLimitsUpdate() {
        return minTransaction != null || maxTransaction != null || dailyLimit != null;
    }


    public boolean hasAvailabilityUpdate() {
        return availability != null && !availability.isEmpty();
    }

}
