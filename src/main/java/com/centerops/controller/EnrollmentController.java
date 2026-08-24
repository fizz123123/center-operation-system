package com.centerops.controller;

import com.centerops.dto.request.EnrollmentCreateRequest;
import com.centerops.dto.request.EnrollmentUpdateRequest;
import com.centerops.dto.response.CourseOptionResponse;
import com.centerops.dto.response.EnrollmentResponse;
import com.centerops.dto.response.PageResponse;
import com.centerops.service.EnrollmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @GetMapping("/enrollments")
    public ResponseEntity<PageResponse<EnrollmentResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        return ResponseEntity.ok(enrollmentService.getAll(page, sort, direction));
    }

    @GetMapping("/people/{personId}/enrollments")
    public ResponseEntity<PageResponse<EnrollmentResponse>> getByPersonId(
            @PathVariable Long personId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        return ResponseEntity.ok(enrollmentService.getByPersonId(personId, page, sort, direction));
    }

    @GetMapping("/people/{personId}/available-courses")
    public ResponseEntity<List<CourseOptionResponse>> getAvailableCourses(@PathVariable Long personId) {
        return ResponseEntity.ok(enrollmentService.getAvailableCourses(personId));
    }

    @PostMapping("/enrollments")
    public ResponseEntity<EnrollmentResponse> create(@Valid @RequestBody EnrollmentCreateRequest request) {
        EnrollmentResponse response = enrollmentService.create(request);
        return ResponseEntity.created(URI.create("/api/enrollments/" + response.id())).body(response);
    }

    @PutMapping("/enrollments/{id}")
    public ResponseEntity<EnrollmentResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody EnrollmentUpdateRequest request
    ) {
        return ResponseEntity.ok(enrollmentService.updateStatus(id, request));
    }
}
