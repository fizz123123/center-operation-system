package com.centerops.service;

import com.centerops.analytics.AlertGenerator;
import com.centerops.dto.request.EnrollmentCreateRequest;
import com.centerops.dto.request.EnrollmentUpdateRequest;
import com.centerops.dto.response.EnrollmentResponse;
import com.centerops.entity.Course;
import com.centerops.entity.Enrollment;
import com.centerops.entity.EnrollmentStatus;
import com.centerops.entity.Person;
import com.centerops.exception.BusinessConflictException;
import com.centerops.exception.DuplicateResourceException;
import com.centerops.exception.InvalidStateException;
import com.centerops.mapper.EnrollmentMapper;
import com.centerops.repository.CourseRepository;
import com.centerops.repository.EnrollmentRepository;
import com.centerops.repository.PersonRepository;
import com.centerops.service.impl.EnrollmentServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceImplTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private PersonRepository personRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private EnrollmentMapper enrollmentMapper;

    @Mock
    private AlertGenerator alertGenerator;

    @InjectMocks
    private EnrollmentServiceImpl enrollmentService;

    @Test
    void getAllShouldReturnFixedTenItemPage() {
        Person person = Person.builder().id(1L).name("Ada").build();
        Course course = Course.builder().id(2L).code("JAVA-001").name("Java").build();
        Enrollment enrollment = Enrollment.builder()
                .id(10L)
                .person(person)
                .course(course)
                .status(EnrollmentStatus.NOT_STARTED)
                .build();
        EnrollmentResponse mapped = response(enrollment);
        when(enrollmentRepository.findAll(any(Pageable.class)))
                .thenAnswer(invocation -> new PageImpl<>(
                        java.util.List.of(enrollment),
                        invocation.getArgument(0),
                        25
                ));
        when(enrollmentMapper.toResponse(enrollment)).thenReturn(mapped);

        var page = enrollmentService.getAll(1, null, "asc");

        assertThat(page.content()).containsExactly(mapped);
        assertThat(page.page()).isEqualTo(1);
        assertThat(page.size()).isEqualTo(10);
        assertThat(page.totalElements()).isEqualTo(25);
        assertThat(page.last()).isFalse();
    }

    @Test
    void getAllShouldSortByCourseNameDescendingBeforePaging() {
        when(enrollmentRepository.findAll(any(Pageable.class)))
                .thenAnswer(invocation -> new PageImpl<>(
                        java.util.List.of(),
                        invocation.getArgument(0),
                        0
                ));

        enrollmentService.getAll(0, "courseName", "desc");

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(enrollmentRepository).findAll(captor.capture());
        assertThat(captor.getValue().getSort().getOrderFor("course.name").getDirection())
                .isEqualTo(Sort.Direction.DESC);
        assertThat(captor.getValue().getSort().getOrderFor("id").getDirection())
                .isEqualTo(Sort.Direction.ASC);
    }

    @Test
    void getAllShouldRejectUnsupportedSortField() {
        assertThatThrownBy(() -> enrollmentService.getAll(0, "status", "asc"))
                .isInstanceOf(InvalidStateException.class)
                .hasMessageContaining("courseName");

        verify(enrollmentRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void getByPersonIdShouldUseRequestedCourseNameSort() {
        when(personRepository.existsById(1L)).thenReturn(true);
        when(enrollmentRepository.findAllByPersonId(any(Long.class), any(Pageable.class)))
                .thenAnswer(invocation -> new PageImpl<>(
                        java.util.List.of(),
                        invocation.getArgument(1),
                        0
                ));

        enrollmentService.getByPersonId(1L, 0, "courseName", "asc");

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(enrollmentRepository).findAllByPersonId(org.mockito.ArgumentMatchers.eq(1L), captor.capture());
        assertThat(captor.getValue().getSort().getOrderFor("course.name").getDirection())
                .isEqualTo(Sort.Direction.ASC);
    }

    @Test
    void createShouldEnrollPersonWithNotStartedStatus() {
        EnrollmentCreateRequest request = new EnrollmentCreateRequest(1L, 2L);
        Person person = Person.builder().id(1L).name("Ada").build();
        Course course = Course.builder().id(2L).code("JAVA-001").name("Java").build();
        Enrollment saved = Enrollment.builder()
                .id(10L)
                .person(person)
                .course(course)
                .status(EnrollmentStatus.NOT_STARTED)
                .build();
        EnrollmentResponse expected = response(saved);

        when(enrollmentRepository.existsByPersonIdAndCourseId(1L, 2L)).thenReturn(false);
        when(personRepository.findById(1L)).thenReturn(Optional.of(person));
        when(courseRepository.findById(2L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(saved);
        when(enrollmentMapper.toResponse(saved)).thenReturn(expected);

        assertThat(enrollmentService.create(request)).isEqualTo(expected);

        ArgumentCaptor<Enrollment> captor = ArgumentCaptor.forClass(Enrollment.class);
        verify(enrollmentRepository).save(captor.capture());
        verify(alertGenerator).synchronize(saved);
        assertThat(captor.getValue().getStatus()).isEqualTo(EnrollmentStatus.NOT_STARTED);
        assertThat(captor.getValue().getPerson()).isSameAs(person);
        assertThat(captor.getValue().getCourse()).isSameAs(course);
    }

    @Test
    void createShouldRejectDuplicateEnrollment() {
        EnrollmentCreateRequest request = new EnrollmentCreateRequest(1L, 2L);
        when(enrollmentRepository.existsByPersonIdAndCourseId(1L, 2L)).thenReturn(true);

        assertThatThrownBy(() -> enrollmentService.create(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already enrolled");

        verify(enrollmentRepository, never()).save(any(Enrollment.class));
    }

    @Test
    void updateStatusShouldSetDatesWhenEnrollmentIsCompleted() {
        Person person = Person.builder().id(1L).name("Ada").build();
        Course course = Course.builder().id(2L).code("JAVA-001").name("Java").build();
        Enrollment enrollment = Enrollment.builder()
                .id(10L)
                .person(person)
                .course(course)
                .status(EnrollmentStatus.NOT_STARTED)
                .build();
        EnrollmentUpdateRequest request = new EnrollmentUpdateRequest(EnrollmentStatus.COMPLETED);

        when(enrollmentRepository.findById(10L)).thenReturn(Optional.of(enrollment));
        when(enrollmentRepository.save(enrollment)).thenReturn(enrollment);
        when(enrollmentMapper.toResponse(enrollment)).thenAnswer(ignored -> response(enrollment));

        EnrollmentResponse actual = enrollmentService.updateStatus(10L, request);

        assertThat(actual.status()).isEqualTo(EnrollmentStatus.COMPLETED);
        assertThat(actual.startDate()).isEqualTo(LocalDate.now());
        assertThat(actual.completeDate()).isEqualTo(LocalDate.now());
        verify(alertGenerator).synchronize(enrollment);
    }

    @Test
    void updateStatusShouldRejectBackwardTransition() {
        Enrollment enrollment = Enrollment.builder()
                .id(10L)
                .status(EnrollmentStatus.IN_PROGRESS)
                .build();
        when(enrollmentRepository.findById(10L)).thenReturn(Optional.of(enrollment));

        EnrollmentUpdateRequest request = new EnrollmentUpdateRequest(EnrollmentStatus.NOT_STARTED);

        assertThatThrownBy(() -> enrollmentService.updateStatus(10L, request))
                .isInstanceOf(BusinessConflictException.class)
                .hasMessageContaining("backwards");

        verify(enrollmentRepository, never()).save(any(Enrollment.class));
    }

    @Test
    void updateStatusShouldRejectAnyChangeAfterCompletion() {
        Enrollment enrollment = Enrollment.builder()
                .id(10L)
                .status(EnrollmentStatus.COMPLETED)
                .build();
        when(enrollmentRepository.findById(10L)).thenReturn(Optional.of(enrollment));

        EnrollmentUpdateRequest request = new EnrollmentUpdateRequest(EnrollmentStatus.COMPLETED);

        assertThatThrownBy(() -> enrollmentService.updateStatus(10L, request))
                .isInstanceOf(BusinessConflictException.class)
                .hasMessageContaining("cannot be modified");

        verify(enrollmentRepository, never()).save(any(Enrollment.class));
    }

    private EnrollmentResponse response(Enrollment enrollment) {
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
