package com.portfolio.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AlphaVantageService {

    @Value("${alphavantage.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    private static final long CACHE_TTL_MS = 60_000;

    private static class CacheEntry {
        final Double price;
        final long timestamp;
        CacheEntry(Double price) { this.price = price; this.timestamp = System.currentTimeMillis(); }
        boolean isExpired() { return System.currentTimeMillis() - timestamp > CACHE_TTL_MS; }
    }

    private final Map<String, CacheEntry> priceCache = new ConcurrentHashMap<>();

    public String searchStocks(String query) {
        String url = "https://www.alphavantage.co/query?function=SYMBOL_SEARCH&keywords="
                + query + "&apikey=" + apiKey;
        return restTemplate.getForObject(url, String.class);
    }

    // fetches the latest price for a single stock symbol
    // returns null if the API call fails or rate limit is hit
    public Double getLatestPrice(String symbol) {
        CacheEntry cached = priceCache.get(symbol);
        if (cached != null && !cached.isExpired()) return cached.price;
        try {
            String url = "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol="
                    + symbol + "&apikey=" + apiKey;
            String json = restTemplate.getForObject(url, String.class);
            // parse the price from the JSON response
            // format: "Global Quote": { "05. price": "123.45" }
            if (json != null && json.contains("05. price")) {
                int start = json.indexOf("05. price") + 13;
                int end = json.indexOf("\"", start);
                Double price = Double.parseDouble(json.substring(start, end));
                priceCache.put(symbol, new CacheEntry(price));
                return price;
            }
        } catch (Exception e) {
            // rate limit or network error - return null
        }
        return null;
    }
}
