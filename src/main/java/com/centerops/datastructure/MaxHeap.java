package com.centerops.datastructure;

import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.Objects;

/** Array-backed max heap that does not delegate storage to a collection. */
public final class MaxHeap<T> {

    private static final int DEFAULT_CAPACITY = 10;

    private final Comparator<? super T> comparator;
    private Object[] elements;
    private int size;

    public MaxHeap(Comparator<? super T> comparator) {
        this(comparator, DEFAULT_CAPACITY);
    }

    public MaxHeap(Comparator<? super T> comparator, int initialCapacity) {
        this.comparator = Objects.requireNonNull(comparator, "comparator must not be null");
        if (initialCapacity <= 0) {
            throw new IllegalArgumentException("initialCapacity must be greater than zero");
        }
        elements = new Object[initialCapacity];
    }

    public void insert(T value) {
        Objects.requireNonNull(value, "value must not be null");
        ensureCapacity();
        elements[size] = value;
        siftUp(size);
        size++;
    }

    public T peek() {
        ensureNotEmpty();
        return elementAt(0);
    }

    public T remove() {
        ensureNotEmpty();
        T maximum = elementAt(0);
        size--;
        elements[0] = elements[size];
        elements[size] = null;
        if (size > 0) {
            siftDown(0);
        }
        return maximum;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    private void siftUp(int index) {
        int current = index;
        while (current > 0) {
            int parent = (current - 1) / 2;
            if (comparator.compare(elementAt(current), elementAt(parent)) <= 0) {
                return;
            }
            swap(current, parent);
            current = parent;
        }
    }

    private void siftDown(int index) {
        int current = index;
        while (true) {
            int left = current * 2 + 1;
            if (left >= size) {
                return;
            }
            int right = left + 1;
            int largest = right < size
                    && comparator.compare(elementAt(right), elementAt(left)) > 0
                    ? right
                    : left;
            if (comparator.compare(elementAt(largest), elementAt(current)) <= 0) {
                return;
            }
            swap(current, largest);
            current = largest;
        }
    }

    private void ensureCapacity() {
        if (size < elements.length) {
            return;
        }
        Object[] expanded = new Object[elements.length << 1];
        System.arraycopy(elements, 0, expanded, 0, elements.length);
        elements = expanded;
    }

    private void ensureNotEmpty() {
        if (isEmpty()) {
            throw new NoSuchElementException("Heap is empty");
        }
    }

    @SuppressWarnings("unchecked")
    private T elementAt(int index) {
        return (T) elements[index];
    }

    private void swap(int first, int second) {
        Object value = elements[first];
        elements[first] = elements[second];
        elements[second] = value;
    }
}
