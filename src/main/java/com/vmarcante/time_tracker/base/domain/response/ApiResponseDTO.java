package com.vmarcante.time_tracker.base.domain.response;

public record ApiResponseDTO<T>(boolean success, int status, T data) {

    public static <T> ApiResponseDTO<T> ok(int status, T data) {
        return new ApiResponseDTO<>(true, status, data);
    }

    public static <T> ApiResponseDTO<T> error(int status, T data) {
        return new ApiResponseDTO<>(false, status, data);
    }
}
