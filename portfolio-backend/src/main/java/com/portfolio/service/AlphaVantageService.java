package com.portfolio.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AlphaVantageService {

    private static final long CACHE_TTL_MS = 120_000;

    @Value("${alphavantage.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<String, Double> priceCache = new ConcurrentHashMap<>();
    private final Map<String, List<Double>> historyCache = new ConcurrentHashMap<>();
    private long cacheTimestamp = 0;

    private synchronized void clearCacheIfStale() {
        if (System.currentTimeMillis() - cacheTimestamp > CACHE_TTL_MS) {
            priceCache.clear();
            historyCache.clear();
            cacheTimestamp = System.currentTimeMillis();
        }
    }

    public String searchStocks(String query) {
        String url = UriComponentsBuilder
                .fromHttpUrl("https://www.alphavantage.co/query")
                .queryParam("function", "SYMBOL_SEARCH")
                .queryParam("keywords", query)
                .queryParam("apikey", apiKey)
                .toUriString();
        return restTemplate.getForObject(url, String.class);
    }

    public Double getLatestPrice(String symbol) {
        clearCacheIfStale();
        if (priceCache.containsKey(symbol)) return priceCache.get(symbol);
        try {
            String url = UriComponentsBuilder
                    .fromHttpUrl("https://www.alphavantage.co/query")
                    .queryParam("function", "GLOBAL_QUOTE")
                    .queryParam("symbol", symbol)
                    .queryParam("apikey", apiKey)
                    .toUriString();
            String json = restTemplate.getForObject(url, String.class);
            if (json == null) return null;
            JsonNode priceNode = objectMapper.readTree(json).path("Global Quote").path("05. price");
            if (priceNode.isMissingNode() || priceNode.asText().isEmpty()) return null;
            Double price = Double.parseDouble(priceNode.asText());
            priceCache.put(symbol, price);
            return price;
        } catch (Exception e) {
            return null;
        }
    }

    public List<Double> getHistoricalPrices(String symbol) {
        clearCacheIfStale();
        if (historyCache.containsKey(symbol)) return historyCache.get(symbol);
        try {
            String url = UriComponentsBuilder
                    .fromHttpUrl("https://www.alphavantage.co/query")
                    .queryParam("function", "TIME_SERIES_DAILY")
                    .queryParam("symbol", symbol)
                    .queryParam("outputsize", "compact")
                    .queryParam("apikey", apiKey)
                    .toUriString();
            String json = restTemplate.getForObject(url, String.class);
            if (json == null) return null;
            JsonNode timeSeries = objectMapper.readTree(json).path("Time Series (Daily)");
            if (timeSeries.isMissingNode()) return null;
            List<Double> prices = new ArrayList<>();
            timeSeries.fields().forEachRemaining(entry ->
                    prices.add(entry.getValue().path("4. close").asDouble()));
            if (prices.isEmpty()) return null;
            historyCache.put(symbol, prices);
            return prices;
        } catch (Exception e) {
            return null;
        }
    }
}
