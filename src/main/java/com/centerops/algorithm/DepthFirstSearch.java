package com.centerops.algorithm;

import com.centerops.datastructure.CourseGraph;
import com.centerops.datastructure.CustomHashTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 使用遞迴沿單一路徑深入走訪 CourseGraph 的深度優先搜尋（Depth-First Search）
 *
 * <p>此類別不保存狀態，可用於產生 DFS 順序或判斷兩個頂點之間是否可到達</p>
 */
public final class DepthFirstSearch {

    private DepthFirstSearch() {
    }

    /**
     * 從指定起點開始，以深度優先方式走訪可到達的所有頂點
     *
     * <p>鄰居的探索順序取決於 Graph 中鄰接邊的加入順序，不與起點連通的頂點
     * 不會出現在結果中</p>
     *
     * @param graph 要走訪的課程圖，不可為 {@code null}
     * @param start 走訪起點，不可為 {@code null}，且必須存在於 graph
     * @param <T> 圖中頂點的型別
     * @return 不可修改的 DFS 走訪順序
     * @throws NullPointerException 當 graph 或 start 為 {@code null}
     * @throws IllegalArgumentException 當 start 不存在於 graph
     */
    public static <T> List<T> traverse(CourseGraph<T> graph, T start) {
        validateVertex(graph, start, "start");
        List<T> result = new ArrayList<>();
        CustomHashTable<T, Boolean> visited = new CustomHashTable<>();
        visit(graph, start, visited, result);
        return List.copyOf(result);
    }

    /**
     * 判斷能否沿著有向邊從 start 到達 target
     *
     * <p>當 start 與 target 相同時視為可到達，此方法可在新增先修邊前協助判斷
     * 是否可能形成 cycle</p>
     *
     * @param graph 要搜尋的課程圖，不可為 {@code null}
     * @param start 搜尋起點，必須存在於 graph
     * @param target 目標頂點，必須存在於 graph
     * @param <T> 圖中頂點的型別
     * @return target 可由 start 到達時回傳 {@code true}，否則回傳 {@code false}
     * @throws NullPointerException 當 graph、start 或 target 為 {@code null}
     * @throws IllegalArgumentException 當 start 或 target 不存在於 graph
     */
    public static <T> boolean isReachable(CourseGraph<T> graph, T start, T target) {
        validateVertex(graph, start, "start");
        validateVertex(graph, target, "target");
        return search(graph, start, target, new CustomHashTable<>());
    }

    private static <T> void visit(
            CourseGraph<T> graph,
            T current,
            CustomHashTable<T, Boolean> visited,
            List<T> result
    ) {
        visited.put(current, true);
        result.add(current);

        for (T neighbor : graph.neighborsOf(current)) {
            if (!visited.containsKey(neighbor)) {
                visit(graph, neighbor, visited, result);
            }
        }
    }

    private static <T> boolean search(
            CourseGraph<T> graph,
            T current,
            T target,
            CustomHashTable<T, Boolean> visited
    ) {
        if (current.equals(target)) {
            return true;
        }

        visited.put(current, true);
        for (T neighbor : graph.neighborsOf(current)) {
            if (!visited.containsKey(neighbor) && search(graph, neighbor, target, visited)) {
                return true;
            }
        }
        return false;
    }

    private static <T> void validateVertex(CourseGraph<T> graph, T vertex, String parameterName) {
        Objects.requireNonNull(graph, "graph must not be null");
        Objects.requireNonNull(vertex, parameterName + " must not be null");
        if (!graph.containsVertex(vertex)) {
            throw new IllegalArgumentException("Unknown " + parameterName + " vertex: " + vertex);
        }
    }
}
