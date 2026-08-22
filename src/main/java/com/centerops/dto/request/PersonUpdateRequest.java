package com.centerops.dto.request;

import com.centerops.entity.PersonStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record PersonUpdateRequest(
        @Size(min = 1, max = 50) String name,
        @Email @Size(max = 100) String email,
        @Size(max = 20) String phone,
        PersonStatus status
) {
}
