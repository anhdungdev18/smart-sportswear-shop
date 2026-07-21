package com.dunghaiquyen.ecommerce.common.response;

import java.util.List;

/**
 * Single response envelope for every controller, per THIET_KE_SAN_PHAM.md 10.3/10.4.
 */
public record ApiResponse<T>(boolean success, String message, T data, Object meta, List<ApiFieldError> errors) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, "OK", data, null, null);
    }

    public static <T> ApiResponse<T> ok(T data, Object meta) {
        return new ApiResponse<>(true, "OK", data, meta, null);
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, message, data, null, null);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null, null, null);
    }

    public static <T> ApiResponse<T> error(String message, List<ApiFieldError> errors) {
        return new ApiResponse<>(false, message, null, null, errors);
    }
}
