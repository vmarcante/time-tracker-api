package com.vmarcante.time_tracker.base.interfaces.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.vmarcante.time_tracker.base.domain.response.ApiResponseDTO;

public class BaseResponseController {

    protected <T> ResponseEntity<ApiResponseDTO<T>> ok(T data) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponseDTO.ok(HttpStatus.OK.value(), data));
    }

    protected <T> ResponseEntity<ApiResponseDTO<T>> created(T data) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.ok(HttpStatus.CREATED.value(), data));
    }

    protected <T> ResponseEntity<ApiResponseDTO<T>> noContent() {
        return ResponseEntity.noContent().build();
    }
}
