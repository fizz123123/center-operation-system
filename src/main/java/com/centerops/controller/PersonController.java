package com.centerops.controller;

import com.centerops.dto.request.PersonCreateRequest;
import com.centerops.dto.request.PersonUpdateRequest;
import com.centerops.dto.response.PageResponse;
import com.centerops.dto.response.PersonResponse;
import com.centerops.service.PersonService;
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

@RestController
@RequestMapping("/api/people")
@RequiredArgsConstructor
public class PersonController {

    private final PersonService personService;

    @GetMapping
    public ResponseEntity<PageResponse<PersonResponse>> getAll(
            @RequestParam(defaultValue = "0") int page
    ) {
        return ResponseEntity.ok(personService.getAll(page));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PersonResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(personService.getById(id));
    }

    @PostMapping
    public ResponseEntity<PersonResponse> create(@Valid @RequestBody PersonCreateRequest request) {
        PersonResponse response = personService.create(request);
        return ResponseEntity.created(URI.create("/api/people/" + response.id())).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PersonResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody PersonUpdateRequest request
    ) {
        return ResponseEntity.ok(personService.update(id, request));
    }
}
