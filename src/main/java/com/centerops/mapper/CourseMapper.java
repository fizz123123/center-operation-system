package com.centerops.mapper;

import com.centerops.dto.request.CourseCreateRequest;
import com.centerops.dto.response.AvailablePrerequisiteResponse;
import com.centerops.dto.response.CourseOptionResponse;
import com.centerops.dto.response.CourseResponse;
import com.centerops.entity.Course;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CourseMapper {

    public Course toEntity(CourseCreateRequest request) {
        return Course.builder()
                .code(request.code().trim().toUpperCase())
                .name(request.name().trim())
                .description(request.description())
                .build();
    }

    public CourseResponse toResponse(Course course, List<Long> prerequisiteIds) {
        return new CourseResponse(
                course.getId(),
                course.getCode(),
                course.getName(),
                course.getDescription(),
                List.copyOf(prerequisiteIds)
        );
    }

    public CourseOptionResponse toOptionResponse(Course course, List<Long> prerequisiteIds) {
        return new CourseOptionResponse(
                course.getId(),
                course.getCode(),
                course.getName(),
                List.copyOf(prerequisiteIds)
        );
    }

    public AvailablePrerequisiteResponse toAvailablePrerequisiteResponse(Course course) {
        return new AvailablePrerequisiteResponse(
                course.getId(),
                course.getCode(),
                course.getName()
        );
    }
}
