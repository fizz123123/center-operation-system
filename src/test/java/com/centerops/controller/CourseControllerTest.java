package com.centerops.controller;

import com.centerops.dto.response.AvailablePrerequisiteResponse;
import com.centerops.dto.response.CourseOptionResponse;
import com.centerops.dto.response.CourseEdgeResponse;
import com.centerops.dto.response.CourseGraphResponse;
import com.centerops.dto.response.CourseNodeResponse;
import com.centerops.dto.response.CoursePrerequisiteResponse;
import com.centerops.dto.response.PageResponse;
import com.centerops.exception.BusinessConflictException;
import com.centerops.exception.GlobalExceptionHandler;
import com.centerops.service.CourseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CourseControllerTest {

    @Mock
    private CourseService courseService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new CourseController(courseService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getAllShouldPassSearchAndPageParameters() throws Exception {
        when(courseService.getAll(1, "java"))
                .thenReturn(new PageResponse<>(List.of(), 1, 10, 0, 0, false, true));

        mockMvc.perform(get("/api/courses")
                        .param("page", "1")
                        .param("search", "java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1));

        verify(courseService).getAll(1, "java");
    }

    @Test
    void addPrerequisiteShouldReturnCreatedRelationship() throws Exception {
        CoursePrerequisiteResponse response = new CoursePrerequisiteResponse(
                10L,
                2L,
                "OOP",
                1L,
                "Java"
        );
        when(courseService.addPrerequisite(2L, 1L)).thenReturn(response);

        mockMvc.perform(post("/api/courses/2/prerequisites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "prerequisiteId": 1
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/courses/2/prerequisites/10"))
                .andExpect(jsonPath("$.prerequisiteId").value(1));
    }

    @Test
    void removePrerequisiteShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/courses/2/prerequisites/1"))
                .andExpect(status().isNoContent());

        verify(courseService).removePrerequisite(2L, 1L);
    }

    @Test
    void addPrerequisiteShouldReturnConflictWhenCycleWouldBeCreated() throws Exception {
        when(courseService.addPrerequisite(1L, 2L))
                .thenThrow(new BusinessConflictException("The prerequisite relationship would create a cycle"));

        mockMvc.perform(post("/api/courses/1/prerequisites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "prerequisiteId": 2
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("The prerequisite relationship would create a cycle"));
    }

    @Test
    void optionsAndAvailablePrerequisitesShouldReturnLightweightResponses() throws Exception {
        when(courseService.getOptions()).thenReturn(List.of(
                new CourseOptionResponse(1L, "JAVA-001", "Java", List.of())
        ));
        when(courseService.getAvailablePrerequisites(2L)).thenReturn(List.of(
                new AvailablePrerequisiteResponse(1L, "JAVA-001", "Java")
        ));

        mockMvc.perform(get("/api/courses/options"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("JAVA-001"));

        mockMvc.perform(get("/api/courses/2/available-prerequisites"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void learningPathShouldReturnNodesEdgesAndTopologicalOrder() throws Exception {
        CourseGraphResponse response = new CourseGraphResponse(
                List.of(
                        new CourseNodeResponse(1L, "JAVA-001", "Java"),
                        new CourseNodeResponse(2L, "OOP-001", "OOP")
                ),
                List.of(new CourseEdgeResponse(1L, 2L)),
                List.of(1L, 2L)
        );
        when(courseService.getLearningPath()).thenReturn(response);

        mockMvc.perform(get("/api/courses/learning-path"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nodes[0].code").value("JAVA-001"))
                .andExpect(jsonPath("$.edges[0].fromCourseId").value(1))
                .andExpect(jsonPath("$.edges[0].toCourseId").value(2))
                .andExpect(jsonPath("$.topologicalOrder[1]").value(2));
    }
}
