package com.centerops.datastructure;

import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * 使用陣列保存 Complete Binary Tree 的泛型最大堆積（Max Heap）
 *
 * <p>元素大小由呼叫端提供的 {@link Comparator} 決定。此類別只負責排列元素，
 * 不包含 Alert priority 等業務判斷規則</p>
 *
 * @param <T> Heap 中保存的元素型別
 */
public final class MaxHeap<T> {

    private final Comparator<? super T> comparator;
    private Object[] elements = new Object[10];
    private int size;

    /**
     * 使用指定的比較規則建立空的 Max Heap
     *
     * @param comparator 判斷元素大小的比較器，不可為 {@code null}
     * @throws NullPointerException 當 comparator 為 {@code null}
     */
    public MaxHeap(Comparator<? super T> comparator) {
        this.comparator = Objects.requireNonNull(comparator, "comparator must not be null");
    }

    /**
     * 插入一個元素，並透過向上交換恢復 Max Heap Property
     *
     * @param value 要插入的元素，不可為 {@code null}
     * @throws NullPointerException 當 value 為 {@code null}
     */
    public void insert(T value) {
        Objects.requireNonNull(value, "value must not be null");
        if (size == elements.length) {
            Object[] larger = new Object[elements.length * 2];
            System.arraycopy(elements, 0, larger, 0, elements.length);
            elements = larger;
        }

        elements[size] = value;
        moveUp(size);
        size++;
    }

    /**
     * 查看目前最大的元素，但不將它移除
     *
     * @return Comparator 判定的最大元素
     * @throws NoSuchElementException 當 Heap 為空
     */
    public T peek() {
        checkNotEmpty();
        return valueAt(0);
    }

    /**
     * 移除並回傳目前最大的元素，接著透過向下交換恢復 Max Heap Property
     *
     * @return 被移除的最大元素
     * @throws NoSuchElementException 當 Heap 為空
     */
    public T remove() {
        checkNotEmpty();
        T maximum = valueAt(0);
        size--;
        elements[0] = elements[size];
        elements[size] = null;
        moveDown(0);
        return maximum;
    }

    /**
     * 取得 Heap 中目前保存的元素數量
     *
     * @return 元素數量
     */
    public int size() {
        return size;
    }

    /**
     * 判斷 Heap 是否沒有任何元素
     *
     * @return Heap 為空時回傳 {@code true}，否則回傳 {@code false}
     */
    public boolean isEmpty() {
        return size == 0;
    }

    private void moveUp(int index) {
        while (index > 0) {
            int parent = (index - 1) / 2;
            if (comparator.compare(valueAt(index), valueAt(parent)) <= 0) {
                return;
            }
            swap(index, parent);
            index = parent;
        }
    }

    private void moveDown(int index) {
        while (index * 2 + 1 < size) {
            int left = index * 2 + 1;
            int right = left + 1;
            int largerChild = right < size
                    && comparator.compare(valueAt(right), valueAt(left)) > 0
                    ? right : left;

            if (comparator.compare(valueAt(largerChild), valueAt(index)) <= 0) {
                return;
            }
            swap(index, largerChild);
            index = largerChild;
        }
    }

    private void checkNotEmpty() {
        if (isEmpty()) {
            throw new NoSuchElementException("Heap is empty");
        }
    }

    @SuppressWarnings("unchecked")
    private T valueAt(int index) {
        return (T) elements[index];
    }

    private void swap(int first, int second) {
        Object temporary = elements[first];
        elements[first] = elements[second];
        elements[second] = temporary;
    }
}
