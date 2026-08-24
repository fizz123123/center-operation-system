package com.centerops.dto.response;

import java.util.List;

public record CourseOptionResponse(
        Long id,
        String code,
        String name,
        List<Long> prerequisiteIds
) {
}
