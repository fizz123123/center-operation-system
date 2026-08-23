package com.centerops.repository;

import com.centerops.entity.Alert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertRepository extends JpaRepository<Alert, Long> {
    Page<Alert> findAllByOrderByPriorityDescCreatedAtAscIdAsc(Pageable pageable);

    Page<Alert> findAllByPriorityOrderByCreatedAtAscIdAsc(int priority, Pageable pageable);
}
