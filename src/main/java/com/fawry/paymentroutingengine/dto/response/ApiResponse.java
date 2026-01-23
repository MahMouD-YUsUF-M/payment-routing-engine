package com.fawry.paymentroutingengine.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Generic API response wrapper
 * Used for success/error responses
 *
 * Success Example:
 * {
 *   "success": true,
 *   "message": "Gateway created successfully",
 *   "data": { ... gateway object ... },
 *   "timestamp": "2025-01-23T14:30:00"
 * }
 *
 * Error Example:
 * {
 *   "success": false,
 *   "message": "Gateway not found",
 *   "error": "GATEWAY_NOT_FOUND",
 *   "timestamp": "2025-01-23T14:30:00"
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private Boolean success;

    private String message;

    private T data;

    private String error;

    private LocalDateTime timestamp = LocalDateTime.now();

    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();

    }

    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> error(String message, String errorCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .error(errorCode)
                .timestamp(LocalDateTime.now())
                .build();
    }


    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }


}
