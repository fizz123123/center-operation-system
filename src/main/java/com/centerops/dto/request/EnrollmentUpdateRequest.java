package com.centerops.dto.request;

import com.centerops.entity.EnrollmentStatus;
import jakarta.validation.constraints.NotNull;

public record EnrollmentUpdateRequest(@NotNull EnrollmentStatus status) {
}
