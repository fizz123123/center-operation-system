package com.centerops.dto.response;

import java.util.List;

public record CourseResponse(
        Long id,
        String code,
        String name,
        String description,
        List<Long> prerequisiteIds
) {
}
