package com.hisaab_khata.hisaab_khata.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Locked API response contract for all endpoints.
 * Success: success=true, statusCode (OK|CREATED|...), message, data, meta
 * Error: success=false, statusCode (VALIDATION_FAILED|UNAUTHORIZED|...), errorCode, message, data=null, meta, errors
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private String statusCode;
    private String message;
    private T data;
    private Map<String, Object> meta;
    private String errorCode;
    private List<ValidationError> errors;

    public static <T> ApiResponse<T> success(String statusCode, String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .statusCode(statusCode)
                .message(message)
                .data(data)
                .meta(Map.of())
                .build();
    }

    public static <T> ApiResponse<T> created(String message, T data) {
        return success("CREATED", message, data);
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return success("OK", message, data);
    }

    public static <T> ApiResponse<T> error(String statusCode, String errorCode, String message, List<ValidationError> errors) {
        return ApiResponse.<T>builder()
                .success(false)
                .statusCode(statusCode)
                .errorCode(errorCode)
                .message(message)
                .data(null)
                .meta(Map.of())
                .errors(errors)
                .build();
    }

    public static <T> ApiResponse<T> error(String statusCode, String errorCode, String message) {
        return error(statusCode, errorCode, message, null);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationError {
        private String field;
        private String message;
    }
}
