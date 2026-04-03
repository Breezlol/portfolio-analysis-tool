package com.portfolio.service;

import com.portfolio.datastructure.AVLTree;
import com.portfolio.entity.PortfolioItem;
import com.portfolio.repository.PortfolioRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;

    public PortfolioService(PortfolioRepository portfolioRepository) {
        this.portfolioRepository = portfolioRepository;
    }

    public List<PortfolioItem> getPortfolio(Long userId) {
        Long portfolioId = portfolioRepository.findPortfolioIdByUserId(userId);
        if (portfolioId == null) return List.of();
        List<PortfolioItem> items = portfolioRepository.findItemsByPortfolioId(portfolioId);
        AVLTree tree = new AVLTree();
        for (PortfolioItem item : items) tree.insert(item);
        return tree.getItemsSorted();
    }

    public Map<String, Object> savePortfolio(Long userId, List<Map<String, Object>> items) {
        Long portfolioId = portfolioRepository.findPortfolioIdByUserId(userId);
        if (portfolioId == null) portfolioId = portfolioRepository.createPortfolio(userId);
        portfolioRepository.deleteItemsByPortfolioId(portfolioId);
        AVLTree tree = new AVLTree();
        for (Map<String, Object> raw : items) {
            String symbol = (String) raw.get("symbol");
            int quantity = raw.get("quantity") instanceof Integer
                    ? (int) raw.get("quantity")
                    : ((Number) raw.get("quantity")).intValue();
            double price = raw.get("purchasePrice") instanceof Double
                    ? (double) raw.get("purchasePrice")
                    : ((Number) raw.get("purchasePrice")).doubleValue();
            tree.insert(new PortfolioItem(symbol, quantity, price));
        }
        for (PortfolioItem item : tree.getItemsSorted()) {
            portfolioRepository.saveItem(portfolioId, item);
        }
        return Map.of("status", "saved", "count", tree.size());
    }

    public Map<String, Object> removeItem(Long userId, String symbol) {
        Long portfolioId = portfolioRepository.findPortfolioIdByUserId(userId);
        if (portfolioId == null) return Map.of("status", "no portfolio found");
        List<PortfolioItem> items = portfolioRepository.findItemsByPortfolioId(portfolioId);
        AVLTree tree = new AVLTree();
        for (PortfolioItem item : items) tree.insert(item);
        tree.remove(symbol.toUpperCase());
        portfolioRepository.deleteItemsByPortfolioId(portfolioId);
        for (PortfolioItem item : tree.getItemsSorted()) {
            portfolioRepository.saveItem(portfolioId, item);
        }
        return Map.of("status", "removed");
    }
}
