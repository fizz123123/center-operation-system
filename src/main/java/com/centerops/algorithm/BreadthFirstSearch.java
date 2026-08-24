package com.centerops.algorithm;

import com.centerops.datastructure.CourseGraph;
import com.centerops.datastructure.CustomHashTable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;

/**
 * 使用 Queue 逐層走訪 CourseGraph 的廣度優先搜尋（Breadth-First Search）
 *
 * <p>此類別不保存狀態；每次呼叫都會建立獨立的 visited、queue 與結果清單</p>
 */
public final class BreadthFirstSearch {

    private BreadthFirstSearch() {
    }

    /**
     * 從指定起點開始，依照與起點距離由近到遠走訪可到達的所有頂點
     *
     * <p>同一層的頂點順序取決於 Graph 中鄰接邊的加入順序，不與起點連通的
     * 頂點不會出現在結果中</p>
     *
     * @param graph 要走訪的課程圖，不可為 {@code null}
     * @param start 走訪起點，不可為 {@code null}，且必須存在於 graph
     * @param <T> 圖中頂點的型別
     * @return 不可修改的 BFS 走訪順序結果
     * @throws NullPointerException 當 graph 或 start 為 {@code null}
     * @throws IllegalArgumentException 當 start 不存在於 graph
     */
    public static <T> List<T> traverse(CourseGraph<T> graph, T start) {
        validateStart(graph, start);

        List<T> result = new ArrayList<>();
        Queue<T> queue = new ArrayDeque<>();
        CustomHashTable<T, Boolean> visited = new CustomHashTable<>();

        queue.add(start);
        visited.put(start, true);

        while (!queue.isEmpty()) {
            T current = queue.remove();
            result.add(current);

            for (T neighbor : graph.neighborsOf(current)) {
                if (!visited.containsKey(neighbor)) {
                    visited.put(neighbor, true);
                    queue.add(neighbor);
                }
            }
        }
        return List.copyOf(result);
    }

    private static <T> void validateStart(CourseGraph<T> graph, T start) {
        Objects.requireNonNull(graph, "graph must not be null");
        Objects.requireNonNull(start, "start must not be null");
        if (!graph.containsVertex(start)) {
            throw new IllegalArgumentException("Unknown start vertex: " + start);
        }
    }
}
