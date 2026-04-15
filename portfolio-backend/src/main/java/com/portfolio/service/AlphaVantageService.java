package com.portfolio.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
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
    private static final String BASE = "https://query1.finance.yahoo.com";

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

    private HttpEntity<Void> headers() {
        HttpHeaders h = new HttpHeaders();
        h.set("User-Agent", "Mozilla/5.0");
        h.set("Accept", "application/json");
        return new HttpEntity<>(h);
    }

    public String searchStocks(String query) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(BASE + "/v1/finance/search")
                    .queryParam("q", query)
                    .queryParam("quotesCount", 10)
                    .queryParam("newsCount", 0)
                    .toUriString();
            ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.GET, headers(), String.class);
            JsonNode quotes = objectMapper.readTree(resp.getBody()).path("quotes");
            List<Map<String, String>> matches = new ArrayList<>();
            for (JsonNode q : quotes) {
                String type = q.path("quoteType").asText("");
                if (!type.equals("EQUITY") && !type.equals("ETF")) continue;
                matches.add(Map.of(
                        "1. symbol", q.path("symbol").asText(),
                        "2. name",   q.path("shortname").asText(q.path("longname").asText())
                ));
            }
            return objectMapper.writeValueAsString(Map.of("bestMatches", matches));
        } catch (Exception e) {
            return "{\"bestMatches\":[]}";
        }
    }
}
