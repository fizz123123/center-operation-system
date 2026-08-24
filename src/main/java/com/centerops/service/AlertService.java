package com.centerops.service;

import com.centerops.dto.response.AlertResponse;
import com.centerops.dto.response.PageResponse;

public interface AlertService {
    PageResponse<AlertResponse> getAllByPriority(int page, Integer priority);
}
