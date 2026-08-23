package com.centerops.datastructure;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Directed adjacency-list graph whose edge direction is prerequisite to
 * dependent course.
 */
public final class CourseGraph<T> {

    private final CustomHashTable<T, Vertex<T>> vertices = new CustomHashTable<>();
    private Vertex<T> firstVertex;
    private Vertex<T> lastVertex;
    private int edgeCount;

    public boolean addVertex(T value) {
        Objects.requireNonNull(value, "vertex must not be null");
        if (vertices.containsKey(value)) {
            return false;
        }

        Vertex<T> vertex = new Vertex<>(value);
        vertices.put(value, vertex);
        if (firstVertex == null) {
            firstVertex = vertex;
        } else {
            lastVertex.nextVertex = vertex;
        }
        lastVertex = vertex;
        return true;
    }

    public boolean addEdge(T prerequisite, T dependent) {
        Vertex<T> source = requireVertex(prerequisite);
        Vertex<T> target = requireVertex(dependent);
        if (source.containsNeighbor(dependent)) {
            return false;
        }

        source.appendNeighbor(dependent);
        target.indegree++;
        edgeCount++;
        return true;
    }

    public boolean containsVertex(T value) {
        return vertices.containsKey(value);
    }

    public boolean containsEdge(T prerequisite, T dependent) {
        Vertex<T> source = vertices.get(prerequisite);
        return source != null && source.containsNeighbor(dependent);
    }

    public List<T> vertices() {
        List<T> result = new ArrayList<>(vertexCount());
        Vertex<T> current = firstVertex;
        while (current != null) {
            result.add(current.value);
            current = current.nextVertex;
        }
        return List.copyOf(result);
    }

    public List<T> neighborsOf(T value) {
        Vertex<T> vertex = requireVertex(value);
        List<T> result = new ArrayList<>();
        Neighbor<T> current = vertex.firstNeighbor;
        while (current != null) {
            result.add(current.value);
            current = current.next;
        }
        return List.copyOf(result);
    }

    public int indegreeOf(T value) {
        return requireVertex(value).indegree;
    }

    public int vertexCount() {
        return vertices.size();
    }

    public int edgeCount() {
        return edgeCount;
    }

    private Vertex<T> requireVertex(T value) {
        Objects.requireNonNull(value, "vertex must not be null");
        Vertex<T> vertex = vertices.get(value);
        if (vertex == null) {
            throw new IllegalArgumentException("Unknown graph vertex: " + value);
        }
        return vertex;
    }

    private static final class Vertex<T> {
        private final T value;
        private Vertex<T> nextVertex;
        private Neighbor<T> firstNeighbor;
        private Neighbor<T> lastNeighbor;
        private int indegree;

        private Vertex(T value) {
            this.value = value;
        }

        private boolean containsNeighbor(T value) {
            Neighbor<T> current = firstNeighbor;
            while (current != null) {
                if (current.value.equals(value)) {
                    return true;
                }
                current = current.next;
            }
            return false;
        }

        private void appendNeighbor(T value) {
            Neighbor<T> neighbor = new Neighbor<>(value);
            if (firstNeighbor == null) {
                firstNeighbor = neighbor;
            } else {
                lastNeighbor.next = neighbor;
            }
            lastNeighbor = neighbor;
        }
    }

    private static final class Neighbor<T> {
        private final T value;
        private Neighbor<T> next;

        private Neighbor(T value) {
            this.value = value;
        }
    }
}
