package com.centerops.service;

import com.centerops.dto.request.PersonCreateRequest;
import com.centerops.dto.response.PersonResponse;
import com.centerops.dto.response.PersonStatisticsResponse;
import com.centerops.entity.Person;
import com.centerops.entity.PersonStatus;
import com.centerops.exception.DuplicateResourceException;
import com.centerops.exception.ResourceNotFoundException;
import com.centerops.mapper.PersonMapper;
import com.centerops.repository.PersonRepository;
import com.centerops.service.impl.PersonServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class PersonServiceImplTest {

    @Mock
    private PersonRepository personRepository;

    @Mock
    private PersonMapper personMapper;

    @InjectMocks
    private PersonServiceImpl personService;

    @Test
    void getAllShouldReturnTenItemPageMetadata() {
        Person person = Person.builder()
                .id(1L)
                .name("Ada")
                .email("ada@example.com")
                .status(PersonStatus.ACTIVE)
                .build();
        PersonResponse mapped = new PersonResponse(1L, "Ada", "ada@example.com", null, PersonStatus.ACTIVE);
        when(personRepository.findAllBySearchAndStatus(eq(null), eq(null), any(Pageable.class)))
                .thenAnswer(invocation -> new PageImpl<>(
                        List.of(person),
                        invocation.getArgument(2),
                        21
                ));
        when(personMapper.toResponse(person)).thenReturn(mapped);

        var response = personService.getAll(1, null, null);

        assertThat(response.content()).containsExactly(mapped);
        assertThat(response.page()).isEqualTo(1);
        assertThat(response.size()).isEqualTo(10);
        assertThat(response.totalElements()).isEqualTo(21);
        assertThat(response.totalPages()).isEqualTo(3);
    }

    @Test
    void getAllShouldNormalizeSearchAndApplyStatusBeforePaging() {
        when(personRepository.findAllBySearchAndStatus(eq("Ada"), eq(PersonStatus.ACTIVE), any(Pageable.class)))
                .thenAnswer(invocation -> new PageImpl<>(List.of(), invocation.getArgument(2), 0));

        personService.getAll(0, "  Ada  ", PersonStatus.ACTIVE);

        verify(personRepository).findAllBySearchAndStatus(eq("Ada"), eq(PersonStatus.ACTIVE), any(Pageable.class));
    }

    @Test
    void getStatisticsShouldReturnConsistentStatusCounts() {
        when(personRepository.countByStatus(PersonStatus.ACTIVE)).thenReturn(180L);
        when(personRepository.countByStatus(PersonStatus.INACTIVE)).thenReturn(20L);

        PersonStatisticsResponse response = personService.getStatistics();

        assertThat(response.total()).isEqualTo(200L);
        assertThat(response.active()).isEqualTo(180L);
        assertThat(response.inactive()).isEqualTo(20L);
    }

    @Test
    void createShouldReturnResponseWhenEmailIsAvailable() {
        PersonCreateRequest request = new PersonCreateRequest("Ada", "ada@example.com", "0912345678");
        Person person = Person.builder()
                .name("Ada")
                .email("ada@example.com")
                .status(PersonStatus.ACTIVE)
                .build();
        Person saved = Person.builder()
                .id(1L)
                .name("Ada")
                .email("ada@example.com")
                .status(PersonStatus.ACTIVE)
                .build();
        PersonResponse expected = new PersonResponse(1L, "Ada", "ada@example.com", "0912345678", PersonStatus.ACTIVE);

        when(personRepository.existsByEmailIgnoreCase("ada@example.com")).thenReturn(false);
        when(personMapper.toEntity(request)).thenReturn(person);
        when(personRepository.save(person)).thenReturn(saved);
        when(personMapper.toResponse(saved)).thenReturn(expected);

        PersonResponse actual = personService.create(request);

        assertThat(actual).isEqualTo(expected);
        verify(personRepository).save(person);
    }

    @Test
    void createShouldRejectDuplicateEmail() {
        PersonCreateRequest request = new PersonCreateRequest("Ada", "ada@example.com", null);
        when(personRepository.existsByEmailIgnoreCase("ada@example.com")).thenReturn(true);

        assertThatThrownBy(() -> personService.create(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("ada@example.com");

        verify(personRepository, never()).save(org.mockito.ArgumentMatchers.any(Person.class));
    }

    @Test
    void getByIdShouldRejectUnknownPerson() {
        when(personRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> personService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Person")
                .hasMessageContaining("99");
    }
}
