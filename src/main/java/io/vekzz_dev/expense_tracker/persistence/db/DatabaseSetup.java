package io.vekzz_dev.expense_tracker.persistence.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;

public class DatabaseSetup {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseSetup.class);

    public static void initialize() {
        try (Connection conn = DatabaseManager.getConnection()) {

            enableWal(conn);

            if (!expensesTableExists(conn)) {
                executeSchema(conn);
                LOGGER.info("Database initialized at {}", DatabaseManager.getDbPath());
            }
        } catch (SQLException e) {
            LOGGER.error("Database initialization failed", e);
            throw new IllegalStateException("Failed to initialize database", e);
        }
    }

    private static void enableWal(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {

            try (ResultSet rs = stmt.executeQuery("PRAGMA journal_mode")) {

                if (rs.next() && !"wal".equalsIgnoreCase(rs.getString(1))) {
                    stmt.execute("PRAGMA journal_mode=WAL");
                }
            }
        }
    }

    private static boolean expensesTableExists(Connection conn) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();

        try (ResultSet rs = meta.getTables(null, null,
                "expenses", null)) {
            return rs.next();
        }
    }

    private static void executeSchema(Connection conn) throws SQLException {
        boolean autoCommit = conn.getAutoCommit();
        conn.setAutoCommit(false);

        try (InputStream is = DatabaseSetup.class.getClassLoader()
                .getResourceAsStream("expenses_schema.sql")) {

            if (is == null) throw new SQLException("Schema not found");

            String script = new String(is.readAllBytes(), StandardCharsets.UTF_8);

            try (Statement stmt = conn.createStatement()) {

                for (String sql : script.split(";")) {
                    sql = sql.trim();
                    if (!sql.isBlank()) stmt.execute(sql);
                }
                conn.commit();

            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        } catch (IOException e) {
            LOGGER.error("Error while executing SQL schema", e);
            throw new SQLException(e);

        } finally {
            conn.setAutoCommit(autoCommit);
        }
    }
}