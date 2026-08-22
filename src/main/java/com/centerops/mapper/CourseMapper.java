package com.centerops.mapper;

import com.centerops.dto.request.CourseCreateRequest;
import com.centerops.dto.response.CourseResponse;
import com.centerops.entity.Course;
import org.springframework.stereotype.Component;

@Component
public class CourseMapper {

    public Course toEntity(CourseCreateRequest request) {
        return Course.builder()
                .code(request.code().trim().toUpperCase())
                .name(request.name().trim())
                .description(request.description())
                .build();
    }

    public CourseResponse toResponse(Course course) {
        return new CourseResponse(
                course.getId(),
                course.getCode(),
                course.getName(),
                course.getDescription()
        );
    }
}
