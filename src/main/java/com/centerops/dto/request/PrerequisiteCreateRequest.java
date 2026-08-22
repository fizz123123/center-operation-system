package com.centerops.dto.request;

import jakarta.validation.constraints.NotNull;

public record PrerequisiteCreateRequest(@NotNull Long prerequisiteId) {
}
