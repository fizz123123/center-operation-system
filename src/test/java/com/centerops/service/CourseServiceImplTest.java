package com.centerops.service;

import com.centerops.dto.request.CourseCreateRequest;
import com.centerops.dto.response.AvailablePrerequisiteResponse;
import com.centerops.dto.response.CourseOptionResponse;
import com.centerops.dto.response.CourseResponse;
import com.centerops.entity.Course;
import com.centerops.entity.CoursePrerequisite;
import com.centerops.exception.BusinessConflictException;
import com.centerops.exception.DuplicateResourceException;
import com.centerops.exception.InvalidStateException;
import com.centerops.mapper.CourseMapper;
import com.centerops.mapper.CoursePrerequisiteMapper;
import com.centerops.repository.CoursePrerequisiteRepository;
import com.centerops.repository.CourseRepository;
import com.centerops.service.impl.CourseServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseServiceImplTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private CoursePrerequisiteRepository prerequisiteRepository;

    @Mock
    private CourseMapper courseMapper;

    @Mock
    private CoursePrerequisiteMapper prerequisiteMapper;

    @InjectMocks
    private CourseServiceImpl courseService;

    @Test
    void getAllShouldUseFixedTenItemPage() {
        Course course = course(1L, "JAVA-001", "Java");
        Course prerequisite = course(2L, "BASIC-001", "Programming Basic");
        CoursePrerequisite relation = CoursePrerequisite.builder()
                .id(1L)
                .course(course)
                .prerequisite(prerequisite)
                .build();
        CourseResponse mapped = new CourseResponse(1L, "JAVA-001", "Java", null, List.of(2L));
        when(courseRepository.findAllBySearch(eq(null), any(Pageable.class)))
                .thenAnswer(invocation -> new PageImpl<>(
                        List.of(course),
                        invocation.getArgument(1),
                        20
                ));
        when(prerequisiteRepository.findAllByCourseIdInOrderByIdAsc(List.of(1L))).thenReturn(List.of(relation));
        when(courseMapper.toResponse(course, List.of(2L))).thenReturn(mapped);

        var response = courseService.getAll(0, null);

        assertThat(response.content()).containsExactly(mapped);
        assertThat(response.size()).isEqualTo(10);
        assertThat(response.totalElements()).isEqualTo(20);
        assertThat(response.totalPages()).isEqualTo(2);
    }

    @Test
    void getAllShouldNormalizeCourseSearch() {
        when(courseRepository.findAllBySearch(eq("Java"), any(Pageable.class)))
                .thenAnswer(invocation -> new PageImpl<>(List.of(), invocation.getArgument(1), 0));

        courseService.getAll(0, "  Java  ");

        verify(courseRepository).findAllBySearch(eq("Java"), any(Pageable.class));
    }

    @Test
    void createShouldReturnResponseWhenCodeIsAvailable() {
        CourseCreateRequest request = new CourseCreateRequest("JAVA-001", "Java", "Java basics");
        Course course = course(1L, "JAVA-001", "Java");
        CourseResponse expected = new CourseResponse(1L, "JAVA-001", "Java", "Java basics", List.of());

        when(courseRepository.existsByCodeIgnoreCase("JAVA-001")).thenReturn(false);
        when(courseMapper.toEntity(request)).thenReturn(course);
        when(courseRepository.save(course)).thenReturn(course);
        when(courseMapper.toResponse(course, List.of())).thenReturn(expected);

        assertThat(courseService.create(request)).isEqualTo(expected);
    }

    @Test
    void createShouldRejectDuplicateCode() {
        CourseCreateRequest request = new CourseCreateRequest("JAVA-001", "Java", null);
        when(courseRepository.existsByCodeIgnoreCase("JAVA-001")).thenReturn(true);

        assertThatThrownBy(() -> courseService.create(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("JAVA-001");

        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void getByIdShouldReturnMappedCourse() {
        Course course = course(1L, "JAVA-001", "Java");
        CourseResponse expected = new CourseResponse(1L, "JAVA-001", "Java", null, List.of());
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(prerequisiteRepository.findAllByCourseIdInOrderByIdAsc(List.of(1L))).thenReturn(List.of());
        when(courseMapper.toResponse(course, List.of())).thenReturn(expected);

        assertThat(courseService.getById(1L)).isEqualTo(expected);
    }

    @Test
    void addPrerequisiteShouldRejectSelfReference() {
        assertThatThrownBy(() -> courseService.addPrerequisite(1L, 1L))
                .isInstanceOf(InvalidStateException.class)
                .hasMessageContaining("own prerequisite");
    }

    @Test
    void addPrerequisiteShouldRejectRelationshipThatCreatesCycle() {
        Course java = course(1L, "JAVA-001", "Java");
        Course oop = course(2L, "JAVA-002", "OOP");
        CoursePrerequisite existing = CoursePrerequisite.builder()
                .course(oop)
                .prerequisite(java)
                .build();

        when(prerequisiteRepository.existsByCourseIdAndPrerequisiteId(1L, 2L)).thenReturn(false);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(java));
        when(courseRepository.findById(2L)).thenReturn(Optional.of(oop));
        when(prerequisiteRepository.findAll()).thenReturn(List.of(existing));

        assertThatThrownBy(() -> courseService.addPrerequisite(1L, 2L))
                .isInstanceOf(BusinessConflictException.class)
                .hasMessageContaining("cycle");

        verify(prerequisiteRepository, never()).save(any(CoursePrerequisite.class));
    }

    @Test
    void getOptionsShouldIncludePrerequisiteIds() {
        Course java = course(1L, "JAVA-001", "Java");
        Course oop = course(2L, "OOP-001", "OOP");
        CoursePrerequisite relation = CoursePrerequisite.builder()
                .id(1L)
                .course(oop)
                .prerequisite(java)
                .build();
        CourseOptionResponse javaOption = new CourseOptionResponse(1L, "JAVA-001", "Java", List.of());
        CourseOptionResponse oopOption = new CourseOptionResponse(2L, "OOP-001", "OOP", List.of(1L));

        when(courseRepository.findAll(any(Sort.class))).thenReturn(List.of(java, oop));
        when(prerequisiteRepository.findAllByCourseIdInOrderByIdAsc(List.of(1L, 2L)))
                .thenReturn(List.of(relation));
        when(courseMapper.toOptionResponse(java, List.of())).thenReturn(javaOption);
        when(courseMapper.toOptionResponse(oop, List.of(1L))).thenReturn(oopOption);

        assertThat(courseService.getOptions()).containsExactly(javaOption, oopOption);
    }

    @Test
    void getAvailablePrerequisitesShouldExcludeSelfExistingAndCycleCandidates() {
        Course target = course(1L, "JAVA-001", "Java");
        Course cycleCandidate = course(2L, "OOP-001", "OOP");
        Course existingPrerequisite = course(3L, "BASIC-001", "Basic");
        Course safeCandidate = course(4L, "DB-001", "Database");
        CoursePrerequisite dependentRelation = CoursePrerequisite.builder()
                .id(1L)
                .course(cycleCandidate)
                .prerequisite(target)
                .build();
        CoursePrerequisite existingRelation = CoursePrerequisite.builder()
                .id(2L)
                .course(target)
                .prerequisite(existingPrerequisite)
                .build();
        AvailablePrerequisiteResponse safeResponse = new AvailablePrerequisiteResponse(
                4L,
                "DB-001",
                "Database"
        );

        when(courseRepository.findById(1L)).thenReturn(Optional.of(target));
        when(courseRepository.findAll(any(Sort.class)))
                .thenReturn(List.of(target, cycleCandidate, existingPrerequisite, safeCandidate));
        when(prerequisiteRepository.findAllByOrderByIdAsc())
                .thenReturn(List.of(dependentRelation, existingRelation));
        when(courseMapper.toAvailablePrerequisiteResponse(safeCandidate)).thenReturn(safeResponse);

        assertThat(courseService.getAvailablePrerequisites(1L)).containsExactly(safeResponse);
    }

    @Test
    void removePrerequisiteShouldDeleteExistingRelationship() {
        CoursePrerequisite relation = CoursePrerequisite.builder().id(10L).build();
        when(prerequisiteRepository.findByCourseIdAndPrerequisiteId(2L, 1L))
                .thenReturn(Optional.of(relation));

        courseService.removePrerequisite(2L, 1L);

        verify(prerequisiteRepository).delete(relation);
    }

    @Test
    void getLearningPathShouldReturnPrerequisitesBeforeDependentCourses() {
        Course java = course(1L, "JAVA-001", "Java");
        Course oop = course(2L, "JAVA-002", "OOP");
        CoursePrerequisite relation = CoursePrerequisite.builder()
                .course(oop)
                .prerequisite(java)
                .build();

        when(courseRepository.findAll(any(Sort.class))).thenReturn(List.of(java, oop));
        when(prerequisiteRepository.findAllByOrderByIdAsc()).thenReturn(List.of(relation));

        assertThat(courseService.getLearningPath()).containsExactly("Java", "OOP");
    }

    private Course course(Long id, String code, String name) {
        return Course.builder().id(id).code(code).name(name).build();
    }
}
