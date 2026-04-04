package com.portfolio.repository;

import com.portfolio.entity.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

/**
 * JDBC-backed repository for user accounts.
 */
@Repository
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<User> findAll() {
        return jdbcTemplate.query("SELECT * FROM users", (rs, rowNum) -> {
            User user = new User();
            user.setId(rs.getLong("id"));
            user.setName(rs.getString("name"));
            user.setAge(rs.getInt("age"));
            user.setSex(rs.getString("sex"));
            user.setEmploymentStatus(rs.getString("employment_status"));
            user.setIncomeRange(rs.getString("income_range"));
            user.setDepositAmount(rs.getDouble("deposit_amount"));
            return user;
        });
    }

    public Optional<User> findById(Long id) {
        List<User> results = jdbcTemplate.query(
                "SELECT * FROM users WHERE id = ?",
                (rs, rowNum) -> {
                    User user = new User();
                    user.setId(rs.getLong("id"));
                    user.setName(rs.getString("name"));
                    user.setAge(rs.getInt("age"));
                    user.setSex(rs.getString("sex"));
                    user.setEmploymentStatus(rs.getString("employment_status"));
                    user.setIncomeRange(rs.getString("income_range"));
                    user.setDepositAmount(rs.getDouble("deposit_amount"));
                    return user;
                },
                id
        );
        return results.stream().findFirst();
    }

    public User save(User user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO users (name, age, sex, employment_status, income_range, deposit_amount) VALUES (?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, user.getName());
            ps.setInt(2, user.getAge());
            ps.setString(3, user.getSex());
            ps.setString(4, user.getEmploymentStatus());
            ps.setString(5, user.getIncomeRange());
            ps.setDouble(6, user.getDepositAmount());
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) throw new IllegalStateException("Insert did not return a generated key");
        user.setId(key.longValue());
        return user;
    }

    public User update(User user) {
        jdbcTemplate.update(
                "UPDATE users SET name=?, age=?, sex=?, employment_status=?, income_range=?, deposit_amount=? WHERE id=?",
                user.getName(), user.getAge(), user.getSex(),
                user.getEmploymentStatus(), user.getIncomeRange(),
                user.getDepositAmount(), user.getId()
        );
        return user;
    }
}
