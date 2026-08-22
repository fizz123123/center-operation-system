package com.centerops.dto.response;

import com.centerops.entity.EnrollmentStatus;

import java.time.LocalDate;

public record EnrollmentResponse(
        Long id,
        Long personId,
        String personName,
        Long courseId,
        String courseCode,
        String courseName,
        EnrollmentStatus status,
        LocalDate startDate,
        LocalDate completeDate
) {
}
