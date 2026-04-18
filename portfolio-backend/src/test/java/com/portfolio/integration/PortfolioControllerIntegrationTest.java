package com.portfolio.integration;

import com.portfolio.dto.PortfolioItemRequest;
import com.portfolio.entity.User;
import com.portfolio.service.YahooFinanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * End-to-end integration tests: real Spring context, real H2 database,
 * real controllers/services/repositories. Only the external Yahoo
 * Finance call is mocked so the tests stay hermetic.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PortfolioControllerIntegrationTest {

    @Autowired TestRestTemplate rest;
    @Autowired JdbcTemplate jdbc;
    @MockBean YahooFinanceService yahooFinanceService;

    @BeforeEach
    void cleanDatabase() {
        jdbc.execute("DELETE FROM portfolio_items");
        jdbc.execute("DELETE FROM portfolios");
        jdbc.execute("DELETE FROM users");
    }

    private User createUser(double deposit) {
        User u = new User();
        u.setName("Test User");
        u.setAge(30);
        u.setSex("F");
        u.setEmploymentStatus("Employed");
        u.setIncomeRange("50-75k");
        u.setDepositAmount(deposit);
        ResponseEntity<User> resp = rest.postForEntity("/users", u, User.class);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertNotNull(resp.getBody().getId());
        return resp.getBody();
    }

    @Test
    void createUserAndFetchById() {
        User saved = createUser(10000);

        ResponseEntity<User> get = rest.getForEntity("/users/" + saved.getId(), User.class);
        assertEquals(HttpStatus.OK, get.getStatusCode());
        assertEquals("Test User", get.getBody().getName());
        assertEquals(10000.0, get.getBody().getDepositAmount());
    }

    @Test
    void fetchUnknownUserReturns404() {
        ResponseEntity<String> resp = rest.getForEntity("/users/999999", String.class);
        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
    }

    @Test
    void savePortfolioThenFetchRoundTrip() {
        User user = createUser(10000);
        List<PortfolioItemRequest> items = List.of(
                new PortfolioItemRequest("AAPL", 10, 150.0),
                new PortfolioItemRequest("MSFT", 5, 300.0)
        );

        ResponseEntity<Map<String, Object>> save = rest.exchange(
                "/users/" + user.getId() + "/portfolio",
                HttpMethod.POST,
                new HttpEntity<>(items),
                new ParameterizedTypeReference<>() {}
        );
        assertEquals(HttpStatus.OK, save.getStatusCode());
        assertEquals("saved", save.getBody().get("status"));
        assertEquals(2, ((Number) save.getBody().get("count")).intValue());

        ResponseEntity<List<Map<String, Object>>> get = rest.exchange(
                "/users/" + user.getId() + "/portfolio",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );
        assertEquals(HttpStatus.OK, get.getStatusCode());
        List<Map<String, Object>> body = get.getBody();
        assertEquals(2, body.size());
        // AVL tree returns items sorted alphabetically
        assertEquals("AAPL", body.get(0).get("symbol"));
        assertEquals("MSFT", body.get(1).get("symbol"));
    }

    @Test
    void savePortfolioExceedingDepositReturns400() {
        User user = createUser(500);
        List<PortfolioItemRequest> items = List.of(
                new PortfolioItemRequest("AAPL", 10, 150.0)
        );

        ResponseEntity<Map<String, Object>> resp = rest.exchange(
                "/users/" + user.getId() + "/portfolio",
                HttpMethod.POST,
                new HttpEntity<>(items),
                new ParameterizedTypeReference<>() {}
        );
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertTrue(((String) resp.getBody().get("error")).contains("exceeds"));
    }

    @Test
    void portfolioValueComputesTotalAndAllocation() {
        User user = createUser(10000);
        rest.exchange(
                "/users/" + user.getId() + "/portfolio",
                HttpMethod.POST,
                new HttpEntity<>(List.of(
                        new PortfolioItemRequest("AAPL", 10, 100.0),
                        new PortfolioItemRequest("MSFT", 5, 200.0)
                )),
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );
        when(yahooFinanceService.getLatestPrice("AAPL")).thenReturn(150.0);
        when(yahooFinanceService.getLatestPrice("MSFT")).thenReturn(250.0);

        ResponseEntity<Map<String, Object>> resp = rest.exchange(
                "/users/" + user.getId() + "/portfolio/value",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        // 10*150 + 5*250 = 1500 + 1250 = 2750
        assertEquals(2750.0, ((Number) resp.getBody().get("totalValue")).doubleValue());
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> holdings = (List<Map<String, Object>>) resp.getBody().get("holdings");
        assertEquals(2, holdings.size());
        assertNotNull(resp.getBody().get("concentrationLabel"));
    }

    @Test
    void topMoversReturnsRankedGainersAndLosers() {
        User user = createUser(100000);
        rest.exchange(
                "/users/" + user.getId() + "/portfolio",
                HttpMethod.POST,
                new HttpEntity<>(List.of(
                        new PortfolioItemRequest("AAPL", 1, 100.0),
                        new PortfolioItemRequest("MSFT", 1, 100.0),
                        new PortfolioItemRequest("TSLA", 1, 100.0)
                )),
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );
        when(yahooFinanceService.getLatestPrice("AAPL")).thenReturn(150.0); // +50%
        when(yahooFinanceService.getLatestPrice("MSFT")).thenReturn(110.0); // +10%
        when(yahooFinanceService.getLatestPrice("TSLA")).thenReturn(70.0);  // -30%

        ResponseEntity<Map<String, Object>> resp = rest.exchange(
                "/users/" + user.getId() + "/portfolio/top-movers?k=2",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> gainers = (List<Map<String, Object>>) resp.getBody().get("topGainers");
        assertEquals("AAPL", gainers.get(0).get("symbol"));
    }

    @Test
    void portfolioRangeQueryReturnsOnlySymbolsInRange() {
        User user = createUser(100000);
        rest.exchange(
                "/users/" + user.getId() + "/portfolio",
                HttpMethod.POST,
                new HttpEntity<>(List.of(
                        new PortfolioItemRequest("AAPL", 1, 100.0),
                        new PortfolioItemRequest("GOOG", 1, 100.0),
                        new PortfolioItemRequest("MSFT", 1, 100.0),
                        new PortfolioItemRequest("TSLA", 1, 100.0)
                )),
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );

        ResponseEntity<Map<String, Object>> resp = rest.exchange(
                "/users/" + user.getId() + "/portfolio/range?from=G&to=N",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> rangeItems = (List<Map<String, Object>>) resp.getBody().get("items");
        assertEquals(2, rangeItems.size());
        assertEquals("GOOG", rangeItems.get(0).get("symbol"));
        assertEquals("MSFT", rangeItems.get(1).get("symbol"));
    }

    @Test
    void portfolioEndpointForUnknownUserReturns404() {
        ResponseEntity<String> resp = rest.getForEntity("/users/999999/portfolio", String.class);
        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
    }
}
