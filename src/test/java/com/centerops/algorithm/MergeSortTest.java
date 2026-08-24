package com.centerops.algorithm;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MergeSortTest {

    @Test
    void sortsValuesFromSmallestToLargestWithoutChangingInput() {
        int[] input = {5, 3, 8, 1};

        int[] result = MergeSort.sort(input);

        assertThat(result).containsExactly(1, 3, 5, 8);
        assertThat(input).containsExactly(5, 3, 8, 1);
    }

    @Test
    void supportsEmptyAndSingleValueArrays() {
        assertThat(MergeSort.sort(new int[0])).isEmpty();
        assertThat(MergeSort.sort(new int[]{7})).containsExactly(7);
    }

    @Test
    void keepsAlreadySortedValuesInOrder() {
        assertThat(MergeSort.sort(new int[]{1, 2, 3, 4}))
                .containsExactly(1, 2, 3, 4);
    }

    @Test
    void supportsDuplicateAndNegativeValues() {
        assertThat(MergeSort.sort(new int[]{3, -1, 3, 0, -5}))
                .containsExactly(-5, -1, 0, 3, 3);
    }

    @Test
    void rejectsNullInput() {
        assertThatThrownBy(() -> MergeSort.sort(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("values must not be null");
    }
}
