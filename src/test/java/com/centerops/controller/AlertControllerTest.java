package com.centerops.controller;

import com.centerops.dto.response.PageResponse;
import com.centerops.exception.GlobalExceptionHandler;
import com.centerops.exception.InvalidStateException;
import com.centerops.service.AlertService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AlertControllerTest {

    @Mock
    private AlertService alertService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AlertController(alertService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getAllShouldPassOptionalPriority() throws Exception {
        when(alertService.getAllByPriority(1, 3))
                .thenReturn(new PageResponse<>(List.of(), 1, 10, 0, 0, false, true));

        mockMvc.perform(get("/api/alerts")
                        .param("page", "1")
                        .param("priority", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1));

        verify(alertService).getAllByPriority(1, 3);
    }

    @Test
    void getAllShouldReturnBadRequestForInvalidPriority() throws Exception {
        when(alertService.getAllByPriority(0, 4))
                .thenThrow(new InvalidStateException("priority must be between 1 and 3"));

        mockMvc.perform(get("/api/alerts").param("priority", "4"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("priority must be between 1 and 3"));
    }
}
