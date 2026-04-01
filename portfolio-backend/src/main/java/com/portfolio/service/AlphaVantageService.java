package com.portfolio.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AlphaVantageService {

    @Value("${alphavantage.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    // simple in-memory cache to avoid duplicate API calls within the same session
    private final Map<String, Double> priceCache = new HashMap<>();
    private final Map<String, List<Double>> historyCache = new HashMap<>();
    private long cacheTimestamp = 0;

    private void clearCacheIfStale() {
        // clear cache after 2 minutes
        if (System.currentTimeMillis() - cacheTimestamp > 120000) {
            priceCache.clear();
            historyCache.clear();
            cacheTimestamp = System.currentTimeMillis();
        }
    }

    public String searchStocks(String query) {
        String url = "https://www.alphavantage.co/query?function=SYMBOL_SEARCH&keywords="
                + query + "&apikey=" + apiKey;
        return restTemplate.getForObject(url, String.class);
    }

    // fetches the latest price for a single stock symbol
    // returns null if the API call fails or rate limit is hit
    public Double getLatestPrice(String symbol) {
        clearCacheIfStale();
        if (priceCache.containsKey(symbol)) return priceCache.get(symbol);
        try {
            String url = "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol="
                    + symbol + "&apikey=" + apiKey;
            String json = restTemplate.getForObject(url, String.class);
            if (json != null && json.contains("05. price")) {
                int start = json.indexOf("05. price") + 13;
                int end = json.indexOf("\"", start);
                Double price = Double.parseDouble(json.substring(start, end));
                priceCache.put(symbol, price);
                return price;
            }
        } catch (Exception e) {
            // rate limit or network error - return null
        }
        return null;
    }

    // fetches ~100 days of daily closing prices for volatility calculation
    public List<Double> getHistoricalPrices(String symbol) {
        clearCacheIfStale();
        if (historyCache.containsKey(symbol)) return historyCache.get(symbol);
        try {
            String url = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol="
                    + symbol + "&outputsize=compact&apikey=" + apiKey;
            String json = restTemplate.getForObject(url, String.class);
            if (json == null || !json.contains("4. close")) return null;

            List<Double> prices = new ArrayList<>();
            int index = 0;
            while ((index = json.indexOf("4. close", index)) != -1) {
                int start = index + 12; // skip past "4. close": "
                int end = json.indexOf("\"", start);
                prices.add(Double.parseDouble(json.substring(start, end)));
                index = end;
            }
            if (prices.isEmpty()) return null;
            historyCache.put(symbol, prices);
            return prices;
        } catch (Exception e) {
            return null;
        }
    }
}
