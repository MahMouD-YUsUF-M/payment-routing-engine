package com.fawry.paymentroutingengine.dto.request;

import com.fawry.paymentroutingengine.constant.DayType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

/*
 * Request DTO for creating a new gateway
 *
 * Example JSON:
 * {
 *   "code": "gateway_4",
 *   "name": "Orange Money",
 *   "description": "Orange mobile wallet",
 *   "commissionFixed": 1.50,
 *   "commissionPercentage": 0.0120,
 *   "minTransaction": 20.00,
 *   "maxTransaction": 3000.00,
 *   "dailyLimit": 40000.00,
 *   "processingTime": "Instant",
 *   "availability": [
 *     {
 *       "dayOfWeek": "ALL",
 *       "startTime": null,
 *       "endTime": null,
 *       "is24_7": true
 *     }
 *   ]
 * }
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GatewayCreateRequest {


    @NotBlank(message = "Gateway name is required")
    @Size(max = 255, message = "Gateway name must not exceed 255 characters")
    private String name;

    @NotNull(message = "Commission fixed amount is required")
    @PositiveOrZero(message = "Commission fixed must be zero or positive")
    private BigDecimal commissionFixed;

    @NotNull(message = "Commission percentage is required")
    @DecimalMin(value = "0.0000", message = "Commission percentage must be zero or positive")
    @DecimalMax(value = "1.0000", message = "Commission percentage must not exceed 1 (100%)")
    private BigDecimal commissionPercentage;

    @NotNull(message = "Minimum transaction amount is required")
    @PositiveOrZero(message = "Minimum transaction must be zero or positive")
    private BigDecimal minTransaction;

    @PositiveOrZero(message = "Maximum transaction must be zero or positive")
    private BigDecimal maxTransaction;

    @NotNull(message = "Daily limit is required")
    @Positive(message = "Daily limit must be greater than 0")
    private BigDecimal dailyLimit;

    @NotNull(message = "processing time is required")
    @PositiveOrZero(message = "processing time must be zero or positive")
    private BigDecimal processingTime;

    @NotNull(message = "Please put his active status")
    private Boolean isActive;

    @Valid
    @NotEmpty(message = "At least one availability schedule is required")
    private List<AvailabilitySchedule> availability;


    /*
        DTO For availability schedule
     */

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
        public static class AvailabilitySchedule {

            @NotNull(message = "day is required")
            private DayType dayOfWeek;


            private LocalTime startTime;

            private LocalTime endTime;

            @NotNull(message = "is24_7 flag is required")
            private Boolean is24_7;
        }



}
