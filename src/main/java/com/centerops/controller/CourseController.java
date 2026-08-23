package com.centerops.controller;

import com.centerops.dto.request.CourseCreateRequest;
import com.centerops.dto.request.CourseUpdateRequest;
import com.centerops.dto.request.PrerequisiteCreateRequest;
import com.centerops.dto.response.AvailablePrerequisiteResponse;
import com.centerops.dto.response.CourseOptionResponse;
import com.centerops.dto.response.CourseGraphResponse;
import com.centerops.dto.response.CoursePrerequisiteResponse;
import com.centerops.dto.response.CourseResponse;
import com.centerops.dto.response.PageResponse;
import com.centerops.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @GetMapping
    public ResponseEntity<PageResponse<CourseResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String search
    ) {
        return ResponseEntity.ok(courseService.getAll(page, search));
    }

    @GetMapping("/options")
    public ResponseEntity<List<CourseOptionResponse>> getOptions() {
        return ResponseEntity.ok(courseService.getOptions());
    }

    @GetMapping("/{courseId}/available-prerequisites")
    public ResponseEntity<List<AvailablePrerequisiteResponse>> getAvailablePrerequisites(
            @PathVariable Long courseId
    ) {
        return ResponseEntity.ok(courseService.getAvailablePrerequisites(courseId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.getById(id));
    }

    @PostMapping
    public ResponseEntity<CourseResponse> create(@Valid @RequestBody CourseCreateRequest request) {
        CourseResponse response = courseService.create(request);
        return ResponseEntity.created(URI.create("/api/courses/" + response.id())).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CourseResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody CourseUpdateRequest request
    ) {
        return ResponseEntity.ok(courseService.update(id, request));
    }

    @PostMapping("/{courseId}/prerequisites")
    public ResponseEntity<CoursePrerequisiteResponse> addPrerequisite(
            @PathVariable Long courseId,
            @Valid @RequestBody PrerequisiteCreateRequest request
    ) {
        CoursePrerequisiteResponse response = courseService.addPrerequisite(courseId, request.prerequisiteId());
        return ResponseEntity.created(URI.create("/api/courses/" + courseId + "/prerequisites/" + response.id()))
                .body(response);
    }

    @DeleteMapping("/{courseId}/prerequisites/{prerequisiteId}")
    public ResponseEntity<Void> removePrerequisite(
            @PathVariable Long courseId,
            @PathVariable Long prerequisiteId
    ) {
        courseService.removePrerequisite(courseId, prerequisiteId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/learning-path")
    public ResponseEntity<CourseGraphResponse> getLearningPath() {
        return ResponseEntity.ok(courseService.getLearningPath());
    }
}
