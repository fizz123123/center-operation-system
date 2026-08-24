package com.centerops.algorithm;

import com.centerops.datastructure.CourseGraph;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DepthFirstSearchTest {

    @Test
    void followsOneBranchBeforeVisitingTheNextBranch() {
        CourseGraph<String> graph = branchingGraph();

        assertThat(DepthFirstSearch.traverse(graph, "Java"))
                .containsExactly("Java", "OOP", "Data Structure", "Spring");
    }

    @Test
    void determinesWhetherTargetIsReachable() {
        CourseGraph<String> graph = branchingGraph();

        assertThat(DepthFirstSearch.isReachable(graph, "Java", "Data Structure")).isTrue();
        assertThat(DepthFirstSearch.isReachable(graph, "OOP", "Spring")).isFalse();
        assertThat(DepthFirstSearch.isReachable(graph, "Database", "Database")).isTrue();
    }

    @Test
    void visitsEachVertexOnceWhenGraphContainsCycle() {
        CourseGraph<String> graph = new CourseGraph<>();
        graph.addVertex("A");
        graph.addVertex("B");
        graph.addEdge("A", "B");
        graph.addEdge("B", "A");

        assertThat(DepthFirstSearch.traverse(graph, "A")).containsExactly("A", "B");
        assertThat(DepthFirstSearch.isReachable(graph, "B", "A")).isTrue();
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
