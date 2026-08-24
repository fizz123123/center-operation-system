package com.centerops.repository;

import com.centerops.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findAllByPriority(int priority);

    Optional<Alert> findFirstByPerson_IdAndCourse_IdAndMessageStartingWith(
            Long personId,
            Long courseId,
            String messagePrefix
    );
}
