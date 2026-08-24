package com.centerops.service.impl;

import com.centerops.analytics.AlertGenerator;
import com.centerops.algorithm.DepthFirstSearch;
import com.centerops.algorithm.MergeSort;
import com.centerops.datastructure.CourseGraph;
import com.centerops.dto.request.EnrollmentCreateRequest;
import com.centerops.dto.request.EnrollmentUpdateRequest;
import com.centerops.dto.response.CourseOptionResponse;
import com.centerops.dto.response.EnrollmentResponse;
import com.centerops.dto.response.PageResponse;
import com.centerops.entity.Course;
import com.centerops.entity.CoursePrerequisite;
import com.centerops.entity.Enrollment;
import com.centerops.entity.EnrollmentStatus;
import com.centerops.entity.Person;
import com.centerops.exception.BusinessConflictException;
import com.centerops.exception.DuplicateResourceException;
import com.centerops.exception.InvalidStateException;
import com.centerops.exception.ResourceNotFoundException;
import com.centerops.mapper.CourseMapper;
import com.centerops.mapper.EnrollmentMapper;
import com.centerops.repository.CoursePrerequisiteRepository;
import com.centerops.repository.CourseRepository;
import com.centerops.repository.EnrollmentRepository;
import com.centerops.repository.PersonRepository;
import com.centerops.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final PersonRepository personRepository;
    private final CourseRepository courseRepository;
    private final CoursePrerequisiteRepository prerequisiteRepository;
    private final CourseMapper courseMapper;
    private final EnrollmentMapper enrollmentMapper;
    private final AlertGenerator alertGenerator;

    @Override
    public PageResponse<EnrollmentResponse> getAll(int page, String sort, String direction) {
        validatePage(page);
        Sort.Direction sortDirection = validateSortOptions(sort, direction);
        if (isCourseNameSort(sort)) {
            return sortByCourseNameAndCreatePage(
                    enrollmentRepository.findAll(),
                    page,
                    sortDirection
            );
        }
        return PageResponse.from(
                enrollmentRepository.findAll(PageRequest.of(
                                page,
                                PageResponse.DEFAULT_SIZE,
                                Sort.by(Sort.Direction.ASC, "id")
                        ))
                        .map(enrollmentMapper::toResponse)
        );
    }

    @Override
    public PageResponse<EnrollmentResponse> getByPersonId(
            Long personId,
            int page,
            String sort,
            String direction
    ) {
        validatePage(page);
        if (!personRepository.existsById(personId)) {
            throw new ResourceNotFoundException("Person", personId);
        }
        Sort.Direction sortDirection = validateSortOptions(sort, direction);
        if (isCourseNameSort(sort)) {
            return sortByCourseNameAndCreatePage(
                    enrollmentRepository.findAllByPersonId(personId),
                    page,
                    sortDirection
            );
        }
        return PageResponse.from(
                enrollmentRepository.findAllByPersonId(
                                personId,
                                PageRequest.of(
                                        page,
                                        PageResponse.DEFAULT_SIZE,
                                        Sort.by(Sort.Direction.ASC, "id")
                                )
                        )
                        .map(enrollmentMapper::toResponse)
        );
    }

    @Override
    public List<CourseOptionResponse> getAvailableCourses(Long personId) {
        if (!personRepository.existsById(personId)) {
            throw new ResourceNotFoundException("Person", personId);
        }

        CourseEligibilityContext context = loadEligibilityContext(personId);
        return context.courses().stream()
                .filter(course -> !context.enrolledCourseIds().contains(course.getId()))
                .filter(course -> findIncompletePrerequisites(course, context).isEmpty())
                .map(course -> courseMapper.toOptionResponse(
                        course,
                        context.prerequisiteIds().getOrDefault(course.getId(), List.of())
                ))
                .toList();
    }

    @Override
    @Transactional
    public EnrollmentResponse create(EnrollmentCreateRequest request) {
        if (enrollmentRepository.existsByPersonIdAndCourseId(request.personId(), request.courseId())) {
            throw new DuplicateResourceException("The person is already enrolled in this course");
        }
        Person person = personRepository.findById(request.personId())
                .orElseThrow(() -> new ResourceNotFoundException("Person", request.personId()));
        Course course = courseRepository.findById(request.courseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course", request.courseId()));

        List<Course> incompletePrerequisites = findIncompletePrerequisites(
                course,
                loadEligibilityContext(request.personId())
        );
        if (!incompletePrerequisites.isEmpty()) {
            String missingCodes = incompletePrerequisites.stream()
                    .map(Course::getCode)
                    .sorted()
                    .reduce((left, right) -> left + ", " + right)
                    .orElse("");
            throw new BusinessConflictException(
                    "Required prerequisite courses must be completed before enrollment: " + missingCodes
            );
        }

        Enrollment enrollment = Enrollment.builder()
                .person(person)
                .course(course)
                .status(EnrollmentStatus.NOT_STARTED)
                .build();
        Enrollment saved = enrollmentRepository.save(enrollment);
        alertGenerator.synchronize(saved);
        return enrollmentMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public EnrollmentResponse updateStatus(Long id, EnrollmentUpdateRequest request) {
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", id));
        EnrollmentStatus current = enrollment.getStatus();
        EnrollmentStatus target = request.status();

        if (current == EnrollmentStatus.COMPLETED) {
            throw new BusinessConflictException("Completed enrollment status cannot be modified");
        }
        if (target.ordinal() < current.ordinal()) {
            throw new BusinessConflictException("Enrollment status cannot move backwards");
        }
        if (target != EnrollmentStatus.NOT_STARTED && enrollment.getStartDate() == null) {
            enrollment.setStartDate(LocalDate.now());
        }
        enrollment.setCompleteDate(target == EnrollmentStatus.COMPLETED ? LocalDate.now() : null);
        enrollment.setStatus(target);

        Enrollment saved = enrollmentRepository.save(enrollment);
        alertGenerator.synchronize(saved);
        return enrollmentMapper.toResponse(saved);
    }

    private void validatePage(int page) {
        if (page < 0) {
            throw new InvalidStateException("Page index must be zero or greater");
        }
    }

    private Sort.Direction validateSortOptions(String sort, String direction) {
        Sort.Direction sortDirection;
        try {
            sortDirection = Sort.Direction.fromString(direction);
        } catch (IllegalArgumentException exception) {
            throw new InvalidStateException("direction must be asc or desc");
        }

        if (sort != null && !sort.isBlank() && !isCourseNameSort(sort)) {
            throw new InvalidStateException("sort must be courseName");
        }
        return sortDirection;
    }

    private boolean isCourseNameSort(String sort) {
        return "courseName".equals(sort);
    }

    /**
     * 建立學員課程資格判斷所需的課程圖與修課狀態快照。
     */
    private CourseEligibilityContext loadEligibilityContext(Long personId) {
        List<Course> courses = courseRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
        List<CoursePrerequisite> relations = prerequisiteRepository.findAllByOrderByIdAsc();
        List<Enrollment> enrollments = enrollmentRepository.findAllByPersonId(personId);

        CourseGraph<Long> graph = new CourseGraph<>();
        courses.forEach(course -> graph.addVertex(course.getId()));

        Map<Long, List<Long>> prerequisiteIds = new HashMap<>();
        for (CoursePrerequisite relation : relations) {
            Long courseId = relation.getCourse().getId();
            Long prerequisiteId = relation.getPrerequisite().getId();
            graph.addEdge(prerequisiteId, courseId);
            prerequisiteIds.computeIfAbsent(courseId, ignored -> new ArrayList<>()).add(prerequisiteId);
        }

        Set<Long> enrolledCourseIds = new HashSet<>();
        Set<Long> completedCourseIds = new HashSet<>();
        for (Enrollment enrollment : enrollments) {
            Long courseId = enrollment.getCourse().getId();
            enrolledCourseIds.add(courseId);
            if (enrollment.getStatus() == EnrollmentStatus.COMPLETED) {
                completedCourseIds.add(courseId);
            }
        }

        return new CourseEligibilityContext(
                courses,
                graph,
                enrolledCourseIds,
                completedCourseIds,
                prerequisiteIds
        );
    }

    /**
     * 使用 DFS 找出所有能沿先修關係到達目標課程、但學員尚未完成的課程。
     */
    private List<Course> findIncompletePrerequisites(Course targetCourse, CourseEligibilityContext context) {
        return context.courses().stream()
                .filter(course -> !course.getId().equals(targetCourse.getId()))
                .filter(course -> DepthFirstSearch.isReachable(
                        context.graph(),
                        course.getId(),
                        targetCourse.getId()
                ))
                .filter(course -> !context.completedCourseIds().contains(course.getId()))
                .toList();
    }

    private PageResponse<EnrollmentResponse> sortByCourseNameAndCreatePage(
            List<Enrollment> enrollments,
            int page,
            Sort.Direction direction
    ) {
        Comparator<String> nameOrder = direction == Sort.Direction.ASC
                ? String.CASE_INSENSITIVE_ORDER
                : String.CASE_INSENSITIVE_ORDER.reversed();
        Comparator<Enrollment> enrollmentOrder = Comparator
                .comparing(
                        (Enrollment enrollment) -> enrollment.getCourse().getName(),
                        nameOrder
                )
                .thenComparing(Enrollment::getId);
        List<Enrollment> sorted = MergeSort.sort(enrollments, enrollmentOrder);

        int size = PageResponse.DEFAULT_SIZE;
        int totalElements = sorted.size();
        int totalPages = (totalElements + size - 1) / size;
        long offset = (long) page * size;
        int fromIndex = (int) Math.min(offset, totalElements);
        int toIndex = Math.min(fromIndex + size, totalElements);
        List<EnrollmentResponse> content = sorted.subList(fromIndex, toIndex).stream()
                .map(enrollmentMapper::toResponse)
                .toList();

        return new PageResponse<>(
                content,
                page,
                size,
                totalElements,
                totalPages,
                page == 0,
                totalPages == 0 || page >= totalPages - 1
        );
    }

    private record CourseEligibilityContext(
            List<Course> courses,
            CourseGraph<Long> graph,
            Set<Long> enrolledCourseIds,
            Set<Long> completedCourseIds,
            Map<Long, List<Long>> prerequisiteIds
    ) {
    }
}
