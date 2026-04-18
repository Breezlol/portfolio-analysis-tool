package com.portfolio.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class YahooFinanceService {

    private static final long CACHE_TTL_MS = 120_000;
    private static final String CHART_BASE = "https://query1.finance.yahoo.com/v8/finance/chart/";
    private static final String SEARCH_BASE = "https://query2.finance.yahoo.com/v1/finance/search";

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
            String url = UriComponentsBuilder.fromHttpUrl(SEARCH_BASE)
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
                        "symbol", q.path("symbol").asText(),
                        "name", q.path("shortname").asText(q.path("longname").asText())
                ));
            }
            return objectMapper.writeValueAsString(Map.of("bestMatches", matches));
        } catch (RestClientException e) {
            return "{\"bestMatches\":[]}";
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public Double getLatestPrice(String symbol) {
        clearCacheIfStale();
        if (priceCache.containsKey(symbol)) return priceCache.get(symbol);
        try {
            String url = CHART_BASE + symbol;
            ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.GET, headers(), String.class);
            JsonNode meta = objectMapper.readTree(resp.getBody())
                    .path("chart").path("result").path(0).path("meta");
            if (meta.isMissingNode()) return null;
            String state = meta.path("marketState").asText("REGULAR");
            double price = switch (state) {
                case "PRE" -> meta.path("preMarketPrice").asDouble(meta.path("regularMarketPrice").asDouble(0));
                case "POST", "POSTPOST", "CLOSED" -> meta.path("postMarketPrice").asDouble(meta.path("regularMarketPrice").asDouble(0));
                default -> meta.path("regularMarketPrice").asDouble(0);
            };
            if (price == 0) return null;
            priceCache.put(symbol, price);
            return price;
        } catch (RestClientException e) {
            return null;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Double> getHistoricalPrices(String symbol) {
        clearCacheIfStale();
        if (historyCache.containsKey(symbol)) return historyCache.get(symbol);
        try {
            String url = UriComponentsBuilder.fromHttpUrl(CHART_BASE + symbol)
                    .queryParam("interval", "1d")
                    .queryParam("range", "2y")
                    .toUriString();
            ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.GET, headers(), String.class);
            JsonNode closes = objectMapper.readTree(resp.getBody())
                    .path("chart").path("result").path(0)
                    .path("indicators").path("quote").path(0).path("close");
            if (closes.isMissingNode() || !closes.isArray()) return null;
            List<Double> prices = new ArrayList<>();
            for (JsonNode c : closes) {
                if (!c.isNull()) prices.add(c.asDouble());
            }
            if (prices.isEmpty()) return null;
            historyCache.put(symbol, prices);
            return prices;
        } catch (RestClientException e) {
            return null;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
