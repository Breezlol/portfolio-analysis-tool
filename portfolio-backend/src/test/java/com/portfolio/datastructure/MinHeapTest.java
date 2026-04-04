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
