package com.portfolio.controller;

import com.portfolio.datastructure.AVLTree;
import com.portfolio.entity.PortfolioItem;
import com.portfolio.repository.PortfolioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.portfolio.repository.UserRepository;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
public class PortfolioController {

    private final PortfolioRepository portfolioRepository;
    private final UserRepository userRepository;

    public PortfolioController(PortfolioRepository portfolioRepository, UserRepository userRepository) {
        this.portfolioRepository = portfolioRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/users/{userId}/portfolio")
    public ResponseEntity<?> getPortfolio(@PathVariable Long userId) {
        if (userRepository.findById(userId).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Long portfolioId = portfolioRepository.findPortfolioIdByUserId(userId);
        if (portfolioId == null) return ResponseEntity.ok(List.of());

        List<PortfolioItem> items = portfolioRepository.findItemsByPortfolioId(portfolioId);
        // rebuild AVL tree so items come back sorted
        AVLTree tree = new AVLTree();
        for (PortfolioItem item : items) tree.insert(item);

        return ResponseEntity.ok(tree.getItemsSorted());
    }

    @PostMapping("/users/{userId}/portfolio")
    public ResponseEntity<?> savePortfolio(@PathVariable Long userId, @RequestBody List<Map<String, Object>> items) {
        // find or create portfolio for this user
        Long portfolioId = portfolioRepository.findPortfolioIdByUserId(userId);
        if (portfolioId == null) portfolioId = portfolioRepository.createPortfolio(userId);

        // clear old items and re-insert through AVL tree
        portfolioRepository.deleteItemsByPortfolioId(portfolioId);
        AVLTree tree = new AVLTree();
        for (Map<String, Object> raw : items) {
            String symbol = (String) raw.get("symbol");
            int quantity = raw.get("quantity") instanceof Integer ? (int) raw.get("quantity") : ((Number) raw.get("quantity")).intValue();
            double price = raw.get("purchasePrice") instanceof Double ? (double) raw.get("purchasePrice") : ((Number) raw.get("purchasePrice")).doubleValue();
            tree.insert(new PortfolioItem(symbol, quantity, price));
        }

        // persist sorted items from AVL tree
        for (PortfolioItem item : tree.getItemsSorted()) {
            portfolioRepository.saveItem(portfolioId, item);
        }

        return ResponseEntity.ok(Map.of("status", "saved", "count", tree.size()));
    }

    @DeleteMapping("/portfolios/{portfolioId}/items/{symbol}")
    public ResponseEntity<?> removeItem(@PathVariable Long portfolioId, @PathVariable String symbol) {
        List<PortfolioItem> items = portfolioRepository.findItemsByPortfolioId(portfolioId);
        AVLTree tree = new AVLTree();
        for (PortfolioItem item : items) tree.insert(item);
        tree.remove(symbol.toUpperCase());

        portfolioRepository.deleteItemsByPortfolioId(portfolioId);
        for (PortfolioItem item : tree.getItemsSorted()) {
            portfolioRepository.saveItem(portfolioId, item);
        }

        return ResponseEntity.ok(Map.of("status", "removed"));
    }
}
