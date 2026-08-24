package com.centerops.datastructure;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 使用鄰接串列表示課程先修關係的有向圖
 *
 * <p>邊的方向固定為「先修課程 → 依賴該先修課程的課程」，Graph 只負責保存
 * 頂點、邊、入度，不負責 BFS、DFS、Cycle Detection 或 Topological Sort</p>
 *
 * @param <T> 圖中頂點的型別，例如課程 ID
 */
public final class CourseGraph<T> {

    private final List<T> vertices = new ArrayList<>();
    private final CustomHashTable<T, List<T>> adjacency = new CustomHashTable<>();
    private final CustomHashTable<T, Integer> indegrees = new CustomHashTable<>();
    private int edgeCount;

    /**
     * 建立一個不包含任何頂點或邊的空課程圖
     */
    public CourseGraph() {
    }

    /**
     * 新增一個頂點，並建立空的鄰接串列與初始入度 0
     *
     * @param vertex 要新增的頂點，不可為 {@code null}
     * @return 新增成功時回傳 {@code true}；頂點已存在時回傳 {@code false}
     * @throws NullPointerException 當 vertex 為 {@code null}
     */
    public boolean addVertex(T vertex) {
        Objects.requireNonNull(vertex, "vertex must not be null");
        if (adjacency.containsKey(vertex)) {
            return false;
        }
        vertices.add(vertex);
        adjacency.put(vertex, new ArrayList<>());
        indegrees.put(vertex, 0);
        return true;
    }

    /**
     * 新增一條「先修課程 → 依賴課程」的有向邊
     *
     * <p>兩個頂點都必須先透過 {@link #addVertex(Object)} 加入圖中，本方法只保存
     * 關係，不檢查加入後是否形成 cycle</p>
     *
     * @param prerequisite 邊的起點，也就是先修課程
     * @param dependent 邊的終點，也就是依賴該先修課程的課程
     * @return 新增成功時回傳 {@code true}；相同邊已存在時回傳 {@code false}
     * @throws NullPointerException 當任一頂點為 {@code null}
     * @throws IllegalArgumentException 當任一頂點尚未加入圖中
     */
    public boolean addEdge(T prerequisite, T dependent) {
        List<T> neighbors = requireNeighbors(prerequisite);
        requireNeighbors(dependent);
        if (neighbors.contains(dependent)) {
            return false;
        }
        neighbors.add(dependent);
        indegrees.put(dependent, indegrees.get(dependent) + 1);
        edgeCount++;
        return true;
    }

    /**
     * 判斷圖中是否存在指定頂點
     *
     * @param vertex 要查詢的頂點，不可為 {@code null}
     * @return 頂點存在時回傳 {@code true}，否則回傳 {@code false}
     * @throws NullPointerException 當 vertex 為 {@code null}
     */
    public boolean containsVertex(T vertex) {
        return adjacency.containsKey(vertex);
    }

    /**
     * 判斷圖中是否存在指定方向的邊
     *
     * @param prerequisite 邊的起點
     * @param dependent 邊的終點
     * @return 邊存在時回傳 {@code true}，否則回傳 {@code false}
     */
    public boolean containsEdge(T prerequisite, T dependent) {
        List<T> neighbors = adjacency.get(prerequisite);
        return neighbors != null && neighbors.contains(dependent);
    }

    /**
     * 依照加入順序取得圖中所有頂點
     *
     * @return 不可修改的頂點清單
     */
    public List<T> vertices() {
        return List.copyOf(vertices);
    }

    /**
     * 取得指定頂點直接指向的所有鄰居
     *
     * @param vertex 要查詢的頂點
     * @return 不可修改的鄰居清單，順序與邊的加入順序相同
     * @throws NullPointerException 當 vertex 為 {@code null}
     * @throws IllegalArgumentException 當頂點不存在
     */
    public List<T> neighborsOf(T vertex) {
        return List.copyOf(requireNeighbors(vertex));
    }

    /**
     * 取得指定頂點的入度，也就是指向該頂點的邊數
     *
     * @param vertex 要查詢的頂點
     * @return 頂點的入度
     * @throws NullPointerException 當 vertex 為 {@code null}
     * @throws IllegalArgumentException 當頂點不存在
     */
    public int indegreeOf(T vertex) {
        requireNeighbors(vertex);
        return indegrees.get(vertex);
    }

    /**
     * 取得圖中的頂點數量
     *
     * @return 頂點數量
     */
    public int vertexCount() {
        return vertices.size();
    }

    /**
     * 取得圖中的有向邊數量
     *
     * @return 邊的數量
     */
    public int edgeCount() {
        return edgeCount;
    }

    private List<T> requireNeighbors(T vertex) {
        Objects.requireNonNull(vertex, "vertex must not be null");
        List<T> neighbors = adjacency.get(vertex);
        if (neighbors == null) {
            throw new IllegalArgumentException("Unknown graph vertex: " + vertex);
        }
        return neighbors;
    }
}
