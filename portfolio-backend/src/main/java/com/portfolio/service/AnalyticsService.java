package com.portfolio.service;

import com.portfolio.datastructure.AVLTree;
import com.portfolio.datastructure.MinHeap;
import com.portfolio.entity.PortfolioItem;
import com.portfolio.repository.PortfolioRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Identifies top-k gainers and losers using two MinHeap instances.
 *
 * <p>Algorithm: O(n log k) — for each holding insert into a size-k heap,
 * evicting the weakest candidate when the heap exceeds k. Full sort would
 * be O(n log n); this approach is preferable when k &lt;&lt; n.
 */
@Service
public class AnalyticsService {

    private final PortfolioRepository portfolioRepository;
    private final AlphaVantageService alphaVantageService;

    public AnalyticsService(PortfolioRepository portfolioRepository,
                            AlphaVantageService alphaVantageService) {
        this.portfolioRepository = portfolioRepository;
        this.alphaVantageService = alphaVantageService;
    }

    private record HoldingSnapshot(String symbol, double gainPercent) {}

    public Map<String, Object> getTopMovers(Long userId, int k) {
        Long portfolioId = portfolioRepository.findPortfolioIdByUserId(userId);
        if (portfolioId == null) return Map.of("error", "No portfolio found");

        List<PortfolioItem> items = portfolioRepository.findItemsByPortfolioId(portfolioId);
        AVLTree tree = new AVLTree();
        for (PortfolioItem item : items) tree.insert(item);

        // gainers heap: min-heap by gainPercent — evict the smallest gain when full
        MinHeap<HoldingSnapshot> gainers =
                new MinHeap<>(Comparator.comparingDouble(HoldingSnapshot::gainPercent));
        // losers heap: min-heap with reversed order — evict the least-negative when full
