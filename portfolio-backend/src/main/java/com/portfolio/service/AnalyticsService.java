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

@Service
public class AnalyticsService {

    private final PortfolioRepository portfolioRepository;
    private final AlphaVantageService alphaVantageService;

    public AnalyticsService(PortfolioRepository portfolioRepository,
                            AlphaVantageService alphaVantageService) {
        this.portfolioRepository = portfolioRepository;
        this.alphaVantageService = alphaVantageService;
    }

    private record HoldingSnapshot(String symbol, double gainPercent, double purchasePrice, double currentPrice) {}

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
        MinHeap<HoldingSnapshot> losers =
                new MinHeap<>(Comparator.comparingDouble(HoldingSnapshot::gainPercent).reversed());

        List<String> skipped = new ArrayList<>();

        for (PortfolioItem item : tree.getItemsSorted()) {
            Double current = alphaVantageService.getLatestPrice(item.getSymbol());
            if (current == null || item.getPurchasePrice() == 0) {
                skipped.add(item.getSymbol());
                continue;
            }
            double gain = (current - item.getPurchasePrice()) / item.getPurchasePrice() * 100.0;
            HoldingSnapshot snap = new HoldingSnapshot(item.getSymbol(), gain, item.getPurchasePrice(), current);

            if (gain >= 0) {
                gainers.insert(snap);
                if (gainers.size() > k) gainers.extractMin();
            } else {
                losers.insert(snap);
                if (losers.size() > k) losers.extractMin();
            }
        }

        return Map.of(
                "topGainers", drainSorted(gainers, Comparator.comparingDouble(HoldingSnapshot::gainPercent).reversed()),
                "topLosers",  drainSorted(losers,  Comparator.comparingDouble(HoldingSnapshot::gainPercent)),
                "skipped", skipped
        );
    }

    private List<Map<String, Object>> drainSorted(MinHeap<HoldingSnapshot> heap,
                                                   Comparator<HoldingSnapshot> order) {
        List<HoldingSnapshot> tmp = new ArrayList<>();
        while (!heap.isEmpty()) tmp.add(heap.extractMin());
        tmp.sort(order);
        List<Map<String, Object>> out = new ArrayList<>();
        for (HoldingSnapshot s : tmp) {
            out.add(Map.of(
                    "symbol", s.symbol(),
                    "gainPercent", Math.round(s.gainPercent() * 10.0) / 10.0,
                    "purchasePrice", Math.round(s.purchasePrice() * 100.0) / 100.0,
                    "currentPrice", Math.round(s.currentPrice() * 100.0) / 100.0
            ));
        }
        return out;
    }
}
