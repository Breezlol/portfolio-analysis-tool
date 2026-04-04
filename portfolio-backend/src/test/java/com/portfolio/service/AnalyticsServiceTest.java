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
class AnalyticsServiceTest {

    @Mock PortfolioRepository portfolioRepository;
    @Mock AlphaVantageService alphaVantageService;
    @InjectMocks AnalyticsService service;

    @Test
    void topGainersCorrectAndDescending() {
        when(portfolioRepository.findPortfolioIdByUserId(1L)).thenReturn(10L);
        when(portfolioRepository.findItemsByPortfolioId(10L)).thenReturn(List.of(
                new PortfolioItem("AAPL", 10, 100.0),
                new PortfolioItem("AMZN", 5, 100.0)
        ));
        when(alphaVantageService.getLatestPrice("AAPL")).thenReturn(120.0);
        when(alphaVantageService.getLatestPrice("AMZN")).thenReturn(150.0);

        Map<String, Object> result = service.getTopMovers(1L, 2);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> gainers = (List<Map<String, Object>>) result.get("topGainers");
        assertEquals(2, gainers.size());
        assertEquals("AMZN", gainers.get(0).get("symbol")); // 50% gain first
        assertEquals("AAPL", gainers.get(1).get("symbol")); // 20% gain second
    }

    @Test
    void topLosersCorrectAndAscending() {
        when(portfolioRepository.findPortfolioIdByUserId(1L)).thenReturn(10L);
        when(portfolioRepository.findItemsByPortfolioId(10L)).thenReturn(List.of(
                new PortfolioItem("MSFT", 10, 100.0),
                new PortfolioItem("TSLA", 5, 100.0)
        ));
        when(alphaVantageService.getLatestPrice("MSFT")).thenReturn(90.0);
        when(alphaVantageService.getLatestPrice("TSLA")).thenReturn(70.0);

        Map<String, Object> result = service.getTopMovers(1L, 2);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> losers = (List<Map<String, Object>>) result.get("topLosers");
        assertEquals(2, losers.size());
        assertEquals("TSLA", losers.get(0).get("symbol")); // -30% worst first
        assertEquals("MSFT", losers.get(1).get("symbol")); // -10% second
    }

    @Test
    void kLargerThanPortfolioReturnsAll() {
        when(portfolioRepository.findPortfolioIdByUserId(1L)).thenReturn(10L);
        when(portfolioRepository.findItemsByPortfolioId(10L)).thenReturn(List.of(
                new PortfolioItem("AAPL", 1, 100.0)
        ));
        when(alphaVantageService.getLatestPrice("AAPL")).thenReturn(120.0);
