package com.portfolio.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AlphaVantageService {

    @Value("${alphavantage.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public String searchStocks(String query) {
        String url = "https://www.alphavantage.co/query?function=SYMBOL_SEARCH&keywords="
                + query + "&apikey=" + apiKey;
        return restTemplate.getForObject(url, String.class);
    }

    // fetches the latest price for a single stock symbol
    // returns null if the API call fails or rate limit is hit
    public Double getLatestPrice(String symbol) {
        try {
            String url = "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol="
                    + symbol + "&apikey=" + apiKey;
            String json = restTemplate.getForObject(url, String.class);
            // parse the price from the JSON response
            // format: "Global Quote": { "05. price": "123.45" }
            if (json != null && json.contains("05. price")) {
                int start = json.indexOf("05. price") + 13;
                int end = json.indexOf("\"", start);
                return Double.parseDouble(json.substring(start, end));
            }
        } catch (Exception e) {
            // rate limit or network error - return null
        }
        return null;
    }
}
