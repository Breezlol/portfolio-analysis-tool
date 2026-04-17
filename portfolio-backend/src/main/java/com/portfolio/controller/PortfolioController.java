package com.portfolio.controller;

import com.portfolio.dto.PortfolioItemRequest;
import com.portfolio.repository.UserRepository;
import com.portfolio.service.AnalyticsService;
import com.portfolio.service.PortfolioService;
import com.portfolio.service.VolatilityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

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

    private ResponseEntity<Object> withUser(Long userId, Supplier<Object> action) {
        if (userRepository.findById(userId).isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(action.get());
    }

    @GetMapping("/users/{userId}/portfolio")
    public ResponseEntity<Object> getPortfolio(@PathVariable Long userId) {
        return withUser(userId, () -> portfolioService.getPortfolio(userId));
    }

    @PostMapping("/users/{userId}/portfolio")
    public ResponseEntity<?> savePortfolio(@PathVariable Long userId, @RequestBody List<PortfolioItemRequest> items) {
        var userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) return ResponseEntity.notFound().build();

        double totalCost = items.stream()
                .mapToDouble(item -> item.quantity() * item.purchasePrice())
                .sum();

        double deposit = userOpt.get().getDepositAmount();
        if (totalCost > deposit) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", String.format("Total portfolio cost ($%.2f) exceeds your deposit amount ($%.2f).", totalCost, deposit)
            ));
        }

        return ResponseEntity.ok(portfolioService.savePortfolio(userId, items));
    }

    @GetMapping("/users/{userId}/portfolio/value")
    public ResponseEntity<Object> getPortfolioValue(@PathVariable Long userId) {
        return withUser(userId, () -> portfolioService.getPortfolioValue(userId));
    }

    @GetMapping("/users/{userId}/portfolio/analytics")
    public ResponseEntity<Object> getPortfolioAnalytics(@PathVariable Long userId) {
        return withUser(userId, () -> volatilityService.getAnalytics(userId));
    }

    @GetMapping("/users/{userId}/portfolio/range")
    public ResponseEntity<Object> getPortfolioRange(@PathVariable Long userId,
                                                    @RequestParam String from,
                                                    @RequestParam String to) {
        return withUser(userId, () -> portfolioService.getPortfolioRange(userId, from, to));
    }

    @GetMapping("/users/{userId}/portfolio/top-movers")
    public ResponseEntity<Object> getTopMovers(@PathVariable Long userId,
                                               @RequestParam(defaultValue = "5") int k) {
        return withUser(userId, () -> analyticsService.getTopMovers(userId, k));
    }
}
