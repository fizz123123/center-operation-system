package com.centerops.repository;

import com.centerops.entity.Person;
import com.centerops.entity.PersonStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PersonRepository extends JpaRepository<Person, Long> {

    @Query("""
            SELECT person
            FROM Person person
            WHERE (:status IS NULL OR person.status = :status)
              AND (:search IS NULL
                   OR LOWER(person.name) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(person.email) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Page<Person> findAllBySearchAndStatus(
            @Param("search") String search,
            @Param("status") PersonStatus status,
            Pageable pageable
    );

    long countByStatus(PersonStatus status);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
}
