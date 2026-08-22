package com.centerops.mapper;

import com.centerops.dto.response.AlertResponse;
import com.centerops.entity.Alert;
import org.springframework.stereotype.Component;

@Component
public class AlertMapper {

    public AlertResponse toResponse(Alert alert) {
        Long courseId = alert.getCourse() == null ? null : alert.getCourse().getId();
        String courseName = alert.getCourse() == null ? null : alert.getCourse().getName();

        return new AlertResponse(
                alert.getId(),
                alert.getPerson().getId(),
                alert.getPerson().getName(),
                courseId,
                courseName,
                alert.getPriority(),
                alert.getMessage(),
                alert.isResolved(),
                alert.getCreatedAt()
        );
    }
}
