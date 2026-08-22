package com.centerops.dto.response;

import java.time.LocalDateTime;

public record AlertResponse(
        Long id,
        Long personId,
        String personName,
        Long courseId,
        String courseName,
        Integer priority,
        String message,
        boolean resolved,
        LocalDateTime createdAt
) {
}
