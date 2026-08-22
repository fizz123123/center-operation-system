package com.centerops.dto.request;

import jakarta.validation.constraints.Size;

public record CourseUpdateRequest(
        @Size(min = 1, max = 50) String code,
        @Size(min = 1, max = 100) String name,
        String description
) {
}
