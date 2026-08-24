package com.centerops.algorithm;

import java.util.Arrays;
import java.util.Objects;

/**
 * 使用 Divide and Conquer 實作的整數合併排序（Merge Sort）
 *
 * <p>演算法會先將陣列持續分成左右兩半，再把兩個已排序區間合併。此類別不保存
 * 狀態，且不會修改呼叫端傳入的原始陣列</p>
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
}
