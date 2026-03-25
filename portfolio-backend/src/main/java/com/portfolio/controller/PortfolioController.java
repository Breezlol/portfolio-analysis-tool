package com.portfolio.controller;

import com.portfolio.datastructure.AVLTree;
import com.portfolio.entity.PortfolioItem;
import com.portfolio.repository.PortfolioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.portfolio.repository.UserRepository;
import com.portfolio.service.AlphaVantageService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
public class PortfolioController {

    private final PortfolioRepository portfolioRepository;
    private final UserRepository userRepository;
    private final AlphaVantageService alphaVantageService;

    public PortfolioController(PortfolioRepository portfolioRepository, UserRepository userRepository, AlphaVantageService alphaVantageService) {
        this.portfolioRepository = portfolioRepository;
        this.userRepository = userRepository;
        this.alphaVantageService = alphaVantageService;
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

    @GetMapping("/users/{userId}/portfolio/value")
    public ResponseEntity<?> getPortfolioValue(@PathVariable Long userId) {
        if (userRepository.findById(userId).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Long portfolioId = portfolioRepository.findPortfolioIdByUserId(userId);
        if (portfolioId == null) return ResponseEntity.ok(Map.of("totalValue", 0, "holdings", List.of(), "warnings", List.of()));

        List<PortfolioItem> items = portfolioRepository.findItemsByPortfolioId(portfolioId);
        AVLTree tree = new AVLTree();
        for (PortfolioItem item : items) tree.insert(item);

        List<Map<String, Object>> holdingValues = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        double totalValue = 0;

        for (PortfolioItem item : tree.getItemsSorted()) {
            Double price = alphaVantageService.getLatestPrice(item.getSymbol());
            if (price != null) {
                double value = price * item.getQuantity();
                totalValue += value;
                Map<String, Object> h = new HashMap<>();
                h.put("symbol", item.getSymbol());
                h.put("quantity", item.getQuantity());
                h.put("purchasePrice", item.getPurchasePrice());
                h.put("currentPrice", price);
                h.put("marketValue", value);
                holdingValues.add(h);
            } else {
                warnings.add(item.getSymbol() + ": price unavailable");
            }
        }

        // compute allocation percentages
        if (totalValue > 0) {
            for (Map<String, Object> h : holdingValues) {
                double mv = (double) h.get("marketValue");
                h.put("allocationPercentage", Math.round(mv / totalValue * 1000.0) / 10.0);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalValue", totalValue);
        result.put("holdings", holdingValues);
        result.put("warnings", warnings);
        return ResponseEntity.ok(result);
    }
}
