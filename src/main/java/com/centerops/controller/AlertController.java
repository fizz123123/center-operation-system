package com.centerops.controller;

import com.centerops.dto.response.AlertResponse;
import com.centerops.dto.response.PageResponse;
import com.centerops.service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @GetMapping
    public ResponseEntity<PageResponse<AlertResponse>> getAll(
            @RequestParam(defaultValue = "0") int page
    ) {
        return ResponseEntity.ok(alertService.getAllByPriority(page));
    }
}
