package com.centerops.service;

import com.centerops.dto.request.EnrollmentCreateRequest;
import com.centerops.dto.request.EnrollmentUpdateRequest;
import com.centerops.dto.response.EnrollmentResponse;
import com.centerops.dto.response.PageResponse;

public interface EnrollmentService {
    PageResponse<EnrollmentResponse> getAll(int page, String sort, String direction);

    PageResponse<EnrollmentResponse> getByPersonId(Long personId, int page, String sort, String direction);

    EnrollmentResponse create(EnrollmentCreateRequest request);

    EnrollmentResponse updateStatus(Long id, EnrollmentUpdateRequest request);
}
