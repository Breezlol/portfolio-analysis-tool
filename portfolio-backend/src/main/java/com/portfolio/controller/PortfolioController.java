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

        // compute allocation percentages and find max
        double maxAllocation = 0;
        if (totalValue > 0) {
            for (Map<String, Object> h : holdingValues) {
                double mv = (double) h.get("marketValue");
                double pct = Math.round(mv / totalValue * 1000.0) / 10.0;
                h.put("allocationPercentage", pct);
                if (pct > maxAllocation) maxAllocation = pct;
            }
        }

        // concentration analysis
        String concentrationLabel;
        String concentrationExplanation;
        if (holdingValues.size() <= 1) {
            concentrationLabel = "Highly concentrated";
            concentrationExplanation = "Your entire portfolio is in a single stock.";
        } else if (maxAllocation >= 60) {
            concentrationLabel = "Highly concentrated";
            concentrationExplanation = "A large share of your portfolio is invested in one stock.";
        } else if (maxAllocation >= 40) {
            concentrationLabel = "Somewhat concentrated";
            concentrationExplanation = "A few holdings make up most of your portfolio.";
        } else {
            concentrationLabel = "Well diversified";
            concentrationExplanation = "Your investments are spread across multiple holdings.";
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalValue", totalValue);
        result.put("holdings", holdingValues);
        result.put("warnings", warnings);
        result.put("concentrationLabel", concentrationLabel);
        result.put("concentrationExplanation", concentrationExplanation);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/users/{userId}/portfolio/analytics")
    public ResponseEntity<?> getPortfolioAnalytics(@PathVariable Long userId) {
        if (userRepository.findById(userId).isEmpty()) return ResponseEntity.notFound().build();
        Long portfolioId = portfolioRepository.findPortfolioIdByUserId(userId);
        if (portfolioId == null) return ResponseEntity.ok(Map.of("error", "No portfolio found"));

        List<PortfolioItem> items = portfolioRepository.findItemsByPortfolioId(portfolioId);
        AVLTree tree = new AVLTree();
        for (PortfolioItem item : items) tree.insert(item);

        List<String> analyzed = new ArrayList<>();
        List<String> skipped = new ArrayList<>();
        List<Double> weights = new ArrayList<>();
        List<List<Double>> allReturns = new ArrayList<>();
        double totalValue = 0;
        List<Double> marketValues = new ArrayList<>();

        for (PortfolioItem item : tree.getItemsSorted()) {
            Double price = alphaVantageService.getLatestPrice(item.getSymbol());
            List<Double> history = alphaVantageService.getHistoricalPrices(item.getSymbol());
            if (price != null && history != null && history.size() > 1) {
                double mv = price * item.getQuantity();
                marketValues.add(mv);
                totalValue += mv;
                analyzed.add(item.getSymbol());
                List<Double> returns = new ArrayList<>();
                for (int i = 1; i < history.size(); i++) {
                    returns.add((history.get(i - 1) - history.get(i)) / history.get(i));
                }
                allReturns.add(returns);
            } else {
                skipped.add(item.getSymbol());
            }
        }

        if (analyzed.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                "error", "Could not retrieve market data for any holdings.",
                "skipped", skipped
            ));
        }

        for (Double mv : marketValues) weights.add(mv / totalValue);
        int minLen = allReturns.stream().mapToInt(List::size).min().orElse(0);

        double[] portfolioReturns = new double[minLen];
        for (int t = 0; t < minLen; t++) {
            for (int s = 0; s < weights.size(); s++) {
                portfolioReturns[t] += weights.get(s) * allReturns.get(s).get(t);
            }
        }

        double mean = 0;
        for (double r : portfolioReturns) mean += r;
        mean /= portfolioReturns.length;
        double variance = 0;
        for (double r : portfolioReturns) variance += (r - mean) * (r - mean);
        variance /= portfolioReturns.length;

        double annualizedVolatility = Math.sqrt(variance * 252) * 100;
        annualizedVolatility = Math.round(annualizedVolatility * 10.0) / 10.0;

        String riskLabel;
        String riskExplanation;
        if (annualizedVolatility < 10) {
            riskLabel = "Low";
            riskExplanation = "Your portfolio is relatively stable with small price movements.";
        } else if (annualizedVolatility < 20) {
            riskLabel = "Moderate";
            riskExplanation = "Your portfolio may experience noticeable price swings, but not extreme ones.";
        } else if (annualizedVolatility < 35) {
            riskLabel = "High";
            riskExplanation = "Your portfolio can experience significant price swings.";
        } else {
            riskLabel = "Very High";
            riskExplanation = "Your portfolio is highly volatile and may see large daily changes.";
        }

        Map<String, Object> result = new HashMap<>();
        result.put("volatility", annualizedVolatility);
        result.put("riskLabel", riskLabel);
        result.put("riskExplanation", riskExplanation);
        result.put("analyzedSymbols", analyzed);
        result.put("skippedSymbols", skipped);
        return ResponseEntity.ok(result);
    }
}
