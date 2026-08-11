package com.vmarcante.time_tracker.core.interfaces.health.controllers;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vmarcante.time_tracker.base.domain.response.ApiResponseDTO;
import com.vmarcante.time_tracker.base.interfaces.controllers.BaseResponseController;
import com.vmarcante.time_tracker.core.application.health.dto.output.HealthStatusOutputDTO;
import com.vmarcante.time_tracker.core.application.health.in.HealthCheckUseCase;
import com.vmarcante.time_tracker.core.domain.user.auth.annotation.AuthSecure;
import com.vmarcante.time_tracker.core.domain.user.enums.UserRoleType;

@RestController
@RequestMapping("/health")
public class HealthController extends BaseResponseController {

    private final HealthCheckUseCase healthCheckUseCase;

    public HealthController(HealthCheckUseCase healthCheckUseCase) {
        this.healthCheckUseCase = healthCheckUseCase;
    }

    @GetMapping
    public ResponseEntity<ApiResponseDTO<Map<String, String>>> health() {
        return ok(Map.of("status", "UP"));
    }

    @GetMapping("/details")
    @AuthSecure(acceptedRoles = { UserRoleType.ADMINISTRATOR }, appKeyAllowed = true)
    public ResponseEntity<ApiResponseDTO<HealthStatusOutputDTO>> healthDetails() {
        return ok(healthCheckUseCase.execute());
    }
}
