package com.centerops.service;

import com.centerops.dto.request.PersonCreateRequest;
import com.centerops.dto.request.PersonUpdateRequest;
import com.centerops.dto.response.PageResponse;
import com.centerops.dto.response.PersonResponse;

public interface PersonService {
    PageResponse<PersonResponse> getAll(int page);

    PersonResponse getById(Long id);

    PersonResponse create(PersonCreateRequest request);

    PersonResponse update(Long id, PersonUpdateRequest request);
}
