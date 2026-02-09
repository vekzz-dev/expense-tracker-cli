package io.vekzz_dev.expense_tracker.persistence.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseManager.class);

    private static final Path DEFAULT_DB_PATH = Path.of(System.getProperty("user.home"))
            .resolve(".expense_tracker")
            .resolve("expenses.db");

    private static Path customDbPath;

    public static void setDbPath(Path path) {
        customDbPath = path;
    }

    public static Path getDbPath() {
        return customDbPath != null ? customDbPath : DEFAULT_DB_PATH;
    }

    public static Connection getConnection() throws SQLException {
        Path dbPath = getDbPath();

        try {
            Files.createDirectories(dbPath.getParent());

        } catch (IOException e) {
            LOGGER.error("Could not create database directories: {}", dbPath.getParent(), e);
            throw new SQLException("Failed to connect to database", e);
        }

        return DriverManager.getConnection("jdbc:sqlite:" + dbPath);
    }
}