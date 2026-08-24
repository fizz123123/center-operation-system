package com.centerops.service.impl;

import com.centerops.dto.response.AlertResponse;
import com.centerops.dto.response.PageResponse;
import com.centerops.datastructure.MaxHeap;
import com.centerops.entity.Alert;
import com.centerops.exception.InvalidStateException;
import com.centerops.mapper.AlertMapper;
import com.centerops.repository.AlertRepository;
import com.centerops.service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlertServiceImpl implements AlertService {

    private static final Comparator<Alert> ALERT_PRIORITY_COMPARATOR = Comparator
            .comparing(Alert::getPriority)
            .thenComparing(
                    Alert::getCreatedAt,
                    Comparator.nullsFirst(Comparator.reverseOrder())
            )
            .thenComparing(
                    Alert::getId,
                    Comparator.nullsFirst(Comparator.reverseOrder())
            );

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

        List<Alert> alerts = priority == null
                ? alertRepository.findAll()
                : alertRepository.findAllByPriority(priority);
        return createPage(orderByPriority(alerts), page);
    }

    private List<Alert> orderByPriority(List<Alert> alerts) {
        MaxHeap<Alert> heap = new MaxHeap<>(ALERT_PRIORITY_COMPARATOR);
        for (Alert alert : alerts) {
            heap.insert(alert);
        }

        List<Alert> ordered = new ArrayList<>(alerts.size());
        while (!heap.isEmpty()) {
            ordered.add(heap.remove());
        }
        return ordered;
    }

    private PageResponse<AlertResponse> createPage(List<Alert> alerts, int page) {
        int size = PageResponse.DEFAULT_SIZE;
        int totalElements = alerts.size();
        int totalPages = (totalElements + size - 1) / size;
        long offset = (long) page * size;
        int fromIndex = (int) Math.min(offset, totalElements);
        int toIndex = Math.min(fromIndex + size, totalElements);
        List<AlertResponse> content = alerts.subList(fromIndex, toIndex).stream()
                .map(alertMapper::toResponse)
                .toList();

        return new PageResponse<>(
                content,
                page,
                size,
                totalElements,
                totalPages,
                page == 0,
                totalPages == 0 || page >= totalPages - 1
        );
    }
}
