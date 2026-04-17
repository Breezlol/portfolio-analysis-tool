package com.portfolio.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.KeyHolder;

/**
 * Abstract base class for JDBC repositories.
 * Provides shared JdbcTemplate access and common key-extraction logic
 * so subclasses avoid duplicating constructor and error-handling boilerplate.
 */
public abstract class BaseRepository {

    protected final JdbcTemplate jdbc;

    protected BaseRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Extracts the generated primary key from a KeyHolder, throwing if absent. */
    protected long extractKey(KeyHolder keyHolder) {
        Number key = keyHolder.getKey();
        if (key == null) throw new IllegalStateException("Insert did not return a generated key");
        return key.longValue();
    }
}
