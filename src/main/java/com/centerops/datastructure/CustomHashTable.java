package com.centerops.datastructure;

import java.util.Objects;

/**
 * 使用分離鏈結法（Separate Chaining）處理碰撞的簡易雜湊表
 *
 * <p>此類別自行管理 bucket 陣列、碰撞鏈與擴容，不委派給 Java {@code HashMap}。</p>
 *
 * @param <K> key 的型別
 * @param <V> value 的型別
 */
public final class CustomHashTable<K, V> {

    private static final int DEFAULT_CAPACITY = 16;
    private static final double MAX_LOAD_FACTOR = 0.75;
    private static final int RESIZE_MULTIPLIER = 2;

    private Node<K, V>[] buckets;
    private int size;

    /**
     * 使用預設容量 16 建立空的雜湊表
     */
    public CustomHashTable() {
        this(DEFAULT_CAPACITY);
    }

    /**
     * 使用指定的 bucket 容量建立空的雜湊表
     *
     * @param capacity 初始 bucket 數量，必須大於 0
     * @throws IllegalArgumentException 當 capacity 小於或等於 0
     */
    @SuppressWarnings("unchecked")
    public CustomHashTable(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be greater than zero");
        }
        buckets = (Node<K, V>[]) new Node[capacity];
    }

    /**
     * 新增一組 key-value；若 key 已存在，則以新 value 取代舊 value
     *
     * @param key   用來計算 bucket 位置的 key，不可為 {@code null}
     * @param value 與 key 對應的 value，可以為 {@code null}
     * @return key 原本對應的 value；若是新 key，則回傳 {@code null}
     * @throws NullPointerException 當 key 為 {@code null}
     */
    public V put(K key, V value) {
        Objects.requireNonNull(key, "key must not be null");
        int index = bucketIndex(key);

        for (Node<K, V> node = buckets[index]; node != null; node = node.next) {
            if (node.key.equals(key)) {
                V oldValue = node.value;
                node.value = value;
                return oldValue;
            }
        }

        buckets[index] = new Node<>(key, value, buckets[index]);
        size++;
        if (loadFactorExceeded()) {
            resizeBuckets();
        }
        return null;
    }

    /**
     * 取得指定 key 對應的 value
     *
     * <p>key 不存在或對應的 value 本身為 {@code null} 時都會回傳 {@code null}；
     * 若要區分這兩種情況，請搭配 {@link #containsKey(Object)}。</p>
     *
     * @param key 要查詢的 key，不可為 {@code null}
     * @return key 對應的 value；找不到時回傳 {@code null}
     * @throws NullPointerException 當 key 為 {@code null}
     */
    public V get(K key) {
        Node<K, V> node = findNode(key);
        return node == null ? null : node.value;
    }

    /**
     * 判斷指定 key 是否存在於雜湊表中
     *
     * @param key 要查詢的 key，不可為 {@code null}
     * @return key 存在時回傳 {@code true}，否則回傳 {@code false}
     * @throws NullPointerException 當 key 為 {@code null}
     */
    public boolean containsKey(K key) {
        return findNode(key) != null;
    }

    /**
     * 刪除指定 key 及其對應的 value
     *
     * @param key 要刪除的 key，不可為 {@code null}
     * @return 被刪除的 value；key 不存在時回傳 {@code null}
     * @throws NullPointerException 當 key 為 {@code null}
     */
    public V remove(K key) {
        Objects.requireNonNull(key, "key must not be null");
        int index = bucketIndex(key);
        Node<K, V> previous = null;
        Node<K, V> current = buckets[index];

        while (current != null) {
            if (current.key.equals(key)) {
                if (previous == null) {
                    buckets[index] = current.next;
                } else {
                    previous.next = current.next;
                }
                size--;
                return current.value;
            }
            previous = current;
            current = current.next;
        }
        return null;
    }

    /**
     * 取得目前保存的 key-value 數量
     *
     * @return 資料筆數
     */
    public int size() {
        return size;
    }

    private Node<K, V> findNode(K key) {
        Objects.requireNonNull(key, "key must not be null");
        int index = bucketIndex(key);
        for (Node<K, V> node = buckets[index]; node != null; node = node.next) {
            if (node.key.equals(key)) {
                return node;
            }
        }
        return null;
    }

    private boolean loadFactorExceeded() {
        return (double) size / buckets.length > MAX_LOAD_FACTOR;
    }

    @SuppressWarnings("unchecked")
    private void resizeBuckets() {
        Node<K, V>[] oldBuckets = buckets;
        buckets = (Node<K, V>[]) new Node[oldBuckets.length * RESIZE_MULTIPLIER];
        size = 0;

        //Bucket index 與 Capacity 有關，因此擴容後所有資料都必須重新計算 index 位置
        for (Node<K, V> bucket : oldBuckets) {
            for (Node<K, V> node = bucket; node != null; node = node.next) {
                put(node.key, node.value);
            }
        }
    }

    private int bucketIndex(K key) {
        int hashValue = key.hashCode();
        return Math.floorMod(hashValue, buckets.length);
    }

    private static final class Node<K, V> {
        private final K key;
        private V value;
        private Node<K, V> next;

        private Node(K key, V value, Node<K, V> next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }
}
