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
        if (size == data.length) data = Arrays.copyOf(data, data.length * 2);
        data[size++] = item;
        siftUp(size - 1);
    }

    public T extractMin() {
        if (size == 0) throw new IllegalStateException("heap is empty");
        @SuppressWarnings("unchecked") T min = (T) data[0];
        data[0] = data[--size];
        data[size] = null;
        if (size > 0) siftDown(0);
        return min;
    }

    public T peekMin() {
        if (size == 0) throw new IllegalStateException("heap is empty");
        @SuppressWarnings("unchecked") T top = (T) data[0];
        return top;
    }

    public int size() { return size; }
    public boolean isEmpty() { return size == 0; }

    private void siftUp(int i) {
        while (i > 0) {
            int parent = (i - 1) / 2;
