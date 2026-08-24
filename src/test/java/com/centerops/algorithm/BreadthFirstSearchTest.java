package com.centerops.algorithm;

import com.centerops.datastructure.CourseGraph;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BreadthFirstSearchTest {

    @Test
    void visitsReachableVerticesLevelByLevel() {
        CourseGraph<String> graph = branchingGraph();

        assertThat(BreadthFirstSearch.traverse(graph, "Java"))
                .containsExactly("Java", "OOP", "Spring", "Data Structure");
    }

    @Test
    void rejectsUnknownStartVertex() {
        CourseGraph<String> graph = branchingGraph();

        assertThatThrownBy(() -> BreadthFirstSearch.traverse(graph, "Unknown"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown");
    }

    @Test
    void visitsEachVertexOnceWhenGraphContainsCycle() {
        CourseGraph<String> graph = new CourseGraph<>();
        graph.addVertex("A");
        graph.addVertex("B");
        graph.addEdge("A", "B");
        graph.addEdge("B", "A");

        assertThat(BreadthFirstSearch.traverse(graph, "A")).containsExactly("A", "B");
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
