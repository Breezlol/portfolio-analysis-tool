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
