package com.centerops.dto.response;

public record DashboardResponse(
        long totalPeople,
        long totalCourses,
        long totalEnrollments,
        double completionRate
) {
}
