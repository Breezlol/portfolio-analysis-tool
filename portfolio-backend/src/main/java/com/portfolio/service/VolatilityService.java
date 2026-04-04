package com.portfolio.service;

import com.portfolio.datastructure.AVLTree;
import com.portfolio.entity.PortfolioItem;
import com.portfolio.repository.PortfolioRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Computes annualised volatility, Sharpe ratio, and 1-day 95% parametric VaR
 * for a user's portfolio using historical daily close prices.
 */
@Service
public class VolatilityService {

    private static final double RISK_FREE_RATE = 0.04;
    private static final double Z_95 = 1.645;

    private final PortfolioRepository portfolioRepository;
    private final AlphaVantageService alphaVantageService;

    public VolatilityService(PortfolioRepository portfolioRepository, AlphaVantageService alphaVantageService) {
        this.portfolioRepository = portfolioRepository;
        this.alphaVantageService = alphaVantageService;
    }

    public Map<String, Object> getAnalytics(Long userId) {
        Long portfolioId = portfolioRepository.findPortfolioIdByUserId(userId);
        if (portfolioId == null) return Map.of("error", "No portfolio found");

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
            return Map.of("error", "Could not retrieve market data for any holdings.", "skipped", skipped);
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

        double dailyVol = Math.sqrt(variance);
