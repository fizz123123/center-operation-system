package com.centerops.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "course_prerequisites", uniqueConstraints = {
        @UniqueConstraint(name = "uq_course_prerequisite", columnNames = {"course_id", "prerequisite_id"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoursePrerequisite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "prerequisite_id", nullable = false)
    private Course prerequisite;

    @Column(name = "created_at", insertable = false, updatable = false, nullable = false)
    private LocalDateTime createdAt;
}
