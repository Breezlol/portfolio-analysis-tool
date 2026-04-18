package com.portfolio.service;

import com.portfolio.entity.PortfolioItem;
import com.portfolio.repository.PortfolioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VolatilityServiceTest {

    @Mock PortfolioRepository portfolioRepository;
    @Mock YahooFinanceService yahooFinanceService;
    @InjectMocks VolatilityService service;

    private List<Double> risingPrices() {
        return List.of(100.0, 102.0, 105.0, 108.0, 110.0);
    }

    @Test
    void returnsVolatilityForValidPortfolio() {
        when(portfolioRepository.findPortfolioIdByUserId(1L)).thenReturn(10L);
        when(portfolioRepository.findItemsByPortfolioId(10L))
                .thenReturn(List.of(new PortfolioItem("AAPL", 10, 100.0)));
        when(yahooFinanceService.getLatestPrice("AAPL")).thenReturn(110.0);
        when(yahooFinanceService.getHistoricalPrices("AAPL")).thenReturn(risingPrices());

        Map<String, Object> result = service.getAnalytics(1L);

        assertFalse(result.containsKey("error"));
        assertTrue((double) result.get("volatility") > 0);
    }

    @Test
    void sharpeRatioPresentAndPositiveForRisingPortfolio() {
        when(portfolioRepository.findPortfolioIdByUserId(1L)).thenReturn(10L);
        when(portfolioRepository.findItemsByPortfolioId(10L))
                .thenReturn(List.of(new PortfolioItem("AAPL", 10, 100.0)));
        when(yahooFinanceService.getLatestPrice("AAPL")).thenReturn(110.0);
        when(yahooFinanceService.getHistoricalPrices("AAPL")).thenReturn(risingPrices());

        Map<String, Object> result = service.getAnalytics(1L);

        assertTrue(result.containsKey("sharpeRatio"));
        assertTrue((double) result.get("sharpeRatio") > 0);
    }

    @Test
    void var95PresentAndPositive() {
        when(portfolioRepository.findPortfolioIdByUserId(1L)).thenReturn(10L);
        when(portfolioRepository.findItemsByPortfolioId(10L))
                .thenReturn(List.of(new PortfolioItem("AAPL", 10, 100.0)));
        when(yahooFinanceService.getLatestPrice("AAPL")).thenReturn(110.0);
        when(yahooFinanceService.getHistoricalPrices("AAPL")).thenReturn(risingPrices());

        Map<String, Object> result = service.getAnalytics(1L);

        assertTrue(result.containsKey("var95"));
        assertTrue((double) result.get("var95") > 0);
    }

    @Test
    void returnsErrorWhenNoPortfolioFound() {
        when(portfolioRepository.findPortfolioIdByUserId(99L)).thenReturn(null);
        Map<String, Object> result = service.getAnalytics(99L);
        assertTrue(result.containsKey("error"));
    }

    @Test
    void skipsSymbolsWithNoMarketData() {
        when(portfolioRepository.findPortfolioIdByUserId(1L)).thenReturn(10L);
        when(portfolioRepository.findItemsByPortfolioId(10L))
                .thenReturn(List.of(new PortfolioItem("AAPL", 10, 100.0),
                                    new PortfolioItem("XYZ", 5, 50.0)));
        when(yahooFinanceService.getLatestPrice("AAPL")).thenReturn(110.0);
        when(yahooFinanceService.getHistoricalPrices("AAPL")).thenReturn(risingPrices());
        when(yahooFinanceService.getLatestPrice("XYZ")).thenReturn(null);

        Map<String, Object> result = service.getAnalytics(1L);

        assertFalse(result.containsKey("error"));
        @SuppressWarnings("unchecked")
        List<String> skipped = (List<String>) result.get("skippedSymbols");
        assertTrue(skipped.contains("XYZ"));
    }
}
