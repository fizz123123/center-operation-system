package com.centerops.dto.response;

import java.util.List;

public record CourseGraphResponse(
        List<CourseNodeResponse> nodes,
        List<CourseEdgeResponse> edges,
        List<Long> topologicalOrder
) {
    public CourseGraphResponse {
        nodes = List.copyOf(nodes);
        edges = List.copyOf(edges);
        topologicalOrder = List.copyOf(topologicalOrder);
    }
}
