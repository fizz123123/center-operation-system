package com.centerops.dto.response;

public record CoursePrerequisiteResponse(
        Long id,
        Long courseId,
        String courseName,
        Long prerequisiteId,
        String prerequisiteName
) {
}
