package io.vekzz_dev.expense_tracker.persistence.db;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class DatabaseManagerTest {

    @TempDir
    Path tempDir;

    private Path testDbPath;

    @BeforeEach
    void setUp() {
        testDbPath = tempDir.resolve("test_expenses.db");
        DatabaseManager.setDbPath(testDbPath);
    }

    @AfterEach
    void tearDown() {
        DatabaseManager.setDbPath(null);
    }

    @Test
    void testGetConnection_createsDatabaseDirectory() throws SQLException {
        try (Connection conn = DatabaseManager.getConnection()) {
            assertThat(conn).isNotNull();
            assertThat(Files.exists(testDbPath)).isTrue();
            assertThat(conn.getMetaData().getURL()).contains("sqlite");
        }
    }

    @Test
    void testSetDbPath_configuresCustomPath() {
        Path customPath = Path.of("/custom/path/expenses.db");
        DatabaseManager.setDbPath(customPath);

        assertThat(DatabaseManager.getDbPath()).isEqualTo(customPath);
    }

    @Test
    void testGetDbPath_returnsCustomPath_whenSet() {
        Path customPath = tempDir.resolve("custom.db");
        DatabaseManager.setDbPath(customPath);

        assertThat(DatabaseManager.getDbPath()).isEqualTo(customPath);
    }

    @Test
    void testGetDbPath_returnsDefaultPath_whenNotSet() {
        DatabaseManager.setDbPath(null);

        Path defaultPath = Path.of(System.getProperty("user.home"))
                .resolve(".expense_tracker")
                .resolve("expenses.db");

        assertThat(DatabaseManager.getDbPath()).isEqualTo(defaultPath);
    }

    @Test
    void testGetConnection_throwsSQLException_onDirectoryFailure() {
        assumeTrue(System.getProperty("os.name").toLowerCase().contains("linux") ||
                        System.getProperty("os.name").toLowerCase().contains("mac"),
                "PosixFilePermissions only supported on Unix-like systems");

        Path readOnlyDir = tempDir.resolve("readonly");
        try {
            Files.createDirectory(readOnlyDir);
            Files.setPosixFilePermissions(readOnlyDir, java.util.Set.of(
                    PosixFilePermission.OWNER_READ
            ));
        } catch (IOException e) {
            fail("Failed to setup test directory", e);
        }

        Path impossiblePath = readOnlyDir.resolve("subdir").resolve("file.db");
        DatabaseManager.setDbPath(impossiblePath);

        Throwable thrown = catchThrowable(() -> DatabaseManager.getConnection());

        assertThat(thrown).isInstanceOf(SQLException.class)
                .hasMessageContaining("Failed to connect to database");
    }

    @Test
    void testGetConnection_returnsValidConnection() throws SQLException {
        try (Connection conn = DatabaseManager.getConnection()) {
            assertThat(conn).isNotNull();
            assertThat(conn.isValid(1)).isTrue();
            assertThat(conn.getMetaData().getDatabaseProductName()).isEqualTo("SQLite");
        }
    }
}
