package io.vekzz_dev.expense_tracker.persistence;

import io.vekzz_dev.expense_tracker.persistence.db.DatabaseManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.*;

class DatabaseManagerTest {

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        Path testDbPath = tempDir.resolve("test_expenses.db");
        DatabaseManager.setDbPath(testDbPath);
    }

    @AfterEach
    void tearDown() {
        DatabaseManager.setDbPath(null);
    }

    @Test
    void testGetConnection_createsDatabaseDirectory() throws SQLException {
        Path dbPath = tempDir.resolve("test_expenses.db");

        try (Connection conn = DatabaseManager.getConnection()) {
            assertThat(conn).isNotNull();
            assertThat(Files.exists(dbPath)).isTrue();
        }
    }

    @Test
    void testInitializeDatabase_whenTablesNotExist() throws SQLException {
        try (Connection conn = DatabaseManager.getConnection()) {
            DatabaseManager.initializeDatabase(conn);

            DatabaseMetaData metaData = conn.getMetaData();
            try (ResultSet rs = metaData.getTables(null, null, "expenses", null)) {
                assertThat(rs.next()).isTrue();
                assertThat(rs.getString("TABLE_NAME")).isEqualTo("expenses");
            }
        }
    }

    @Test
    void testInitializeDatabase_whenTablesExist() throws SQLException {
        try (Connection conn = DatabaseManager.getConnection()) {
            DatabaseManager.initializeDatabase(conn);

            DatabaseMetaData metaData = conn.getMetaData();
            try (ResultSet rsBefore = metaData.getTables(null, null, "expenses", null)) {
                assertThat(rsBefore.next()).isTrue();
            }

            DatabaseManager.initializeDatabase(conn);

            try (ResultSet rsAfter = metaData.getTables(null, null, "expenses", null)) {
                assertThat(rsAfter.next()).isTrue();
            }
        }
    }

    @Test
    void testExecuteSQLScript_withValidResource() throws SQLException {
        try (Connection conn = DatabaseManager.getConnection()) {
            DatabaseManager.executeSQLScript(conn);

            DatabaseMetaData metaData = conn.getMetaData();
            try (ResultSet rs = metaData.getTables(null, null, "expenses", null)) {
                assertThat(rs.next()).isTrue();
            }

            try (ResultSet rs = conn.getMetaData().getColumns(null, null, "expenses", null)) {
                assertThat(rs.next()).isTrue();
                assertThat(rs.getString("COLUMN_NAME")).isEqualTo("id");
                assertThat(rs.next()).isTrue();
                assertThat(rs.getString("COLUMN_NAME")).isEqualTo("description");
                assertThat(rs.next()).isTrue();
                assertThat(rs.getString("COLUMN_NAME")).isEqualTo("amount");
                assertThat(rs.next()).isTrue();
                assertThat(rs.getString("COLUMN_NAME")).isEqualTo("created_at");
                assertThat(rs.next()).isTrue();
                assertThat(rs.getString("COLUMN_NAME")).isEqualTo("updated_at");
            }
        }
    }

    @Test
    void testExecuteSQLScript_handlesNullConnection() {
        Throwable thrown = catchThrowable(() -> DatabaseManager.executeSQLScript(null));

        assertThat(thrown).isInstanceOf(NullPointerException.class);
    }

    @Test
    void testGetConnection_handlesDirectoryCreationError() {
        Path readOnlyDir = tempDir.resolve("readonly");
        try {
            Files.createDirectory(readOnlyDir);
            Files.setPosixFilePermissions(readOnlyDir, java.util.Set.of(
                java.nio.file.attribute.PosixFilePermission.OWNER_READ
            ));
        } catch (IOException e) {
            fail("Failed to setup test directory", e);
        }

        Path impossiblePath = readOnlyDir.resolve("subdir").resolve("file.db");
        DatabaseManager.setDbPath(impossiblePath);

        Throwable thrown = catchThrowable(() -> DatabaseManager.getConnection());

        assertThat(thrown).isInstanceOf(SQLException.class);
    }
}