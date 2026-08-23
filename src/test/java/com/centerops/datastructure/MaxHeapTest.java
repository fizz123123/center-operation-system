package com.centerops.datastructure;

import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MaxHeapTest {

    @Test
    void returnsValuesFromHighestToLowestPriority() {
        MaxHeap<Integer> heap = new MaxHeap<>(Comparator.naturalOrder(), 2);
        heap.insert(1);
        heap.insert(3);
        heap.insert(2);
        heap.insert(3);

        assertThat(heap.peek()).isEqualTo(3);
        assertThat(heap.remove()).isEqualTo(3);
        assertThat(heap.remove()).isEqualTo(3);
        assertThat(heap.remove()).isEqualTo(2);
        assertThat(heap.remove()).isEqualTo(1);
        assertThat(heap.isEmpty()).isTrue();
    }

    @Test
    void rejectsReadingAnEmptyHeap() {
        MaxHeap<Integer> heap = new MaxHeap<>(Comparator.naturalOrder());

        assertThatThrownBy(heap::peek).isInstanceOf(NoSuchElementException.class);
        assertThatThrownBy(heap::remove).isInstanceOf(NoSuchElementException.class);
    }
}
