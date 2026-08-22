package com.centerops.service.impl;

import com.centerops.dto.request.PersonCreateRequest;
import com.centerops.dto.request.PersonUpdateRequest;
import com.centerops.dto.response.PageResponse;
import com.centerops.dto.response.PersonResponse;
import com.centerops.entity.Person;
import com.centerops.exception.DuplicateResourceException;
import com.centerops.exception.InvalidStateException;
import com.centerops.exception.ResourceNotFoundException;
import com.centerops.mapper.PersonMapper;
import com.centerops.repository.PersonRepository;
import com.centerops.service.PersonService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PersonServiceImpl implements PersonService {

    private final PersonRepository personRepository;
    private final PersonMapper personMapper;

    @Override
    public PageResponse<PersonResponse> getAll(int page) {
        validatePage(page);
        return PageResponse.from(
                personRepository.findAll(PageRequest.of(
                                page,
                                PageResponse.DEFAULT_SIZE,
                                Sort.by(Sort.Direction.ASC, "id")
                        ))
                        .map(personMapper::toResponse)
        );
    }

    @Override
    public PersonResponse getById(Long id) {
        return personMapper.toResponse(findPerson(id));
    }

    @Override
    @Transactional
    public PersonResponse create(PersonCreateRequest request) {
        String email = request.email().trim();
        if (personRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException("Email is already in use: " + email);
        }
        return personMapper.toResponse(personRepository.save(personMapper.toEntity(request)));
    }

    @Override
    @Transactional
    public PersonResponse update(Long id, PersonUpdateRequest request) {
        Person person = findPerson(id);

        if (request.name() != null) {
            person.setName(requireText(request.name(), "name"));
        }
        if (request.email() != null) {
            String email = requireText(request.email(), "email").toLowerCase();
            if (personRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
                throw new DuplicateResourceException("Email is already in use: " + email);
            }
            person.setEmail(email);
        }
        if (request.phone() != null) {
            person.setPhone(request.phone().trim());
        }
        if (request.status() != null) {
            person.setStatus(request.status());
        }

        return personMapper.toResponse(personRepository.save(person));
    }

    private Person findPerson(Long id) {
        return personRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Person", id));
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
}
