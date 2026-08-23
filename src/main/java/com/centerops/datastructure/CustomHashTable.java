package com.centerops.datastructure;

import java.util.Objects;

/**
 * A small separate-chaining hash table used by the algorithm module.
 *
 * <p>The implementation intentionally owns its bucket storage instead of
 * delegating to a Java collection.</p>
 */
public final class CustomHashTable<K, V> {

    private static final int DEFAULT_CAPACITY = 16;
    private static final float LOAD_FACTOR = 0.75F;

    private Node<K, V>[] buckets;
    private int size;
    private int resizeThreshold;

    public CustomHashTable() {
        this(DEFAULT_CAPACITY);
    }

    @SuppressWarnings("unchecked")
    public CustomHashTable(int initialCapacity) {
        if (initialCapacity <= 0) {
            throw new IllegalArgumentException("initialCapacity must be greater than zero");
        }
        int capacity = tableSizeFor(initialCapacity);
        buckets = (Node<K, V>[]) new Node[capacity];
        resizeThreshold = threshold(capacity);
    }

    public V put(K key, V value) {
        Objects.requireNonNull(key, "key must not be null");
        int index = bucketIndex(key, buckets.length);
        Node<K, V> current = buckets[index];
        while (current != null) {
            if (current.key.equals(key)) {
                V previous = current.value;
                current.value = value;
                return previous;
            }
            current = current.next;
        }

        buckets[index] = new Node<>(key, value, buckets[index]);
        size++;
        if (size > resizeThreshold) {
            resize();
        }
        return null;
    }

    public V get(K key) {
        Node<K, V> node = findNode(key);
        return node == null ? null : node.value;
    }

    public boolean containsKey(K key) {
        return findNode(key) != null;
    }

    public V remove(K key) {
        Objects.requireNonNull(key, "key must not be null");
        int index = bucketIndex(key, buckets.length);
        Node<K, V> current = buckets[index];
        Node<K, V> previous = null;

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

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void clear() {
        @SuppressWarnings("unchecked")
        Node<K, V>[] emptyBuckets = (Node<K, V>[]) new Node[buckets.length];
        buckets = emptyBuckets;
        size = 0;
    }

    private Node<K, V> findNode(K key) {
        Objects.requireNonNull(key, "key must not be null");
        Node<K, V> current = buckets[bucketIndex(key, buckets.length)];
        while (current != null) {
            if (current.key.equals(key)) {
                return current;
            }
            current = current.next;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private void resize() {
        Node<K, V>[] oldBuckets = buckets;
        buckets = (Node<K, V>[]) new Node[oldBuckets.length << 1];
        resizeThreshold = threshold(buckets.length);

        for (Node<K, V> bucket : oldBuckets) {
            Node<K, V> current = bucket;
            while (current != null) {
                Node<K, V> next = current.next;
                int index = bucketIndex(current.key, buckets.length);
                current.next = buckets[index];
                buckets[index] = current;
                current = next;
            }
        }
    }

    private int bucketIndex(K key, int capacity) {
        int hash = key.hashCode();
        hash ^= hash >>> 16;
        return hash & (capacity - 1);
    }

    private int threshold(int capacity) {
        return Math.max(1, (int) (capacity * LOAD_FACTOR));
    }

    private int tableSizeFor(int requestedCapacity) {
        int capacity = 1;
        while (capacity < requestedCapacity) {
            capacity <<= 1;
        }
        return capacity;
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
