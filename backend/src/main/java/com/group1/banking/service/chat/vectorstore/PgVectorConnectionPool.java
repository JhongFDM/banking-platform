package com.group1.banking.service.chat.vectorstore;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * A small HikariCP connection pool to the separate Postgres+pgvector
 * instance, built by hand here rather than registered as a Spring-managed
 * DataSource bean. That's deliberate: this app's primary H2 datasource is
 * already the one auto-configured DataSource bean Spring's JPA setup
 * expects, and adding a second one at that level would create ambiguity
 * for anything that autowires DataSource without a qualifier.
 *
 * If pgvector.datasource.url is blank, no pool is ever created and
 * isConfigured() returns false -- the app starts up fine either way.
 */
@Component
public class PgVectorConnectionPool {

    @Value("${pgvector.datasource.url:}")
    private String url;

    @Value("${pgvector.datasource.username:}")
    private String username;

    @Value("${pgvector.datasource.password:}")
    private String password;

    @Value("${pgvector.api.timeout-ms:8000}")
    private long connectionTimeoutMs;

    private HikariDataSource dataSource;

    public boolean isConfigured() {
        return url != null && !url.isBlank();
    }

    public synchronized DataSource getDataSource() {
        if (!isConfigured()) {
            return null;
        }
        if (dataSource == null) {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(url);
            config.setUsername(username);
            config.setPassword(password);
            config.setPoolName("pgvector-pool");
            config.setMaximumPoolSize(5);
            config.setMinimumIdle(1);
            config.setConnectionTimeout(connectionTimeoutMs);
            dataSource = new HikariDataSource(config);
        }
        return dataSource;
    }

    @PreDestroy
    public void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}
