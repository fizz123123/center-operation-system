package com.centerops.dto.response;

import com.centerops.entity.PersonStatus;

public record PersonResponse(Long id, String name, String email, String phone, PersonStatus status) {
}
