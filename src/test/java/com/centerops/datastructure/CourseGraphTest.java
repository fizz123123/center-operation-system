package com.centerops.datastructure;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CourseGraphTest {

    @Test
    void storesPrerequisiteToDependentEdgesInInsertionOrder() {
        CourseGraph<Long> graph = new CourseGraph<>();
        graph.addVertex(1L);
        graph.addVertex(2L);
        graph.addVertex(3L);

        graph.addEdge(1L, 2L);
        graph.addEdge(1L, 3L);

        assertThat(graph.vertices()).containsExactly(1L, 2L, 3L);
        assertThat(graph.neighborsOf(1L)).containsExactly(2L, 3L);
        assertThat(graph.indegreeOf(1L)).isZero();
        assertThat(graph.indegreeOf(2L)).isEqualTo(1);
        assertThat(graph.edgeCount()).isEqualTo(2);
    }

    @Test
    void rejectsUnknownVerticesAndDuplicateEdges() {
        CourseGraph<Long> graph = new CourseGraph<>();
        graph.addVertex(1L);
        graph.addVertex(2L);

        assertThat(graph.addEdge(1L, 2L)).isTrue();
        assertThat(graph.addEdge(1L, 2L)).isFalse();
        assertThatThrownBy(() -> graph.addEdge(1L, 3L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("3");
    }
}
