package com.centerops.service;

import com.centerops.dto.response.AlertResponse;
import com.centerops.entity.Alert;
import com.centerops.exception.InvalidStateException;
import com.centerops.mapper.AlertMapper;
import com.centerops.repository.AlertRepository;
import com.centerops.service.impl.AlertServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlertServiceImplTest {

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private AlertMapper alertMapper;

    @InjectMocks
    private AlertServiceImpl alertService;

    @Test
    void getAllByPriorityShouldUseMaxHeapOrderBeforePaging() {
        Alert low = alert(1L, 1, LocalDateTime.of(2026, 1, 1, 10, 0));
        Alert newerHigh = alert(2L, 3, LocalDateTime.of(2026, 2, 1, 10, 0));
        Alert olderHighWithSmallerId = alert(3L, 3, LocalDateTime.of(2026, 1, 1, 10, 0));
        Alert olderHighWithLargerId = alert(4L, 3, LocalDateTime.of(2026, 1, 1, 10, 0));
        when(alertRepository.findAll()).thenReturn(List.of(
                low,
                newerHigh,
                olderHighWithLargerId,
                olderHighWithSmallerId
        ));
        when(alertMapper.toResponse(low)).thenReturn(response(low));
        when(alertMapper.toResponse(newerHigh)).thenReturn(response(newerHigh));
        when(alertMapper.toResponse(olderHighWithSmallerId)).thenReturn(response(olderHighWithSmallerId));
        when(alertMapper.toResponse(olderHighWithLargerId)).thenReturn(response(olderHighWithLargerId));

        var page = alertService.getAllByPriority(0, null);

        assertThat(page.content())
                .extracting(AlertResponse::id)
                .containsExactly(3L, 4L, 2L, 1L);
        assertThat(page.page()).isZero();
        assertThat(page.size()).isEqualTo(10);
        assertThat(page.totalElements()).isEqualTo(4);
        assertThat(page.totalPages()).isEqualTo(1);
    }

    @Test
    void getAllByPriorityShouldFilterBeforePaging() {
        Alert alert = alert(1L, 3, LocalDateTime.of(2026, 1, 1, 10, 0));
        AlertResponse mapped = response(alert);
        when(alertRepository.findAllByPriority(3)).thenReturn(List.of(alert));
        when(alertMapper.toResponse(alert)).thenReturn(mapped);

        var page = alertService.getAllByPriority(0, 3);

        assertThat(page.content()).containsExactly(mapped);
        assertThat(page.totalElements()).isEqualTo(1);
        verify(alertRepository).findAllByPriority(3);
    }

    @Test
    void getAllByPriorityShouldPageAfterOrderingAllMatchingAlerts() {
        List<Alert> alerts = java.util.stream.LongStream.rangeClosed(1, 12)
                .mapToObj(id -> alert(id, id <= 2 ? 1 : 3, LocalDateTime.of(2026, 1, 1, 10, 0)))
                .toList();
        when(alertRepository.findAll()).thenReturn(alerts);
        when(alertMapper.toResponse(alerts.get(0))).thenReturn(response(alerts.get(0)));
        when(alertMapper.toResponse(alerts.get(1))).thenReturn(response(alerts.get(1)));

        var page = alertService.getAllByPriority(1, null);

        assertThat(page.content()).hasSize(2);
        assertThat(page.content())
                .extracting(AlertResponse::priority)
                .containsOnly(1);
        assertThat(page.totalElements()).isEqualTo(12);
        assertThat(page.totalPages()).isEqualTo(2);
        assertThat(page.first()).isFalse();
        assertThat(page.last()).isTrue();
    }

    @Test
    void getAllByPriorityShouldRejectPriorityOutsideSupportedRange() {
        assertThatThrownBy(() -> alertService.getAllByPriority(0, 4))
                .isInstanceOf(InvalidStateException.class)
                .hasMessageContaining("between 1 and 3");

        verify(alertRepository, never()).findAll();
    }

    private Alert alert(Long id, int priority, LocalDateTime createdAt) {
        return Alert.builder()
                .id(id)
                .priority(priority)
                .message("Alert " + id)
                .createdAt(createdAt)
                .build();
    }

    private AlertResponse response(Alert alert) {
        return new AlertResponse(
                alert.getId(),
                1L,
                "Ada",
                2L,
                "Java",
                alert.getPriority(),
                alert.getMessage(),
                false,
                alert.getCreatedAt()
        );
    }
}
