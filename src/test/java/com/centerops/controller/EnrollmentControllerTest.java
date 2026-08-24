package com.centerops.controller;

import com.centerops.dto.request.EnrollmentUpdateRequest;
import com.centerops.dto.response.CourseOptionResponse;
import com.centerops.dto.response.PageResponse;
import com.centerops.exception.BusinessConflictException;
import com.centerops.exception.GlobalExceptionHandler;
import com.centerops.service.EnrollmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class EnrollmentControllerTest {

    @Mock
    private EnrollmentService enrollmentService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new EnrollmentController(enrollmentService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getAllShouldPassCourseNameSortParameters() throws Exception {
        when(enrollmentService.getAll(1, "courseName", "desc"))
                .thenReturn(new PageResponse<>(List.of(), 1, 10, 0, 0, false, true));

        mockMvc.perform(get("/api/enrollments")
                        .param("page", "1")
                        .param("sort", "courseName")
                        .param("direction", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1));

        verify(enrollmentService).getAll(1, "courseName", "desc");
    }

    @Test
    void getByPersonIdShouldPassCourseNameSortParameters() throws Exception {
        when(enrollmentService.getByPersonId(9L, 0, "courseName", "asc"))
                .thenReturn(new PageResponse<>(List.of(), 0, 10, 0, 0, true, true));

        mockMvc.perform(get("/api/people/9/enrollments")
                        .param("sort", "courseName")
                        .param("direction", "asc"))
                .andExpect(status().isOk());

        verify(enrollmentService).getByPersonId(9L, 0, "courseName", "asc");
    }

    @Test
    void getAvailableCoursesShouldReturnEligibleCourseOptions() throws Exception {
        when(enrollmentService.getAvailableCourses(9L)).thenReturn(List.of(
                new CourseOptionResponse(2L, "JAVA-002", "Object Oriented Programming", List.of(1L))
        ));

        mockMvc.perform(get("/api/people/9/available-courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2L))
                .andExpect(jsonPath("$[0].code").value("JAVA-002"))
                .andExpect(jsonPath("$[0].prerequisiteIds[0]").value(1L));

        verify(enrollmentService).getAvailableCourses(9L);
    }

    @Test
    void updateCompletedEnrollmentShouldReturnConflict() throws Exception {
        when(enrollmentService.updateStatus(
                org.mockito.ArgumentMatchers.eq(10L),
                any(EnrollmentUpdateRequest.class)
        )).thenThrow(new BusinessConflictException("Completed enrollment status cannot be modified"));

        mockMvc.perform(put("/api/enrollments/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "IN_PROGRESS"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Completed enrollment status cannot be modified"));
    }
}
