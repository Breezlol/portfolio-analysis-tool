package com.portfolio.service;

import com.portfolio.datastructure.AVLTree;
import com.portfolio.dto.PortfolioItemRequest;
import com.portfolio.entity.PortfolioItem;
import com.portfolio.entity.User;
import com.portfolio.repository.PortfolioRepository;
import com.portfolio.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final YahooFinanceService yahooFinanceService;
    private final UserRepository userRepository;

    public PortfolioService(PortfolioRepository portfolioRepository,
                            YahooFinanceService yahooFinanceService,
                            UserRepository userRepository) {
        this.portfolioRepository = portfolioRepository;
        this.yahooFinanceService = yahooFinanceService;
        this.userRepository = userRepository;
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

        List<Map<String, Object>> holdings = new ArrayList<>();
        List<Double> marketValues = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        double totalValue = 0;

        for (PortfolioItem item : loadTree(portfolioId).getItemsSorted()) {
            Double price = yahooFinanceService.getLatestPrice(item.getSymbol());
            if (price == null) {
                warnings.add(item.getSymbol() + ": price unavailable");
                continue;
            }
            double value = price * item.getQuantity();
            totalValue += value;
            marketValues.add(value);
            Map<String, Object> h = new HashMap<>();
            h.put("symbol", item.getSymbol());
            h.put("quantity", item.getQuantity());
            h.put("purchasePrice", item.getPurchasePrice());
            h.put("currentPrice", price);
            h.put("marketValue", value);
            holdings.add(h);
        }

        double maxAllocation = 0;
        if (totalValue > 0) {
            for (int i = 0; i < holdings.size(); i++) {
                double allocation = Math.round(marketValues.get(i) / totalValue * 1000.0) / 10.0;
                holdings.get(i).put("allocationPercentage", allocation);
                if (allocation > maxAllocation) maxAllocation = allocation;
            }
        }

        String[] concentration = classifyConcentration(holdings.size(), maxAllocation);

        Map<String, Object> result = new HashMap<>();
        result.put("totalValue", totalValue);
        result.put("holdings", holdings);
        result.put("warnings", warnings);
        result.put("concentrationLabel", concentration[0]);
        result.put("concentrationExplanation", concentration[1]);
        return result;
    }

    private String[] classifyConcentration(int holdingCount, double maxAllocation) {
        if (holdingCount <= 1) return new String[]{"Highly concentrated", "Your entire portfolio is in a single stock."};
        if (maxAllocation >= 60) return new String[]{"Highly concentrated", "A large share of your portfolio is invested in one stock."};
        if (maxAllocation >= 40) return new String[]{"Somewhat concentrated", "A few holdings make up most of your portfolio."};
        return new String[]{"Well diversified", "Your investments are spread across multiple holdings."};
    }

    @Transactional
    public Map<String, Object> savePortfolio(Long userId, List<PortfolioItemRequest> items) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        double totalCost = items.stream()
                .mapToDouble(item -> item.quantity() * item.purchasePrice())
                .sum();
        double deposit = user.getDepositAmount();
        if (totalCost > deposit) {
            throw new BudgetExceededException(String.format(
                    "Total portfolio cost ($%.2f) exceeds your deposit amount ($%.2f).",
                    totalCost, deposit));
        }

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
