package com.portfolio.datastructure;

import com.portfolio.entity.PortfolioItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AVLTreeTest {

    private AVLTree tree;

    @BeforeEach
    void setUp() {
        tree = new AVLTree();
    }

    @Test
    void insertionReturnsSortedOrder() {
        tree.insert(new PortfolioItem("TSLA", 1, 200.0));
        tree.insert(new PortfolioItem("AAPL", 2, 150.0));
        tree.insert(new PortfolioItem("MSFT", 3, 300.0));

        List<PortfolioItem> sorted = tree.getItemsSorted();

        assertEquals("AAPL", sorted.get(0).getSymbol());
        assertEquals("MSFT", sorted.get(1).getSymbol());
        assertEquals("TSLA", sorted.get(2).getSymbol());
    }

    @Test
    void worstCaseInsertOrderStillReturnsSorted() {
        // Inserting already-sorted keys is the worst case for an unbalanced BST
        // (it degrades to a linked list). AVL must stay sorted and balanced.
        tree.insert(new PortfolioItem("AAPL", 1, 100.0));
        tree.insert(new PortfolioItem("GOOG", 1, 100.0));
        tree.insert(new PortfolioItem("MSFT", 1, 100.0));
        tree.insert(new PortfolioItem("NFLX", 1, 100.0));
        tree.insert(new PortfolioItem("TSLA", 1, 100.0));

        List<String> symbols = tree.getItemsSorted().stream().map(PortfolioItem::getSymbol).toList();

        assertEquals(List.of("AAPL", "GOOG", "MSFT", "NFLX", "TSLA"), symbols);
    }

    @Test
    void treeHeightIsLogarithmicAfterSortedInserts() {
        // 5 nodes in a balanced tree has height 3 (ceil(log2(5)) + 1)
        // An unbalanced BST with sorted inserts would have height 5
        tree.insert(new PortfolioItem("AAPL", 1, 100.0));
        tree.insert(new PortfolioItem("GOOG", 1, 100.0));
        tree.insert(new PortfolioItem("MSFT", 1, 100.0));
        tree.insert(new PortfolioItem("NFLX", 1, 100.0));
        tree.insert(new PortfolioItem("TSLA", 1, 100.0));

        assertTrue(tree.height() <= 3, "Tree height should be <= 3 for 5 nodes, was: " + tree.height());
    }
}
