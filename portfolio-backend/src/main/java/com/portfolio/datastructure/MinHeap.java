package com.portfolio.datastructure;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Generic array-based binary min-heap.
 *
 * <p>Complexity: insert O(log n), extractMin O(log n), peekMin O(1).
 * Pass {@link Comparator#reverseOrder()} to get max-heap behaviour.
 */
public class MinHeap<T> {

    private static final int DEFAULT_CAPACITY = 16;

    private Object[] data;
    private int size;
    private final Comparator<T> comparator;

    public MinHeap(Comparator<T> comparator) {
        this.comparator = comparator;
        this.data = new Object[DEFAULT_CAPACITY];
    }

    public void insert(T item) {
        if (item == null) throw new IllegalArgumentException("null items not allowed");
