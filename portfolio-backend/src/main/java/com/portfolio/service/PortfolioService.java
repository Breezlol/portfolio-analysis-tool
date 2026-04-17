package com.portfolio.service;

import com.portfolio.datastructure.AVLTree;
import com.portfolio.dto.PortfolioItemRequest;
import com.portfolio.entity.PortfolioItem;
import com.portfolio.repository.PortfolioRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Business logic for portfolio CRUD and value calculation.
 * Uses an AVL tree as the in-memory working structure to keep items sorted
 * and to support O(log n) single-item lookup and O(log n + k) range queries.
 */
@Service
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final AlphaVantageService alphaVantageService;

    public PortfolioService(PortfolioRepository portfolioRepository, AlphaVantageService alphaVantageService) {
        this.portfolioRepository = portfolioRepository;
        this.alphaVantageService = alphaVantageService;
    }

    private AVLTree loadTree(Long portfolioId) {
        List<PortfolioItem> items = portfolioRepository.findItemsByPortfolioId(portfolioId);
        AVLTree tree = new AVLTree();
        for (PortfolioItem item : items) tree.insert(item);
        return tree;
    }

    public List<PortfolioItem> getPortfolio(Long userId) {
        Long portfolioId = portfolioRepository.findPortfolioIdByUserId(userId);
        if (portfolioId == null) return List.of();
        return loadTree(portfolioId).getItemsSorted();
    }

    public Map<String, Object> getPortfolioValue(Long userId) {
        Long portfolioId = portfolioRepository.findPortfolioIdByUserId(userId);
        if (portfolioId == null) {
            return Map.of("totalValue", 0, "holdings", List.of(), "warnings", List.of());
        }
        AVLTree tree = loadTree(portfolioId);

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

        if (totalValue > 0) {
            for (Map<String, Object> h : holdingValues) {
                double mv = (double) h.get("marketValue");
                h.put("allocationPercentage", Math.round(mv / totalValue * 1000.0) / 10.0);
            }
        }

        double maxAllocation = holdingValues.stream()
                .filter(h -> h.containsKey("allocationPercentage"))
                .mapToDouble(h -> (double) h.get("allocationPercentage"))
                .max().orElse(0);

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
        return result;
    }

    public Map<String, Object> savePortfolio(Long userId, List<PortfolioItemRequest> items) {
        Long portfolioId = portfolioRepository.findPortfolioIdByUserId(userId);
        if (portfolioId == null) portfolioId = portfolioRepository.createPortfolio(userId);
        portfolioRepository.deleteItemsByPortfolioId(portfolioId);
        AVLTree tree = new AVLTree();
        for (PortfolioItemRequest req : items) {
            tree.insert(new PortfolioItem(req.symbol(), req.quantity(), req.purchasePrice()));
        }
        for (PortfolioItem item : tree.getItemsSorted()) {
            portfolioRepository.saveItem(portfolioId, item);
        }
        return Map.of("status", "saved", "count", tree.size());
    }

    public Map<String, Object> removeItem(Long userId, String symbol) {
        Long portfolioId = portfolioRepository.findPortfolioIdByUserId(userId);
        if (portfolioId == null) return Map.of("status", "no portfolio found");
        AVLTree tree = loadTree(portfolioId);
        tree.remove(symbol.toUpperCase());
        portfolioRepository.deleteItemsByPortfolioId(portfolioId);
        for (PortfolioItem item : tree.getItemsSorted()) {
            portfolioRepository.saveItem(portfolioId, item);
        }
        return Map.of("status", "removed");
    }

    public PortfolioItem findItem(Long userId, String symbol) {
        Long portfolioId = portfolioRepository.findPortfolioIdByUserId(userId);
        if (portfolioId == null) return null;
        return loadTree(portfolioId).find(symbol.toUpperCase());
    }

    public Map<String, Object> getPortfolioRange(Long userId, String fromSymbol, String toSymbol) {
        Long portfolioId = portfolioRepository.findPortfolioIdByUserId(userId);
        if (portfolioId == null) return Map.of("items", List.of(), "nodesVisited", 0, "totalNodes", 0);
        AVLTree tree = loadTree(portfolioId);
        AVLTree.RangeResult result = tree.findRange(fromSymbol.toUpperCase(), toSymbol.toUpperCase());
        return Map.of(
            "items", result.items(),
            "nodesVisited", result.nodesVisited(),
            "totalNodes", result.totalNodes(),
            "from", fromSymbol.toUpperCase(),
            "to", toSymbol.toUpperCase()
        );
    }
}
