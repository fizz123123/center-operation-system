package com.centerops.repository;

import com.centerops.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseRepository extends JpaRepository<Course, Long> {

    @Query("""
            SELECT course
            FROM Course course
            WHERE (:search IS NULL
                   OR LOWER(course.code) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(course.name) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Page<Course> findAllBySearch(@Param("search") String search, Pageable pageable);

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);
}
