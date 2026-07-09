package com.school.sis.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        List<FieldErrorResponse> errors,
        Instant timestamp
) {
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, null, Instant.now());
    }

    public static ApiResponse<Void> success(String message) {
        return new ApiResponse<>(true, message, null, null, Instant.now());
    }

    public static ApiResponse<Void> failure(String message) {
        return new ApiResponse<>(false, message, null, null, Instant.now());
    }

    public static ApiResponse<Void> validationFailure(String message, List<FieldErrorResponse> errors) {
        return new ApiResponse<>(false, message, null, errors, Instant.now());
    }
}
