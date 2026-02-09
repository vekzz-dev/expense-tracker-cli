package io.vekzz_dev.expense_tracker.persistence.db;

import io.vekzz_dev.expense_tracker.persistence.db.DatabaseManager;
import io.vekzz_dev.expense_tracker.persistence.db.DatabaseSetup;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.*;

class DatabaseSetupTest {

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
    void testInitialize_createsDatabaseAndTable() {
        DatabaseSetup.initialize();

        assertThat(Files.exists(testDbPath)).isTrue();

        try (Connection conn = DatabaseManager.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            try (ResultSet rs = metaData.getTables(null, null, "expenses", null)) {
                assertThat(rs.next()).isTrue();
                assertThat(rs.getString("TABLE_NAME")).isEqualTo("expenses");
            }
        } catch (SQLException e) {
            fail("Database connection failed", e);
        }
    }

    @Test
    void testInitialize_enablesWalMode() {
        DatabaseSetup.initialize();

        try (Connection conn = DatabaseManager.getConnection()) {
            try (var stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("PRAGMA journal_mode")) {

                assertThat(rs.next()).isTrue();
                String journalMode = rs.getString(1);
                assertThat(journalMode).isEqualToIgnoringCase("wal");
            }
        } catch (SQLException e) {
            fail("Database query failed", e);
        }
    }

    @Test
    void testInitialize_doesNotRecreateTable_whenAlreadyExists() {
        DatabaseSetup.initialize();

        try (Connection conn = DatabaseManager.getConnection()) {
            try (var stmt = conn.createStatement()) {
                stmt.execute("INSERT INTO expenses (description, amount, created_at, updated_at) VALUES ('test', 100, '2024-01-01T00:00:00Z', '2024-01-01T00:00:00Z')");

                var rs = stmt.executeQuery("SELECT COUNT(*) FROM expenses");
                assertThat(rs.next()).isTrue();
                int initialCount = rs.getInt(1);
                assertThat(initialCount).isEqualTo(1);
            }
        } catch (SQLException e) {
            fail("Database query failed", e);
        }

        DatabaseSetup.initialize();

        try (Connection conn = DatabaseManager.getConnection()) {
            try (var stmt = conn.createStatement()) {
                var rs = stmt.executeQuery("SELECT COUNT(*) FROM expenses");
                assertThat(rs.next()).isTrue();
                int finalCount = rs.getInt(1);
                assertThat(finalCount).isEqualTo(1);
            }
        } catch (SQLException e) {
            fail("Database query failed", e);
        }
    }

    @Test
    void testInitialize_throwsIllegalStateException_onConnectionFailure() {
        Path invalidPath = Path.of("/invalid/nonexistent/path/expenses.db");
        DatabaseManager.setDbPath(invalidPath);

        Throwable thrown = catchThrowable(() -> DatabaseSetup.initialize());

        assertThat(thrown).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to initialize database");
    }

    @Test
    void testInitialize_createsAllTableColumns() {
        DatabaseSetup.initialize();

        try (Connection conn = DatabaseManager.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            try (ResultSet rs = metaData.getColumns(null, null, "expenses", null)) {

                String[] expectedColumns = {"id", "description", "amount", "created_at", "updated_at"};
                int columnCount = 0;

                while (rs.next()) {
                    String columnName = rs.getString("COLUMN_NAME");
                    assertThat(columnName).isIn((Object[]) expectedColumns);
                    columnCount++;
                }

                assertThat(columnCount).isEqualTo(expectedColumns.length);
            }
        } catch (SQLException e) {
            fail("Database metadata query failed", e);
        }
    }
}
