package com.centerops.repository;

import com.centerops.entity.Enrollment;
import com.centerops.entity.EnrollmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    Page<Enrollment> findAllByPersonId(Long personId, Pageable pageable);

    List<Enrollment> findAllByPersonId(Long personId);

    boolean existsByPersonIdAndCourseId(Long personId, Long courseId);

    long countByStatus(EnrollmentStatus status);
}
