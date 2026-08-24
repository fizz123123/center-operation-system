package com.centerops.service;

import com.centerops.dto.request.PersonCreateRequest;
import com.centerops.dto.request.PersonUpdateRequest;
import com.centerops.dto.response.PageResponse;
import com.centerops.dto.response.PersonResponse;
import com.centerops.dto.response.PersonStatisticsResponse;
import com.centerops.entity.PersonStatus;

public interface PersonService {
    PageResponse<PersonResponse> getAll(int page, String search, PersonStatus status);

    PersonStatisticsResponse getStatistics();

    PersonResponse getById(Long id);

    PersonResponse create(PersonCreateRequest request);

    PersonResponse update(Long id, PersonUpdateRequest request);
}
