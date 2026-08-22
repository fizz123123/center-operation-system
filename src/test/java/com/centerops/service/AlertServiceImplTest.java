package com.centerops.service;

import com.centerops.dto.response.AlertResponse;
import com.centerops.entity.Alert;
import com.centerops.mapper.AlertMapper;
import com.centerops.repository.AlertRepository;
import com.centerops.service.impl.AlertServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
    void getAllByPriorityShouldReturnFixedTenItemPage() {
        Alert alert = Alert.builder().id(1L).priority(3).message("High priority").build();
        AlertResponse mapped = new AlertResponse(1L, 1L, "Ada", 2L, "Java", 3,
                "High priority", false, null);
        when(alertRepository.findAllByOrderByPriorityDescCreatedAtAsc(any(Pageable.class)))
                .thenAnswer(invocation -> new PageImpl<>(
                        List.of(alert),
                        invocation.getArgument(0),
                        30
                ));
        when(alertMapper.toResponse(alert)).thenReturn(mapped);

        var response = alertService.getAllByPriority(1);

        assertThat(response.content()).containsExactly(mapped);
        assertThat(response.page()).isEqualTo(1);
        assertThat(response.size()).isEqualTo(10);
        assertThat(response.totalElements()).isEqualTo(30);
        assertThat(response.totalPages()).isEqualTo(3);
    }
}
