package com.centerops.algorithm;

import com.centerops.datastructure.CourseGraph;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TopologicalSortTest {

    @Test
    void placesEveryPrerequisiteBeforeItsDependentCourse() {
        CourseGraph<String> graph = branchingGraph();

        List<String> result = TopologicalSort.sort(graph);

        assertThat(result).containsExactlyInAnyOrder(
                "Java", "OOP", "Spring", "Data Structure", "Database"
        );
        assertThat(result.indexOf("Java")).isLessThan(result.indexOf("OOP"));
        assertThat(result.indexOf("Java")).isLessThan(result.indexOf("Spring"));
        assertThat(result.indexOf("OOP")).isLessThan(result.indexOf("Data Structure"));
        assertThat(result.indexOf("Spring")).isLessThan(result.indexOf("Data Structure"));
    }

    @Test
    void includesDisconnectedVerticesAndSupportsEmptyGraph() {
        CourseGraph<String> graph = branchingGraph();

        assertThat(TopologicalSort.sort(graph)).contains("Database");
        assertThat(TopologicalSort.sort(new CourseGraph<>())).isEmpty();
    }

    @Test
    void detectsCycleAndRejectsTopologicalSort() {
        CourseGraph<String> graph = new CourseGraph<>();
        graph.addVertex("A");
        graph.addVertex("B");
        graph.addVertex("C");
        graph.addEdge("A", "B");
        graph.addEdge("B", "C");
        graph.addEdge("C", "A");

        assertThat(TopologicalSort.hasCycle(graph)).isTrue();
        assertThatThrownBy(() -> TopologicalSort.sort(graph))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cycle");
    }

    private CourseGraph<String> branchingGraph() {
        CourseGraph<String> graph = new CourseGraph<>();
        graph.addVertex("Java");
        graph.addVertex("OOP");
        graph.addVertex("Spring");
        graph.addVertex("Data Structure");
        graph.addVertex("Database");
        graph.addEdge("Java", "OOP");
        graph.addEdge("Java", "Spring");
        graph.addEdge("OOP", "Data Structure");
        graph.addEdge("Spring", "Data Structure");
        return graph;
    }
}
