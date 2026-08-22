package com.centerops.service.impl;

import com.centerops.dto.response.DashboardResponse;
import com.centerops.entity.EnrollmentStatus;
import com.centerops.repository.CourseRepository;
import com.centerops.repository.EnrollmentRepository;
import com.centerops.repository.PersonRepository;
import com.centerops.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final PersonRepository personRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Override
    public DashboardResponse getSummary() {
        long totalEnrollments = enrollmentRepository.count();
        long completed = enrollmentRepository.countByStatus(EnrollmentStatus.COMPLETED);
        double completionRate = totalEnrollments == 0
                ? 0.0
                : Math.round((completed * 1000.0) / totalEnrollments) / 10.0;

        return new DashboardResponse(
                personRepository.count(),
                courseRepository.count(),
                totalEnrollments,
                completionRate
        );
    }
}
