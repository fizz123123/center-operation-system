package com.centerops.service.impl;

import com.centerops.algorithm.DepthFirstSearch;
import com.centerops.algorithm.TopologicalSort;
import com.centerops.datastructure.CourseGraph;
import com.centerops.dto.request.CourseCreateRequest;
import com.centerops.dto.request.CourseUpdateRequest;
import com.centerops.dto.response.AvailablePrerequisiteResponse;
import com.centerops.dto.response.CourseOptionResponse;
import com.centerops.dto.response.CourseEdgeResponse;
import com.centerops.dto.response.CourseGraphResponse;
import com.centerops.dto.response.CourseNodeResponse;
import com.centerops.dto.response.CoursePrerequisiteResponse;
import com.centerops.dto.response.CourseResponse;
import com.centerops.dto.response.PageResponse;
import com.centerops.entity.Course;
import com.centerops.entity.CoursePrerequisite;
import com.centerops.exception.BusinessConflictException;
import com.centerops.exception.DuplicateResourceException;
import com.centerops.exception.InvalidStateException;
import com.centerops.exception.ResourceNotFoundException;
import com.centerops.mapper.CourseMapper;
import com.centerops.mapper.CoursePrerequisiteMapper;
import com.centerops.repository.CoursePrerequisiteRepository;
import com.centerops.repository.CourseRepository;
import com.centerops.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final CoursePrerequisiteRepository prerequisiteRepository;
    private final CourseMapper courseMapper;
    private final CoursePrerequisiteMapper prerequisiteMapper;

    @Override
    public PageResponse<CourseResponse> getAll(int page, String search) {
        validatePage(page);
        Page<Course> courses = courseRepository.findAllBySearch(
                normalizeSearch(search),
                PageRequest.of(
                        page,
                        PageResponse.DEFAULT_SIZE,
                        Sort.by(Sort.Direction.ASC, "id")
                )
        );
        Map<Long, List<Long>> prerequisiteIds = getPrerequisiteIds(courses.getContent());
        return PageResponse.from(courses.map(course ->
                courseMapper.toResponse(course, prerequisiteIds.getOrDefault(course.getId(), List.of()))
        ));
    }

    @Override
    public List<CourseOptionResponse> getOptions() {
        List<Course> courses = courseRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
        Map<Long, List<Long>> prerequisiteIds = getPrerequisiteIds(courses);
        return courses.stream()
                .map(course -> courseMapper.toOptionResponse(
                        course,
                        prerequisiteIds.getOrDefault(course.getId(), List.of())
                ))
                .toList();
    }

    @Override
    public List<AvailablePrerequisiteResponse> getAvailablePrerequisites(Long courseId) {
        findCourse(courseId);
        List<Course> courses = courseRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
        List<CoursePrerequisite> relations = prerequisiteRepository.findAllByOrderByIdAsc();
        Set<Long> existingPrerequisiteIds = new HashSet<>();
        relations.stream()
                .filter(relation -> relation.getCourse().getId().equals(courseId))
                .map(relation -> relation.getPrerequisite().getId())
                .forEach(existingPrerequisiteIds::add);
        CourseGraph<Long> graph = buildCourseGraph(courses, relations);

        return courses.stream()
                .filter(candidate -> !candidate.getId().equals(courseId))
                .filter(candidate -> !existingPrerequisiteIds.contains(candidate.getId()))
                .filter(candidate -> !wouldCreateCycle(graph, courseId, candidate.getId()))
                .map(courseMapper::toAvailablePrerequisiteResponse)
                .toList();
    }

    @Override
    public CourseResponse getById(Long id) {
        Course course = findCourse(id);
        Map<Long, List<Long>> prerequisiteIds = getPrerequisiteIds(List.of(course));
        return courseMapper.toResponse(course, prerequisiteIds.getOrDefault(id, List.of()));
    }

    @Override
    @Transactional
    public CourseResponse create(CourseCreateRequest request) {
        String code = request.code().trim();
        if (courseRepository.existsByCodeIgnoreCase(code)) {
            throw new DuplicateResourceException("Course code already exists: " + code);
        }
        return courseMapper.toResponse(
                courseRepository.save(courseMapper.toEntity(request)),
                List.of()
        );
    }

    @Override
    @Transactional
    public CourseResponse update(Long id, CourseUpdateRequest request) {
        Course course = findCourse(id);

        if (request.code() != null) {
            String code = requireText(request.code(), "code").toUpperCase();
            if (courseRepository.existsByCodeIgnoreCaseAndIdNot(code, id)) {
                throw new DuplicateResourceException("Course code already exists: " + code);
            }
            course.setCode(code);
        }
        if (request.name() != null) {
            course.setName(requireText(request.name(), "name"));
        }
        if (request.description() != null) {
            course.setDescription(request.description());
        }

        Map<Long, List<Long>> prerequisiteIds = getPrerequisiteIds(List.of(course));
        return courseMapper.toResponse(
                courseRepository.save(course),
                prerequisiteIds.getOrDefault(id, List.of())
        );
    }

    @Override
    @Transactional
    public CoursePrerequisiteResponse addPrerequisite(Long courseId, Long prerequisiteId) {
        if (courseId.equals(prerequisiteId)) {
            throw new InvalidStateException("A course cannot be its own prerequisite");
        }
        if (prerequisiteRepository.existsByCourseIdAndPrerequisiteId(courseId, prerequisiteId)) {
            throw new DuplicateResourceException("The prerequisite relationship already exists");
        }

        Course course = findCourse(courseId);
        Course prerequisite = findCourse(prerequisiteId);
        List<CoursePrerequisite> relations = prerequisiteRepository.findAll();
        CourseGraph<Long> graph = buildCourseGraph(List.of(course, prerequisite), relations);
        if (wouldCreateCycle(graph, courseId, prerequisiteId)) {
            throw new BusinessConflictException("The prerequisite relationship would create a cycle");
        }

        CoursePrerequisite relation = CoursePrerequisite.builder()
                .course(course)
                .prerequisite(prerequisite)
                .build();
        return prerequisiteMapper.toResponse(prerequisiteRepository.save(relation));
    }

    @Override
    @Transactional
    public void removePrerequisite(Long courseId, Long prerequisiteId) {
        CoursePrerequisite relation = prerequisiteRepository
                .findByCourseIdAndPrerequisiteId(courseId, prerequisiteId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Course prerequisite relationship",
                        prerequisiteId
                ));
        prerequisiteRepository.delete(relation);
    }

    @Override
    public CourseGraphResponse getLearningPath() {
        List<Course> courses = courseRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
        List<CoursePrerequisite> relations = prerequisiteRepository.findAllByOrderByIdAsc();
        CourseGraph<Long> graph = buildCourseGraph(courses, relations);
        List<List<Long>> stages;
        List<Long> topologicalOrder;
        try {
            stages = TopologicalSort.sortByStages(graph);
            topologicalOrder = stages.stream()
                    .flatMap(List::stream)
                    .toList();
        } catch (IllegalStateException exception) {
            throw new InvalidStateException("Course prerequisite graph contains a cycle");
        }

        List<CourseNodeResponse> nodes = courses.stream()
                .map(course -> new CourseNodeResponse(course.getId(), course.getCode(), course.getName()))
                .toList();
        List<CourseEdgeResponse> edges = relations.stream()
                .map(relation -> new CourseEdgeResponse(
                        relation.getPrerequisite().getId(),
                        relation.getCourse().getId()
                ))
                .toList();
        return new CourseGraphResponse(nodes, edges, topologicalOrder, stages);
    }

    private CourseGraph<Long> buildCourseGraph(
            Collection<Course> courses,
            List<CoursePrerequisite> relations
    ) {
        CourseGraph<Long> graph = new CourseGraph<>();
        courses.forEach(course -> graph.addVertex(course.getId()));
        relations.forEach(relation -> {
            graph.addVertex(relation.getPrerequisite().getId());
            graph.addVertex(relation.getCourse().getId());
        });
        relations.forEach(relation -> graph.addEdge(
                relation.getPrerequisite().getId(),
                relation.getCourse().getId()
        ));
        return graph;
    }

    private boolean wouldCreateCycle(
            CourseGraph<Long> graph,
            Long courseId,
            Long prerequisiteId
    ) {
        return DepthFirstSearch.isReachable(graph, courseId, prerequisiteId);
    }

    private Map<Long, List<Long>> getPrerequisiteIds(Collection<Course> courses) {
        if (courses.isEmpty()) {
            return Map.of();
        }
        List<Long> courseIds = courses.stream().map(Course::getId).toList();
        Map<Long, List<Long>> prerequisiteIds = new HashMap<>();
        prerequisiteRepository.findAllByCourseIdInOrderByIdAsc(courseIds).forEach(relation ->
                prerequisiteIds.computeIfAbsent(relation.getCourse().getId(), ignored -> new ArrayList<>())
                        .add(relation.getPrerequisite().getId())
        );
        return prerequisiteIds;
    }

    private Course findCourse(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", id));
    }

    private String requireText(String value, String field) {
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new InvalidStateException(field + " must not be blank");
        }
        return normalized;
    }

    private void validatePage(int page) {
        if (page < 0) {
            throw new InvalidStateException("Page index must be zero or greater");
        }
    }

    private String normalizeSearch(String search) {
        if (search == null) {
            return null;
        }
        String normalized = search.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
