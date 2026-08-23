package com.centerops.dto.response;

public record PersonStatisticsResponse(
        long total,
        long active,
        long inactive
) {
}
