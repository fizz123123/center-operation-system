package com.centerops.mapper;

import com.centerops.dto.request.PersonCreateRequest;
import com.centerops.dto.response.PersonResponse;
import com.centerops.entity.Person;
import com.centerops.entity.PersonStatus;
import org.springframework.stereotype.Component;

@Component
public class PersonMapper {

    public Person toEntity(PersonCreateRequest request) {
        return Person.builder()
                .name(request.name().trim())
                .email(request.email().trim().toLowerCase())
                .phone(request.phone())
                .status(PersonStatus.ACTIVE)
                .build();
    }

    public PersonResponse toResponse(Person person) {
        return new PersonResponse(
                person.getId(),
                person.getName(),
                person.getEmail(),
                person.getPhone(),
                person.getStatus()
        );
    }
}
