package com.centerops.analytics;

import com.centerops.entity.Alert;
import com.centerops.entity.Course;
import com.centerops.entity.Enrollment;
import com.centerops.entity.EnrollmentStatus;
import com.centerops.entity.Person;
import com.centerops.repository.AlertRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlertGeneratorTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 8, 24);

    @Mock
    private AlertRepository alertRepository;

    @Test
    void synchronizeShouldCreateLowPriorityAlertForNotStartedEnrollment() {
        AlertGenerator generator = new AlertGenerator(alertRepository);
        Enrollment enrollment = enrollment(EnrollmentStatus.NOT_STARTED, null);
        when(alertRepository.findFirstByPerson_IdAndCourse_IdAndMessageStartingWith(
                1L, 2L, AlertGenerator.AUTO_MESSAGE_PREFIX
        )).thenReturn(Optional.empty());

        generator.synchronize(enrollment, TODAY);

        ArgumentCaptor<Alert> captor = ArgumentCaptor.forClass(Alert.class);
        verify(alertRepository).save(captor.capture());
        Alert saved = captor.getValue();
        assertThat(saved.getPriority()).isEqualTo(1);
        assertThat(saved.getMessage()).isEqualTo("[AUTO] Java has not started");
        assertThat(saved.getPerson()).isSameAs(enrollment.getPerson());
        assertThat(saved.getCourse()).isSameAs(enrollment.getCourse());
        assertThat(saved.isResolved()).isFalse();
    }

    @Test
    void synchronizeShouldCreateMediumPriorityAlertAtThirtyDays() {
        AlertGenerator generator = new AlertGenerator(alertRepository);
        Enrollment enrollment = enrollment(EnrollmentStatus.IN_PROGRESS, TODAY.minusDays(30));
        when(alertRepository.findFirstByPerson_IdAndCourse_IdAndMessageStartingWith(
                1L, 2L, AlertGenerator.AUTO_MESSAGE_PREFIX
        )).thenReturn(Optional.empty());

        generator.synchronize(enrollment, TODAY);

        ArgumentCaptor<Alert> captor = ArgumentCaptor.forClass(Alert.class);
        verify(alertRepository).save(captor.capture());
        assertThat(captor.getValue().getPriority()).isEqualTo(2);
        assertThat(captor.getValue().getMessage()).contains("30 days");
    }

    @Test
    void synchronizeShouldUpdateExistingAlertToHighPriorityAtNinetyDays() {
        AlertGenerator generator = new AlertGenerator(alertRepository);
        Enrollment enrollment = enrollment(EnrollmentStatus.IN_PROGRESS, TODAY.minusDays(90));
        Alert existing = Alert.builder()
                .id(10L)
                .person(enrollment.getPerson())
                .course(enrollment.getCourse())
                .priority(2)
                .message("[AUTO] old message")
                .resolved(true)
                .build();
        when(alertRepository.findFirstByPerson_IdAndCourse_IdAndMessageStartingWith(
                1L, 2L, AlertGenerator.AUTO_MESSAGE_PREFIX
        )).thenReturn(Optional.of(existing));

        generator.synchronize(enrollment, TODAY);

        verify(alertRepository).save(existing);
        assertThat(existing.getPriority()).isEqualTo(3);
        assertThat(existing.getMessage()).contains("90 days");
        assertThat(existing.isResolved()).isFalse();
    }

    @Test
    void synchronizeShouldRemoveOldAlertWhenInProgressForLessThanThirtyDays() {
        AlertGenerator generator = new AlertGenerator(alertRepository);
        Enrollment enrollment = enrollment(EnrollmentStatus.IN_PROGRESS, TODAY.minusDays(29));
        Alert existing = Alert.builder().id(10L).build();
        when(alertRepository.findFirstByPerson_IdAndCourse_IdAndMessageStartingWith(
                1L, 2L, AlertGenerator.AUTO_MESSAGE_PREFIX
        )).thenReturn(Optional.of(existing));

        generator.synchronize(enrollment, TODAY);

        verify(alertRepository).delete(existing);
        verify(alertRepository, never()).save(existing);
    }

    @Test
    void synchronizeShouldRemoveAutomaticAlertForCompletedEnrollment() {
        AlertGenerator generator = new AlertGenerator(alertRepository);
        Enrollment enrollment = enrollment(EnrollmentStatus.COMPLETED, TODAY.minusDays(100));
        Alert existing = Alert.builder().id(10L).build();
        when(alertRepository.findFirstByPerson_IdAndCourse_IdAndMessageStartingWith(
                1L, 2L, AlertGenerator.AUTO_MESSAGE_PREFIX
        )).thenReturn(Optional.of(existing));

        generator.synchronize(enrollment, TODAY);

        verify(alertRepository).delete(existing);
        verify(alertRepository, never()).save(existing);
    }

    @Test
    void synchronizeShouldNotCreateAlertWhenInProgressStartDateIsMissing() {
        AlertGenerator generator = new AlertGenerator(alertRepository);
        Enrollment enrollment = enrollment(EnrollmentStatus.IN_PROGRESS, null);
        when(alertRepository.findFirstByPerson_IdAndCourse_IdAndMessageStartingWith(
                1L, 2L, AlertGenerator.AUTO_MESSAGE_PREFIX
        )).thenReturn(Optional.empty());

        generator.synchronize(enrollment, TODAY);

        verify(alertRepository, never()).save(org.mockito.ArgumentMatchers.any(Alert.class));
    }

    private Enrollment enrollment(EnrollmentStatus status, LocalDate startDate) {
        Person person = Person.builder().id(1L).name("Ada").build();
        Course course = Course.builder().id(2L).code("JAVA-001").name("Java").build();
        return Enrollment.builder()
                .id(3L)
                .person(person)
                .course(course)
                .status(status)
                .startDate(startDate)
                .build();
    }
}
