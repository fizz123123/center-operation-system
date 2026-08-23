package com.centerops.dto.response;

public record CourseEdgeResponse(
        Long fromCourseId,
        Long toCourseId
) {
}
