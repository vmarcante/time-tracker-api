package com.vmarcante.time_tracker.core.application.person.dto;

public record UpdatePersonProfileInputDTO(
        String name,
        String phone,
        Integer age) {
}
