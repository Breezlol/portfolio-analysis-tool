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
        // heap should contain the 3 largest: 6, 7, 9
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
