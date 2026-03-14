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
}
