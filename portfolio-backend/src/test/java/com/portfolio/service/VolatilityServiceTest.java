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
    @Mock AlphaVantageService alphaVantageService;
    @InjectMocks VolatilityService service;

    private List<Double> risingPrices() {
        return List.of(110.0, 108.0, 105.0, 102.0, 100.0);
    }

    @Test
    void returnsVolatilityForValidPortfolio() {
        when(portfolioRepository.findPortfolioIdByUserId(1L)).thenReturn(10L);
        when(portfolioRepository.findItemsByPortfolioId(10L))
                .thenReturn(List.of(new PortfolioItem("AAPL", 10, 100.0)));
