package com.centerops.mapper;

import com.centerops.dto.response.CoursePrerequisiteResponse;
import com.centerops.entity.CoursePrerequisite;
import org.springframework.stereotype.Component;

@Component
public class CoursePrerequisiteMapper {

    public CoursePrerequisiteResponse toResponse(CoursePrerequisite relation) {
        return new CoursePrerequisiteResponse(
                relation.getId(),
                relation.getCourse().getId(),
                relation.getCourse().getName(),
                relation.getPrerequisite().getId(),
                relation.getPrerequisite().getName()
        );
    }
}
