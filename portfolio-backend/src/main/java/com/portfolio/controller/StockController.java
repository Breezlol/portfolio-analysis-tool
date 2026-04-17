package com.portfolio.controller;

import com.portfolio.service.YahooFinanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/stocks")
@CrossOrigin
public class StockController {

    private final YahooFinanceService yahooFinanceService;

    public StockController(YahooFinanceService yahooFinanceService) {
        this.yahooFinanceService = yahooFinanceService;
    }

    @GetMapping("/search")
    public ResponseEntity<String> search(@RequestParam String q) {
        return ResponseEntity.ok(yahooFinanceService.searchStocks(q));
    }

    @GetMapping("/quote")
    public ResponseEntity<?> quote(@RequestParam String symbol) {
        Double price = yahooFinanceService.getLatestPrice(symbol);
        if (price == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(price);
    }
}
