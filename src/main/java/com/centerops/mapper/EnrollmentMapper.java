package com.centerops.mapper;

import com.centerops.dto.response.EnrollmentResponse;
import com.centerops.entity.Enrollment;
import org.springframework.stereotype.Component;

@Component
public class EnrollmentMapper {

    public EnrollmentResponse toResponse(Enrollment enrollment) {
        return new EnrollmentResponse(
                enrollment.getId(),
                enrollment.getPerson().getId(),
                enrollment.getPerson().getName(),
                enrollment.getCourse().getId(),
                enrollment.getCourse().getCode(),
                enrollment.getCourse().getName(),
                enrollment.getStatus(),
                enrollment.getStartDate(),
                enrollment.getCompleteDate()
        );
    }
}
