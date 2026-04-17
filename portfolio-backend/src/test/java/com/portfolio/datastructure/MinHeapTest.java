package com.portfolio.datastructure;

import org.junit.jupiter.api.Test;

import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;

class MinHeapTest {

    @Test
    void extractMinReturnsAscendingOrder() {
        MinHeap<Integer> heap = new MinHeap<Integer>(Comparator.naturalOrder());
        heap.insert(5); heap.insert(1); heap.insert(3);
        assertEquals(1, heap.extractMin());
        assertEquals(3, heap.extractMin());
        assertEquals(5, heap.extractMin());
    }

    @Test
    void peekMinDoesNotRemoveElement() {
        MinHeap<Integer> heap = new MinHeap<Integer>(Comparator.naturalOrder());
        heap.insert(7); heap.insert(2);
        assertEquals(2, heap.peekMin());
        assertEquals(2, heap.peekMin());
        assertEquals(2, heap.size());
    }

    @Test
    void topKAlgorithmSelectsLargestK() {
        MinHeap<Integer> heap = new MinHeap<Integer>(Comparator.naturalOrder());
        int k = 3;
        for (int v : new int[]{4, 1, 7, 9, 2, 6}) {
            heap.insert(v);
            if (heap.size() > k) heap.extractMin();
        }
        assertEquals(3, heap.size());
        assertEquals(6, heap.extractMin());
        assertEquals(7, heap.extractMin());
        assertEquals(9, heap.extractMin());
    }

    @Test
    void reversedComparatorGivesMaxHeap() {
        MinHeap<Integer> heap = new MinHeap<Integer>(Comparator.reverseOrder());
        heap.insert(3); heap.insert(8); heap.insert(1);
        assertEquals(8, heap.extractMin());
    }

    @Test
    void extractMinOnEmptyHeapThrows() {
        MinHeap<Integer> heap = new MinHeap<Integer>(Comparator.naturalOrder());
        assertThrows(IllegalStateException.class, heap::extractMin);
    }

    @Test
    void insertNullThrows() {
        MinHeap<Integer> heap = new MinHeap<Integer>(Comparator.naturalOrder());
        assertThrows(IllegalArgumentException.class, () -> heap.insert(null));
    }

    @Test
    void heapGrowsBeyondDefaultCapacity() {
        MinHeap<Integer> heap = new MinHeap<Integer>(Comparator.naturalOrder());
        for (int i = 20; i >= 1; i--) heap.insert(i);
        assertEquals(1, heap.extractMin());
        assertEquals(19, heap.size());
    }
}
