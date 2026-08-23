package com.centerops.service.impl;

import com.centerops.dto.response.AlertResponse;
import com.centerops.dto.response.PageResponse;
import com.centerops.exception.InvalidStateException;
import com.centerops.mapper.AlertMapper;
import com.centerops.repository.AlertRepository;
import com.centerops.service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlertServiceImpl implements AlertService {

    private final AlertRepository alertRepository;
    private final AlertMapper alertMapper;

    @Override
    public PageResponse<AlertResponse> getAllByPriority(int page, Integer priority) {
        if (page < 0) {
            throw new InvalidStateException("Page index must be zero or greater");
        }
        if (priority != null && (priority < 1 || priority > 3)) {
            throw new InvalidStateException("priority must be between 1 and 3");
        }

        PageRequest pageRequest = PageRequest.of(page, PageResponse.DEFAULT_SIZE);
        var alerts = priority == null
                ? alertRepository.findAllByOrderByPriorityDescCreatedAtAscIdAsc(pageRequest)
                : alertRepository.findAllByPriorityOrderByCreatedAtAscIdAsc(priority, pageRequest);
        return PageResponse.from(
                alerts.map(alertMapper::toResponse)
        );
    }
}
