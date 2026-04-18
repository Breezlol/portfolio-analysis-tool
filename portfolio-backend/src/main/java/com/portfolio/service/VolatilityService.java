package com.portfolio.service;

import com.portfolio.datastructure.AVLTree;
import com.portfolio.entity.PortfolioItem;
import com.portfolio.repository.PortfolioRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class VolatilityService {

    private static final double RISK_FREE_RATE = 0.04;
    private static final double Z_95 = 1.645;

    private final PortfolioRepository portfolioRepository;
    private final YahooFinanceService yahooFinanceService;

    public VolatilityService(PortfolioRepository portfolioRepository, YahooFinanceService yahooFinanceService) {
        this.portfolioRepository = portfolioRepository;
        this.yahooFinanceService = yahooFinanceService;
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
            Double price = yahooFinanceService.getLatestPrice(item.getSymbol());
            List<Double> history = yahooFinanceService.getHistoricalPrices(item.getSymbol());
            if (price != null && history != null && history.size() > 1) {
                double mv = price * item.getQuantity();
                marketValues.add(mv);
                totalValue += mv;
                analyzed.add(item.getSymbol());
                List<Double> returns = new ArrayList<>();
                for (int i = 1; i < history.size(); i++) {
                    returns.add((history.get(i) - history.get(i - 1)) / history.get(i - 1));
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
        variance /= (portfolioReturns.length - 1);

        double dailyVol = Math.sqrt(variance);
        double annualVol = dailyVol * Math.sqrt(252);
        double annualMeanReturn = mean * 252;

        if (annualVol == 0) {
            return Map.of("error", "Not enough price history to compute risk metrics.",
                    "skipped", skipped, "analyzedSymbols", analyzed);
        }

        double annualizedVolatility = Math.round(annualVol * 1000.0) / 10.0;
        double sharpeRatio = Math.round((annualMeanReturn - RISK_FREE_RATE) / annualVol * 100.0) / 100.0;
        double var95 = Math.round(totalValue * dailyVol * Z_95 * 100.0) / 100.0;

        String[] risk = classifyRisk(annualizedVolatility);

        Map<String, Object> result = new HashMap<>();
        result.put("volatility", annualizedVolatility);
        result.put("sharpeRatio", sharpeRatio);
        result.put("var95", var95);
        result.put("riskLabel", risk[0]);
        result.put("riskExplanation", risk[1]);
        result.put("analyzedSymbols", analyzed);
        result.put("skippedSymbols", skipped);
        return result;
    }

    private String[] classifyRisk(double annualizedVolatility) {
        if (annualizedVolatility < 10) return new String[]{"Low", "Your portfolio is relatively stable with small price movements."};
        if (annualizedVolatility < 20) return new String[]{"Moderate", "Your portfolio may experience noticeable price swings, but not extreme ones."};
        if (annualizedVolatility < 35) return new String[]{"High", "Your portfolio can experience significant price swings."};
        return new String[]{"Very High", "Your portfolio is highly volatile and may see large daily changes."};
    }
}
