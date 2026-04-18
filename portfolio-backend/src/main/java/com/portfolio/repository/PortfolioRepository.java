package com.portfolio.repository;

import com.portfolio.entity.PortfolioItem;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;

/**
 * JDBC-backed repository for portfolios and their items.
 * All write operations are non-transactional; callers must coordinate
 * delete-then-insert sequences within the same request thread.
 */
@Repository
public class PortfolioRepository extends BaseRepository {

    public PortfolioRepository(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    public Long findPortfolioIdByUserId(Long userId) {
        List<Long> ids = jdbc.query(
                "SELECT id FROM portfolios WHERE user_id = ?",
                (rs, rowNum) -> rs.getLong("id"),
                userId
        );
        return ids.isEmpty() ? null : ids.get(0);
    }

    public Long createPortfolio(Long userId) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO portfolios (user_id) VALUES (?)",
                    new String[]{"id"}
            );
            ps.setLong(1, userId);
            return ps;
        }, keyHolder);
        return extractKey(keyHolder);
    }

    public List<PortfolioItem> findItemsByPortfolioId(Long portfolioId) {
        return jdbc.query(
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
        jdbc.update(
                "INSERT INTO portfolio_items (portfolio_id, symbol, quantity, purchase_price) VALUES (?, ?, ?, ?)",
                portfolioId, item.getSymbol(), item.getQuantity(), item.getPurchasePrice()
        );
    }

    public void deleteItemsByPortfolioId(Long portfolioId) {
        jdbc.update("DELETE FROM portfolio_items WHERE portfolio_id = ?", portfolioId);
    }
}
