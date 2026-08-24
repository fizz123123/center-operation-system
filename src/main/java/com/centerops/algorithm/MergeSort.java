package com.centerops.algorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * 使用 Divide and Conquer 實作的合併排序（Merge Sort）
 *
 * <p>演算法會先將陣列持續分成左右兩半，再把兩個已排序區間合併。此類別不保存
 * 狀態，並提供整數陣列與泛型物件清單版本；兩者都不會修改呼叫端傳入的資料。</p>
 */
public final class MergeSort {

    private MergeSort() {
    }

    /**
     * 將整數陣列由小到大排序，並回傳新的陣列
     *
     * @param values 要排序的整數陣列，不可為 {@code null}
     * @return 排序完成的新陣列；原始陣列不會被修改
     * @throws NullPointerException 當 values 為 {@code null}
     */
    public static int[] sort(int[] values) {
        Objects.requireNonNull(values, "values must not be null");
        int[] sorted = Arrays.copyOf(values, values.length);
        int[] temporary = new int[values.length];
        sortRange(sorted, temporary, 0, sorted.length - 1);
        return sorted;
    }

    /**
     * 使用指定比較規則排序物件清單，並回傳新的清單。
     *
     * <p>合併時比較結果相同會先取左半元素，因此此版本維持 Merge Sort
     * 的 Stable Sort 特性。原始清單不會被修改。</p>
     *
     * @param values 要排序的物件清單，不可為 {@code null}
     * @param comparator 判斷元素順序的比較器，不可為 {@code null}
     * @param <T> 清單元素型別
     * @return 排序完成的新清單
     * @throws NullPointerException 當 values 或 comparator 為 {@code null}
     */
    public static <T> List<T> sort(List<T> values, Comparator<? super T> comparator) {
        Objects.requireNonNull(values, "values must not be null");
        Objects.requireNonNull(comparator, "comparator must not be null");

        List<T> sorted = new ArrayList<>(values);
        List<T> temporary = new ArrayList<>(values);
        sortRange(sorted, temporary, comparator, 0, sorted.size() - 1);
        return sorted;
    }

    private static void sortRange(int[] values, int[] temporary, int left, int right) {
        if (left >= right) {
            return;
        }

        int middle = left + (right - left) / 2;
        sortRange(values, temporary, left, middle);
        sortRange(values, temporary, middle + 1, right);
        merge(values, temporary, left, middle, right);
    }

    private static void merge(int[] values, int[] temporary, int left, int middle, int right) {
        System.arraycopy(values, left, temporary, left, right - left + 1);

        int leftIndex = left;
        int rightIndex = middle + 1;
        int writeIndex = left;

        while (leftIndex <= middle && rightIndex <= right) {
            if (temporary[leftIndex] <= temporary[rightIndex]) {
                values[writeIndex++] = temporary[leftIndex++];
            } else {
                values[writeIndex++] = temporary[rightIndex++];
            }
        }

        while (leftIndex <= middle) {
            values[writeIndex++] = temporary[leftIndex++];
        }

        while (rightIndex <= right) {
            values[writeIndex++] = temporary[rightIndex++];
        }
    }

    private static <T> void sortRange(
            List<T> values,
            List<T> temporary,
            Comparator<? super T> comparator,
            int left,
            int right
    ) {
        if (left >= right) {
            return;
        }

        int middle = left + (right - left) / 2;
        sortRange(values, temporary, comparator, left, middle);
        sortRange(values, temporary, comparator, middle + 1, right);
        merge(values, temporary, comparator, left, middle, right);
    }

    private static <T> void merge(
            List<T> values,
            List<T> temporary,
            Comparator<? super T> comparator,
            int left,
            int middle,
            int right
    ) {
        for (int index = left; index <= right; index++) {
            temporary.set(index, values.get(index));
        }

        int leftIndex = left;
        int rightIndex = middle + 1;
        int writeIndex = left;

        while (leftIndex <= middle && rightIndex <= right) {
            if (comparator.compare(temporary.get(leftIndex), temporary.get(rightIndex)) <= 0) {
                values.set(writeIndex++, temporary.get(leftIndex++));
            } else {
                values.set(writeIndex++, temporary.get(rightIndex++));
            }
        }

        while (leftIndex <= middle) {
            values.set(writeIndex++, temporary.get(leftIndex++));
        }

        while (rightIndex <= right) {
            values.set(writeIndex++, temporary.get(rightIndex++));
        }
    }
}
