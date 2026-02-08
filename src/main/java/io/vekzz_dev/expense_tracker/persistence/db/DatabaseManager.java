package io.vekzz_dev.expense_tracker.persistence.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;

public class DatabaseManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseManager.class);

    private static final Path DEFAULT_DB_PATH = Path.of(System.getProperty("user.home"))
            .resolve(".expense_tracker")
            .resolve("expenses.db");

    private static Path customDbPath = null;

    public static void setDbPath(Path path) {
        customDbPath = path;
    }

    private static Path getDbPath() {
        return customDbPath != null ? customDbPath : DEFAULT_DB_PATH;
    }

    public static Connection getConnection() throws SQLException {
        Path dbPath = getDbPath();
        try {
            if (Files.notExists(dbPath.getParent())) Files.createDirectories(dbPath.getParent());

            Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath.toAbsolutePath());
            initializeDatabase(conn);

            return conn;

        } catch (IOException e) {
            LOGGER.error("Could not create database directories: {}", dbPath.getParent(), e);
            throw new SQLException("Failed to connect to database", e);
        }
    }

    public static void initializeDatabase(Connection conn) throws SQLException {
        // Verify if exists
        DatabaseMetaData metaData = conn.getMetaData();

        try (ResultSet rs = metaData.getTables(null, null,
                "expenses", null)) {

            if (!rs.next()) {
                // Create tables
                executeSQLScript(conn);
                LOGGER.info("Database initialized at {}", getDbPath());
            }
        }
    }

    public static void executeSQLScript(Connection conn) throws SQLException {
        try (InputStream is = DatabaseManager.class.getClassLoader()
                .getResourceAsStream("expenses_schema.sql")) {

            if (is == null) throw new SQLException("SQL script not found in resources");

            String script = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            String[] statements = script.split(";");

            try (Statement stmt = conn.createStatement()) {

                for (String sql : statements) {
                    sql = sql.trim();
                    if (!sql.isEmpty()) stmt.execute(sql);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Error while executing SQL schema", e);
            throw new SQLException("Failed to execute SQL schema", e);
        }
    }
}