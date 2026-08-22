package com.centerops.service;

import com.centerops.dto.response.DashboardResponse;
import com.centerops.entity.EnrollmentStatus;
import com.centerops.repository.CourseRepository;
import com.centerops.repository.EnrollmentRepository;
import com.centerops.repository.PersonRepository;
import com.centerops.service.impl.DashboardServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock
    private PersonRepository personRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    @Test
    void getSummaryShouldCalculateCompletionRateToOneDecimalPlace() {
        when(personRepository.count()).thenReturn(10L);
        when(courseRepository.count()).thenReturn(5L);
        when(enrollmentRepository.count()).thenReturn(3L);
        when(enrollmentRepository.countByStatus(EnrollmentStatus.COMPLETED)).thenReturn(2L);

        DashboardResponse response = dashboardService.getSummary();

        assertThat(response.totalPeople()).isEqualTo(10L);
        assertThat(response.totalCourses()).isEqualTo(5L);
        assertThat(response.totalEnrollments()).isEqualTo(3L);
        assertThat(response.completionRate()).isEqualTo(66.7);
    }

    @Test
    void getSummaryShouldReturnZeroRateWhenThereAreNoEnrollments() {
        when(personRepository.count()).thenReturn(2L);
        when(courseRepository.count()).thenReturn(4L);
        when(enrollmentRepository.count()).thenReturn(0L);
        when(enrollmentRepository.countByStatus(EnrollmentStatus.COMPLETED)).thenReturn(0L);

        DashboardResponse response = dashboardService.getSummary();

        assertThat(response.totalEnrollments()).isZero();
        assertThat(response.completionRate()).isZero();
    }
}
