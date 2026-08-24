package com.centerops.service;

import com.centerops.analytics.AlertGenerator;
import com.centerops.dto.request.EnrollmentCreateRequest;
import com.centerops.dto.request.EnrollmentUpdateRequest;
import com.centerops.dto.response.CourseOptionResponse;
import com.centerops.dto.response.EnrollmentResponse;
import com.centerops.entity.Course;
import com.centerops.entity.CoursePrerequisite;
import com.centerops.entity.Enrollment;
import com.centerops.entity.EnrollmentStatus;
import com.centerops.entity.Person;
import com.centerops.exception.BusinessConflictException;
import com.centerops.exception.DuplicateResourceException;
import com.centerops.exception.InvalidStateException;
import com.centerops.mapper.CourseMapper;
import com.centerops.mapper.EnrollmentMapper;
import com.centerops.repository.CoursePrerequisiteRepository;
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
import java.util.List;
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
    private CoursePrerequisiteRepository prerequisiteRepository;

    @Mock
    private CourseMapper courseMapper;

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

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(enrollmentRepository).findAll(pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getSort().getOrderFor("id").getDirection())
                .isEqualTo(Sort.Direction.ASC);
    }

    @Test
    void getAllShouldUseMergeSortBeforeTakingRequestedPage() {
        List<Enrollment> enrollments = java.util.stream.IntStream.iterate(12, value -> value - 1)
                .limit(12)
                .mapToObj(value -> enrollment(
                        (long) value,
                        String.valueOf((char) ('A' + value - 1))
                ))
                .toList();
        Enrollment courseK = enrollments.get(1);
        Enrollment courseL = enrollments.get(0);
        when(enrollmentRepository.findAll()).thenReturn(enrollments);
        when(enrollmentMapper.toResponse(courseK)).thenReturn(response(courseK));
        when(enrollmentMapper.toResponse(courseL)).thenReturn(response(courseL));

        var page = enrollmentService.getAll(1, "courseName", "asc");

        assertThat(page.content())
                .extracting(EnrollmentResponse::courseName)
                .containsExactly("K", "L");
        assertThat(page.totalElements()).isEqualTo(12);
        assertThat(page.totalPages()).isEqualTo(2);
        assertThat(page.first()).isFalse();
        assertThat(page.last()).isTrue();
        verify(enrollmentRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void getAllShouldRejectUnsupportedSortField() {
        assertThatThrownBy(() -> enrollmentService.getAll(0, "status", "asc"))
                .isInstanceOf(InvalidStateException.class)
                .hasMessageContaining("courseName");

        verify(enrollmentRepository, never()).findAll(any(Pageable.class));
        verify(enrollmentRepository, never()).findAll();
    }

    @Test
    void getAllShouldRejectUnsupportedDirectionBeforeQuerying() {
        assertThatThrownBy(() -> enrollmentService.getAll(0, "courseName", "sideways"))
                .isInstanceOf(InvalidStateException.class)
                .hasMessageContaining("asc or desc");

        verify(enrollmentRepository, never()).findAll(any(Pageable.class));
        verify(enrollmentRepository, never()).findAll();
    }

    @Test
    void getByPersonIdShouldUseIdAscendingWhenCourseNamesAreEqual() {
        Enrollment laterJava = enrollment(3L, "Java");
        Enrollment earlierJava = enrollment(1L, "Java");
        Enrollment database = enrollment(2L, "Database");
        when(personRepository.existsById(1L)).thenReturn(true);
        when(enrollmentRepository.findAllByPersonId(1L))
                .thenReturn(List.of(laterJava, database, earlierJava));
        when(enrollmentMapper.toResponse(laterJava)).thenReturn(response(laterJava));
        when(enrollmentMapper.toResponse(earlierJava)).thenReturn(response(earlierJava));
        when(enrollmentMapper.toResponse(database)).thenReturn(response(database));

        var page = enrollmentService.getByPersonId(1L, 0, "courseName", "desc");

        assertThat(page.content())
                .extracting(EnrollmentResponse::id)
                .containsExactly(1L, 3L, 2L);
        verify(enrollmentRepository, never())
                .findAllByPersonId(org.mockito.ArgumentMatchers.eq(1L), any(Pageable.class));
    }

    @Test
    void getAvailableCoursesShouldReturnOnlyUnlockedAndNotEnrolledCourses() {
        Course java = course(1L, "JAVA-001", "Java Basic");
        Course oop = course(2L, "JAVA-002", "Object Oriented Programming");
        Course spring = course(3L, "SPRING-001", "Spring Boot");
        Course git = course(4L, "GIT-001", "Git Collaboration");
        Enrollment completedJava = Enrollment.builder()
                .id(10L)
                .person(Person.builder().id(1L).name("Ada").build())
                .course(java)
                .status(EnrollmentStatus.COMPLETED)
                .build();
        CourseOptionResponse oopOption = new CourseOptionResponse(2L, "JAVA-002", oop.getName(), List.of(1L));
        CourseOptionResponse gitOption = new CourseOptionResponse(4L, "GIT-001", git.getName(), List.of());

        when(personRepository.existsById(1L)).thenReturn(true);
        when(courseRepository.findAll(any(Sort.class))).thenReturn(List.of(java, oop, spring, git));
        when(prerequisiteRepository.findAllByOrderByIdAsc()).thenReturn(List.of(
                prerequisite(oop, java),
                prerequisite(spring, oop)
        ));
        when(enrollmentRepository.findAllByPersonId(1L)).thenReturn(List.of(completedJava));
        when(courseMapper.toOptionResponse(oop, List.of(1L))).thenReturn(oopOption);
        when(courseMapper.toOptionResponse(git, List.of())).thenReturn(gitOption);

        assertThat(enrollmentService.getAvailableCourses(1L))
                .containsExactly(oopOption, gitOption);
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
    void createShouldRejectWhenTransitivePrerequisiteIsIncomplete() {
        EnrollmentCreateRequest request = new EnrollmentCreateRequest(1L, 3L);
        Person person = Person.builder().id(1L).name("Ada").build();
        Course java = course(1L, "JAVA-001", "Java Basic");
        Course oop = course(2L, "JAVA-002", "Object Oriented Programming");
        Course spring = course(3L, "SPRING-001", "Spring Boot");
        Enrollment completedOop = Enrollment.builder()
                .id(10L)
                .person(person)
                .course(oop)
                .status(EnrollmentStatus.COMPLETED)
                .build();

        when(enrollmentRepository.existsByPersonIdAndCourseId(1L, 3L)).thenReturn(false);
        when(personRepository.findById(1L)).thenReturn(Optional.of(person));
        when(courseRepository.findById(3L)).thenReturn(Optional.of(spring));
        when(courseRepository.findAll(any(Sort.class))).thenReturn(List.of(java, oop, spring));
        when(prerequisiteRepository.findAllByOrderByIdAsc()).thenReturn(List.of(
                prerequisite(oop, java),
                prerequisite(spring, oop)
        ));
        when(enrollmentRepository.findAllByPersonId(1L)).thenReturn(List.of(completedOop));

        assertThatThrownBy(() -> enrollmentService.create(request))
                .isInstanceOf(BusinessConflictException.class)
                .hasMessageContaining("JAVA-001");

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

    private Enrollment enrollment(Long id, String courseName) {
        Person person = Person.builder().id(1L).name("Ada").build();
        Course course = Course.builder()
                .id(id)
                .code("COURSE-" + id)
                .name(courseName)
                .build();
        return Enrollment.builder()
                .id(id)
                .person(person)
                .course(course)
                .status(EnrollmentStatus.NOT_STARTED)
                .build();
    }

    private Course course(Long id, String code, String name) {
        return Course.builder().id(id).code(code).name(name).build();
    }

    private CoursePrerequisite prerequisite(Course course, Course prerequisite) {
        return CoursePrerequisite.builder()
                .course(course)
                .prerequisite(prerequisite)
                .build();
    }
}
