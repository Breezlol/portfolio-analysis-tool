package com.portfolio.controller;

import com.portfolio.entity.PortfolioItem;
import com.portfolio.repository.UserRepository;
import com.portfolio.service.AnalyticsService;
import com.portfolio.service.PortfolioService;
import com.portfolio.service.VolatilityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for portfolio operations.
 *
 * <p>All endpoints are scoped under {@code /users/{userId}/portfolio}.
 * Requests for non-existent users return HTTP 404 immediately.
 */
@RestController
@CrossOrigin
public class PortfolioController {

    private final UserRepository userRepository;
    private final PortfolioService portfolioService;
    private final VolatilityService volatilityService;
    private final AnalyticsService analyticsService;

    public PortfolioController(UserRepository userRepository,
                               PortfolioService portfolioService,
                               VolatilityService volatilityService,
                               AnalyticsService analyticsService) {
        this.userRepository = userRepository;
        this.portfolioService = portfolioService;
        this.volatilityService = volatilityService;
        this.analyticsService = analyticsService;
    }

    @GetMapping("/users/{userId}/portfolio")
    public ResponseEntity<?> getPortfolio(@PathVariable Long userId) {
        if (userRepository.findById(userId).isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(portfolioService.getPortfolio(userId));
    }

    @PostMapping("/users/{userId}/portfolio")
    public ResponseEntity<?> savePortfolio(@PathVariable Long userId, @RequestBody List<Map<String, Object>> items) {
        var userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) return ResponseEntity.notFound().build();

        double totalCost = items.stream().mapToDouble(item -> {
            int quantity = ((Number) item.get("quantity")).intValue();
            double price = ((Number) item.get("purchasePrice")).doubleValue();
            return quantity * price;
        }).sum();

        double deposit = userOpt.get().getDepositAmount();
        if (totalCost > deposit) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", String.format("Total portfolio cost ($%.2f) exceeds your deposit amount ($%.2f).", totalCost, deposit)
            ));
        }

        return ResponseEntity.ok(portfolioService.savePortfolio(userId, items));
    }

    @DeleteMapping("/users/{userId}/portfolio/items/{symbol}")
    public ResponseEntity<?> removeItem(@PathVariable Long userId, @PathVariable String symbol) {
        if (userRepository.findById(userId).isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(portfolioService.removeItem(userId, symbol));
    }

    @GetMapping("/users/{userId}/portfolio/value")
    public ResponseEntity<?> getPortfolioValue(@PathVariable Long userId) {
        if (userRepository.findById(userId).isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(portfolioService.getPortfolioValue(userId));
    }

    @GetMapping("/users/{userId}/portfolio/analytics")
    public ResponseEntity<?> getPortfolioAnalytics(@PathVariable Long userId) {
        if (userRepository.findById(userId).isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(volatilityService.getAnalytics(userId));
    }

    @GetMapping("/users/{userId}/portfolio/items/{symbol}")
    public ResponseEntity<?> getItem(@PathVariable Long userId, @PathVariable String symbol) {
        if (userRepository.findById(userId).isEmpty()) return ResponseEntity.notFound().build();
        PortfolioItem item = portfolioService.findItem(userId, symbol);
        return item != null ? ResponseEntity.ok(item) : ResponseEntity.notFound().build();
    }

    @GetMapping("/users/{userId}/portfolio/range")
    public ResponseEntity<?> getPortfolioRange(@PathVariable Long userId,
                                               @RequestParam String from,
                                               @RequestParam String to) {
        if (userRepository.findById(userId).isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(portfolioService.getPortfolioRange(userId, from, to));
    }

    @GetMapping("/users/{userId}/portfolio/top-movers")
    public ResponseEntity<?> getTopMovers(@PathVariable Long userId,
                                          @RequestParam(defaultValue = "5") int k) {
        if (userRepository.findById(userId).isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(analyticsService.getTopMovers(userId, k));
    }
}
