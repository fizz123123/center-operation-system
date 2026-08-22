package com.centerops.repository;

import com.centerops.entity.CoursePrerequisite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CoursePrerequisiteRepository extends JpaRepository<CoursePrerequisite, Long> {
    List<CoursePrerequisite> findAllByOrderByIdAsc();

    boolean existsByCourseIdAndPrerequisiteId(Long courseId, Long prerequisiteId);
}
