package com.portfolio.datastructure;

import com.portfolio.entity.PortfolioItem;
import java.util.ArrayList;
import java.util.List;

/**
 * Self-balancing AVL tree keyed on {@link PortfolioItem#getSymbol()}.
 *
 * <p>Complexity: insert O(log n), getItemsSorted O(n),
 * findRange O(log n + k) where k is result size.
 */
public class AVLTree {

    private Node root;

    private class Node {
        PortfolioItem item;
        Node left, right;
        int height;
        Node(PortfolioItem item) { this.item = item; this.height = 1; }
    }

    private int height(Node n) { return n == null ? 0 : n.height; }

    private int getBalance(Node n) { return n == null ? 0 : height(n.left) - height(n.right); }

    private Node rotateRight(Node y) {
        Node x = y.left;
        y.left = x.right;
        x.right = y;
        y.height = 1 + Math.max(height(y.left), height(y.right));
        x.height = 1 + Math.max(height(x.left), height(x.right));
        return x;
    }

    private Node rotateLeft(Node x) {
        Node y = x.right;
        x.right = y.left;
        y.left = x;
        x.height = 1 + Math.max(height(x.left), height(x.right));
        y.height = 1 + Math.max(height(y.left), height(y.right));
        return y;
    }

    private Node balance(Node n) {
        n.height = 1 + Math.max(height(n.left), height(n.right));
        int bf = getBalance(n);
        if (bf > 1) {
            if (getBalance(n.left) < 0) n.left = rotateLeft(n.left);
            return rotateRight(n);
        }
        if (bf < -1) {
            if (getBalance(n.right) > 0) n.right = rotateRight(n.right);
            return rotateLeft(n);
        }
        return n;
    }

    public void insert(PortfolioItem item) { root = insert(root, item); }

    private Node insert(Node n, PortfolioItem item) {
        if (n == null) return new Node(item);
        int cmp = item.getSymbol().compareTo(n.item.getSymbol());
        if (cmp < 0) n.left = insert(n.left, item);
        else if (cmp > 0) n.right = insert(n.right, item);
        else n.item = item;
        return balance(n);
    }

    public record RangeResult(List<PortfolioItem> items, int nodesVisited, int totalNodes) {}

    /** Returns items in [fromSymbol, toSymbol] plus traversal stats. O(log n + k). */
    public RangeResult findRange(String fromSymbol, String toSymbol) {
        List<PortfolioItem> result = new ArrayList<>();
        int[] visited = {0};
        findRange(root, fromSymbol, toSymbol, result, visited);
        return new RangeResult(result, visited[0], size());
    }

    private void findRange(Node n, String from, String to, List<PortfolioItem> result, int[] visited) {
        if (n == null) return;
        visited[0]++;
        int cmpFrom = from.compareTo(n.item.getSymbol());
        int cmpTo   = to.compareTo(n.item.getSymbol());
        if (cmpFrom < 0) findRange(n.left, from, to, result, visited);
        if (cmpFrom <= 0 && cmpTo >= 0) result.add(n.item);
        if (cmpTo > 0) findRange(n.right, from, to, result, visited);
    }

    public List<PortfolioItem> getItemsSorted() {
        List<PortfolioItem> result = new ArrayList<>();
        inOrder(root, result);
        return result;
    }

    private void inOrder(Node n, List<PortfolioItem> result) {
        if (n == null) return;
        inOrder(n.left, result);
        result.add(n.item);
        inOrder(n.right, result);
    }

    public int height() { return height(root); }
    public int size() { return size(root); }
    private int size(Node n) { return n == null ? 0 : 1 + size(n.left) + size(n.right); }
}
