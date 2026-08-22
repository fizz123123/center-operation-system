package com.centerops.service;

import com.centerops.dto.request.CourseCreateRequest;
import com.centerops.dto.request.CourseUpdateRequest;
import com.centerops.dto.response.CoursePrerequisiteResponse;
import com.centerops.dto.response.CourseResponse;
import com.centerops.dto.response.PageResponse;

import java.util.List;

public interface CourseService {
    PageResponse<CourseResponse> getAll(int page);

    CourseResponse getById(Long id);

    CourseResponse create(CourseCreateRequest request);

    CourseResponse update(Long id, CourseUpdateRequest request);

    CoursePrerequisiteResponse addPrerequisite(Long courseId, Long prerequisiteId);

    List<String> getLearningPath();
}
