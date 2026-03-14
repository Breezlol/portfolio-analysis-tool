package com.portfolio.datastructure;

import com.portfolio.entity.PortfolioItem;
import java.util.ArrayList;
import java.util.List;

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
}
