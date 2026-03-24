package com.portfolio.repository;

import com.portfolio.entity.PortfolioItem;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class PortfolioRepository {

    private final JdbcTemplate jdbcTemplate;

    public PortfolioRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long findPortfolioIdByUserId(Long userId) {
        List<Long> ids = jdbcTemplate.query(
                "SELECT id FROM portfolios WHERE user_id = ?",
                (rs, rowNum) -> rs.getLong("id"),
                userId
        );
        return ids.isEmpty() ? null : ids.get(0);
    }

    public Long createPortfolio(Long userId) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO portfolios (user_id) VALUES (?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setLong(1, userId);
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public List<PortfolioItem> findItemsByPortfolioId(Long portfolioId) {
        return jdbcTemplate.query(
                "SELECT symbol, quantity, purchase_price FROM portfolio_items WHERE portfolio_id = ?",
                (rs, rowNum) -> new PortfolioItem(
                        rs.getString("symbol"),
                        rs.getInt("quantity"),
                        rs.getDouble("purchase_price")
                ),
                portfolioId
        );
    }

    public void saveItem(Long portfolioId, PortfolioItem item) {
        jdbcTemplate.update(
                "INSERT INTO portfolio_items (portfolio_id, symbol, quantity, purchase_price) VALUES (?, ?, ?, ?)",
                portfolioId, item.getSymbol(), item.getQuantity(), item.getPurchasePrice()
        );
    }

    public void deleteItemsByPortfolioId(Long portfolioId) {
        jdbcTemplate.update("DELETE FROM portfolio_items WHERE portfolio_id = ?", portfolioId);
    }
}
