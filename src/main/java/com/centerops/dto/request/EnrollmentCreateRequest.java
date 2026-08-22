package com.centerops.dto.request;

import jakarta.validation.constraints.NotNull;

public record EnrollmentCreateRequest(
        @NotNull Long personId,
        @NotNull Long courseId
) {
}
