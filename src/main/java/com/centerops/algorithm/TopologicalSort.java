package com.centerops.algorithm;

import com.centerops.datastructure.CourseGraph;
import com.centerops.datastructure.CustomHashTable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;

/**
 * 使用 Kahn's Algorithm 對有向無環圖進行拓樸排序
 *
 * <p>在課程圖中，排序結果保證先修課程出現在依賴課程之前。結果不保證唯一，
 * 也不代表結果中相鄰的兩個頂點一定有直接邊</p>
 */
public final class TopologicalSort {

    private TopologicalSort() {
    }

    /**
     * 依照先修關係產生一組合法的拓樸順序
     *
     * <p>本方法只修改 indegree 的副本，不會改變傳入的 CourseGraph。若 Graph
     * 含有 cycle，將無法處理全部頂點並拋出例外</p>
     *
     * @param graph 要排序的課程圖，不可為 {@code null}
     * @param <T> 圖中頂點的型別
     * @return 不可修改的拓樸排序結果；空圖會回傳空清單
     * @throws NullPointerException 當 graph 為 {@code null}
     * @throws IllegalStateException 當 graph 含有 cycle
     */
    public static <T> List<T> sort(CourseGraph<T> graph) {
        List<T> result = new ArrayList<>();
        for (List<T> stage : sortByStages(graph)) {
            result.addAll(stage);
        }
        return List.copyOf(result);
    }

    /**
     * 依照先修關係將課程分成可平行學習的拓樸階段
     *
     * <p>每一輪會取出當下所有 indegree 為 0 的頂點作為同一階段，再一起移除
     * 它們的出邊。因此同一階段的課程彼此沒有尚未完成的先修依賴，下一階段
     * 則只會在必要的前置階段完成後出現。</p>
     *
     * @param graph 要分組的課程圖，不可為 {@code null}
     * @param <T> 圖中頂點的型別
     * @return 不可修改的階段清單；內層清單同樣不可修改
     * @throws NullPointerException 當 graph 為 {@code null}
     * @throws IllegalStateException 當 graph 含有 cycle
     */
    public static <T> List<List<T>> sortByStages(CourseGraph<T> graph) {
        Objects.requireNonNull(graph, "graph must not be null");

        List<List<T>> stages = new ArrayList<>();
        Queue<T> ready = new ArrayDeque<>();
        CustomHashTable<T, Integer> remainingIndegrees = new CustomHashTable<>();
        int processedCount = 0;

        for (T vertex : graph.vertices()) {
            int indegree = graph.indegreeOf(vertex);
            remainingIndegrees.put(vertex, indegree);
            if (indegree == 0) {
                ready.add(vertex);
            }
        }

        while (!ready.isEmpty()) {
            int stageSize = ready.size();
            List<T> stage = new ArrayList<>(stageSize);

            for (int index = 0; index < stageSize; index++) {
                T current = ready.remove();
                stage.add(current);
                processedCount++;

                for (T dependent : graph.neighborsOf(current)) {
                    int remaining = remainingIndegrees.get(dependent) - 1;
                    remainingIndegrees.put(dependent, remaining);
                    if (remaining == 0) {
                        ready.add(dependent);
                    }
                }
            }

            stages.add(List.copyOf(stage));
        }

        if (processedCount != graph.vertexCount()) {
            throw new IllegalStateException("Course graph contains a cycle");
        }
        return List.copyOf(stages);
    }

    /**
     * 判斷 Graph 是否含有 cycle
     *
     * @param graph 要檢查的課程圖，不可為 {@code null}
     * @param <T> 圖中頂點的型別
     * @return 含有 cycle 時回傳 {@code true}，否則回傳 {@code false}
     * @throws NullPointerException 當 graph 為 {@code null}
     */
    public static <T> boolean hasCycle(CourseGraph<T> graph) {
        try {
            sort(graph);
            return false;
        } catch (IllegalStateException exception) {
            return true;
        }
    }
}
