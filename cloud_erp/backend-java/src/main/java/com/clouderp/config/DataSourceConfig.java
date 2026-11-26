package com.clouderp.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;

@Configuration
public class DataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(DataSourceConfig.class);

    @Value("${spring.datasource.url:jdbc:postgresql://localhost:5432/clouderp}")
    private String pgUrl;

    @Value("${spring.datasource.username:postgres}")
    private String pgUser;

    @Value("${spring.datasource.password:postgres}")
    private String pgPassword;

    @Bean
    @Primary
    public DataSource dataSource() {
        DataSource ds;
        if (pgUrl != null && pgUrl.startsWith("jdbc:h2:")) {
            ds = DataSourceBuilder.create()
                    .driverClassName("org.h2.Driver")
                    .url(pgUrl)
                    .username(pgUser != null ? pgUser : "sa")
                    .password(pgPassword != null ? pgPassword : "")
                    .build();
        } else if (canConnectToPostgres(pgUrl, pgUser, pgPassword)) {
            log.info("[DATABASE] Successfully connected to PostgreSQL at: {}", pgUrl);
            ds = DataSourceBuilder.create()
                    .driverClassName("org.postgresql.Driver")
                    .url(pgUrl)
                    .username(pgUser)
                    .password(pgPassword)
                    .build();
        } else {
            log.warn("[DATABASE] PostgreSQL at {} is not reachable.", pgUrl);
            log.info("[DATABASE] Falling back to local embedded database with PostgreSQL compatibility mode.");

            File dataDir = new File("./data");
            if (!dataDir.exists()) {
                dataDir.mkdirs();
            }

            String h2Url = "jdbc:h2:file:./data/clouderp;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1";
            ds = DataSourceBuilder.create()
                    .driverClassName("org.h2.Driver")
                    .url(h2Url)
                    .username("sa")
                    .password("")
                    .build();
        }

        migrateSchema(ds);
        return ds;
    }

    private void migrateSchema(DataSource ds) {
        try (Connection conn = ds.getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            stmt.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE");
            stmt.execute("UPDATE users SET is_active = TRUE WHERE is_active IS NULL");

            // Migration for sales invoice & payment tracking fields
            stmt.execute("ALTER TABLE sales ADD COLUMN IF NOT EXISTS invoice_number VARCHAR(50)");
            stmt.execute("ALTER TABLE sales ADD COLUMN IF NOT EXISTS paid_amount DOUBLE PRECISION DEFAULT 0.0");
            stmt.execute("ALTER TABLE sales ADD COLUMN IF NOT EXISTS balance_amount DOUBLE PRECISION DEFAULT 0.0");
            stmt.execute("ALTER TABLE sales ADD COLUMN IF NOT EXISTS payment_status VARCHAR(20) DEFAULT 'UNPAID'");
            stmt.execute("ALTER TABLE sales ADD COLUMN IF NOT EXISTS due_date DATE");
            stmt.execute("UPDATE sales SET paid_amount = 0.0 WHERE paid_amount IS NULL");
            stmt.execute("UPDATE sales SET balance_amount = total_amount WHERE balance_amount IS NULL");
            stmt.execute("UPDATE sales SET payment_status = 'UNPAID' WHERE payment_status IS NULL");
            stmt.execute("UPDATE sales SET invoice_number = CONCAT('INV-', id) WHERE invoice_number IS NULL");
        } catch (Exception e) {
            log.debug("[DATABASE] Schema auto-migration note: {}", e.getMessage());
        }
    }

    private boolean canConnectToPostgres(String url, String user, String password) {
        try {
            Class.forName("org.postgresql.Driver");
            DriverManager.setLoginTimeout(3);
            try (Connection conn = DriverManager.getConnection(url, user, password)) {
                return conn != null && !conn.isClosed();
            }
        } catch (Exception e) {
            return false;
        }
    }
}
