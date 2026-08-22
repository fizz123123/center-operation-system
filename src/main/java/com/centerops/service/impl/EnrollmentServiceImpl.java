package com.centerops.service.impl;

import com.centerops.dto.request.EnrollmentCreateRequest;
import com.centerops.dto.request.EnrollmentUpdateRequest;
import com.centerops.dto.response.EnrollmentResponse;
import com.centerops.dto.response.PageResponse;
import com.centerops.entity.Course;
import com.centerops.entity.Enrollment;
import com.centerops.entity.EnrollmentStatus;
import com.centerops.entity.Person;
import com.centerops.exception.DuplicateResourceException;
import com.centerops.exception.InvalidStateException;
import com.centerops.exception.ResourceNotFoundException;
import com.centerops.mapper.EnrollmentMapper;
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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final PersonRepository personRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentMapper enrollmentMapper;

    @Override
    public PageResponse<EnrollmentResponse> getAll(int page) {
        validatePage(page);
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
    public PageResponse<EnrollmentResponse> getByPersonId(Long personId, int page) {
        validatePage(page);
        if (!personRepository.existsById(personId)) {
            throw new ResourceNotFoundException("Person", personId);
        }
        return PageResponse.from(
                enrollmentRepository.findAllByPersonIdOrderByIdAsc(
                                personId,
                                PageRequest.of(page, PageResponse.DEFAULT_SIZE)
                        )
                        .map(enrollmentMapper::toResponse)
        );
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

        Enrollment enrollment = Enrollment.builder()
                .person(person)
                .course(course)
                .status(EnrollmentStatus.NOT_STARTED)
                .build();
        return enrollmentMapper.toResponse(enrollmentRepository.save(enrollment));
    }

    @Override
    @Transactional
    public EnrollmentResponse updateStatus(Long id, EnrollmentUpdateRequest request) {
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", id));
        EnrollmentStatus current = enrollment.getStatus();
        EnrollmentStatus target = request.status();

        if (target.ordinal() < current.ordinal()) {
            throw new InvalidStateException("Enrollment status cannot move backwards");
        }
        if (target != EnrollmentStatus.NOT_STARTED && enrollment.getStartDate() == null) {
            enrollment.setStartDate(LocalDate.now());
        }
        enrollment.setCompleteDate(target == EnrollmentStatus.COMPLETED ? LocalDate.now() : null);
        enrollment.setStatus(target);

        return enrollmentMapper.toResponse(enrollmentRepository.save(enrollment));
    }

    private void validatePage(int page) {
        if (page < 0) {
            throw new InvalidStateException("Page index must be zero or greater");
        }
    }
}
