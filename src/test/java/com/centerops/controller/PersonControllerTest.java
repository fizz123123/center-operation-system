package com.centerops.controller;

import com.centerops.dto.request.PersonCreateRequest;
import com.centerops.dto.response.PersonResponse;
import com.centerops.dto.response.PageResponse;
import com.centerops.entity.PersonStatus;
import com.centerops.exception.GlobalExceptionHandler;
import com.centerops.exception.ResourceNotFoundException;
import com.centerops.service.PersonService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PersonControllerTest {

    @Mock
    private PersonService personService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        PersonController controller = new PersonController(personService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createShouldReturn201AndLocationHeader() throws Exception {
        PersonResponse response = new PersonResponse(
                1L,
                "Ada",
                "ada@example.com",
                "0912345678",
                PersonStatus.ACTIVE
        );
        when(personService.create(any(PersonCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/people")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Ada",
                                  "email": "ada@example.com",
                                  "phone": "0912345678"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/people/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void getAllShouldPassPageParameterAndReturnPaginationMetadata() throws Exception {
        PersonResponse person = new PersonResponse(
                21L,
                "Ada",
                "ada@example.com",
                null,
                PersonStatus.ACTIVE
        );
        PageResponse<PersonResponse> response = new PageResponse<>(
                java.util.List.of(person),
                2,
                10,
                25,
                3,
                false,
                true
        );
        when(personService.getAll(2)).thenReturn(response);

        mockMvc.perform(get("/api/people").param("page", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(21))
                .andExpect(jsonPath("$.page").value(2))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(25))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    void createShouldReturn400ForInvalidRequest() throws Exception {
        mockMvc.perform(post("/api/people")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "",
                                  "email": "not-an-email"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request validation failed"))
                .andExpect(jsonPath("$.fieldErrors.name").exists())
                .andExpect(jsonPath("$.fieldErrors.email").exists());
    }

    @Test
    void getByIdShouldReturn404WhenPersonDoesNotExist() throws Exception {
        when(personService.getById(99L)).thenThrow(new ResourceNotFoundException("Person", 99L));

        mockMvc.perform(get("/api/people/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Person not found with id: 99"))
                .andExpect(jsonPath("$.path").value("/api/people/99"));
    }
}
