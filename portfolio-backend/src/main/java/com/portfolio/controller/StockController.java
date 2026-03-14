package com.portfolio.controller;

import com.portfolio.service.AlphaVantageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/stocks")
@CrossOrigin
public class StockController {

    private final AlphaVantageService alphaVantageService;

    public StockController(AlphaVantageService alphaVantageService) {
        this.alphaVantageService = alphaVantageService;
    }

    @GetMapping("/search")
    public ResponseEntity<String> search(@RequestParam String q) {
        return ResponseEntity.ok(alphaVantageService.searchStocks(q));
    }
}
