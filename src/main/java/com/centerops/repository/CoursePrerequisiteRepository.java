package com.centerops.repository;

import com.centerops.entity.CoursePrerequisite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CoursePrerequisiteRepository extends JpaRepository<CoursePrerequisite, Long> {
    List<CoursePrerequisite> findAllByOrderByIdAsc();

    List<CoursePrerequisite> findAllByCourseIdInOrderByIdAsc(Collection<Long> courseIds);

    Optional<CoursePrerequisite> findByCourseIdAndPrerequisiteId(Long courseId, Long prerequisiteId);

    boolean existsByCourseIdAndPrerequisiteId(Long courseId, Long prerequisiteId);
}
