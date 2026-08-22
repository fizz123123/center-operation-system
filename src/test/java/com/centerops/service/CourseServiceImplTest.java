package com.centerops.service;

import com.centerops.dto.request.CourseCreateRequest;
import com.centerops.dto.response.CourseResponse;
import com.centerops.entity.Course;
import com.centerops.entity.CoursePrerequisite;
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
        CourseResponse mapped = new CourseResponse(1L, "JAVA-001", "Java", null);
        when(courseRepository.findAll(any(Pageable.class)))
                .thenAnswer(invocation -> new PageImpl<>(
                        List.of(course),
                        invocation.getArgument(0),
                        20
                ));
        when(courseMapper.toResponse(course)).thenReturn(mapped);

        var response = courseService.getAll(0);

        assertThat(response.content()).containsExactly(mapped);
        assertThat(response.size()).isEqualTo(10);
        assertThat(response.totalElements()).isEqualTo(20);
        assertThat(response.totalPages()).isEqualTo(2);
    }

    @Test
    void createShouldReturnResponseWhenCodeIsAvailable() {
        CourseCreateRequest request = new CourseCreateRequest("JAVA-001", "Java", "Java basics");
        Course course = course(1L, "JAVA-001", "Java");
        CourseResponse expected = new CourseResponse(1L, "JAVA-001", "Java", "Java basics");

        when(courseRepository.existsByCodeIgnoreCase("JAVA-001")).thenReturn(false);
        when(courseMapper.toEntity(request)).thenReturn(course);
        when(courseRepository.save(course)).thenReturn(course);
        when(courseMapper.toResponse(course)).thenReturn(expected);

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
        CourseResponse expected = new CourseResponse(1L, "JAVA-001", "Java", null);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(courseMapper.toResponse(course)).thenReturn(expected);

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
                .isInstanceOf(InvalidStateException.class)
                .hasMessageContaining("cycle");

        verify(prerequisiteRepository, never()).save(any(CoursePrerequisite.class));
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
