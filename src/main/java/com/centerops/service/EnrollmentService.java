package com.centerops.service;

import com.centerops.dto.request.EnrollmentCreateRequest;
import com.centerops.dto.request.EnrollmentUpdateRequest;
import com.centerops.dto.response.CourseOptionResponse;
import com.centerops.dto.response.EnrollmentResponse;
import com.centerops.dto.response.PageResponse;

import java.util.List;

public interface EnrollmentService {
    PageResponse<EnrollmentResponse> getAll(int page, String sort, String direction);

    PageResponse<EnrollmentResponse> getByPersonId(Long personId, int page, String sort, String direction);

    List<CourseOptionResponse> getAvailableCourses(Long personId);

    EnrollmentResponse create(EnrollmentCreateRequest request);

    EnrollmentResponse updateStatus(Long id, EnrollmentUpdateRequest request);
}
