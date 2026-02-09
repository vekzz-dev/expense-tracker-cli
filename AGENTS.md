# AGENTS.md - Guidelines for Coding Agents

## Build Commands

### Gradle Commands
- `./gradlew build` - Full build including tests
- `./gradlew test` - Run all tests
- `./gradlew test --tests io.vekzz_dev.expense_tracker.ClassName` - Run tests for specific class
- `./gradlew test --tests io.vekzz_dev.expense_tracker.ClassName.testMethodName` - Run single test
- `./gradlew compileJava` - Compile main sources
- `./gradlew compileTestJava` - Compile test sources
- `./gradlew classes` - Compile main and create classes
- `./gradlew shadowJar` - Create fat JAR with all dependencies
- `./gradlew clean` - Clean build artifacts

## Code Style Guidelines

### Package Structure
- Base package: `io.vekzz_dev.expense_tracker`
- Use subpackages by feature: `models`, `storage`, `services`, `cli`, etc.
- Package names: lowercase, underscore separators (`io.vekzz_dev`)

### Imports
- Group imports: third-party libraries first, then Java stdlib
- Avoid wildcard imports except for `java.util.*` and `org.junit.jupiter.api.*`
- Use explicit imports for `java.nio.file`, `java.sql`, `java.io`

### Naming Conventions
- **Classes**: PascalCase (`DatabaseManager`, `Expense`, `Main`)
- **Methods**: camelCase (`getConnection`, `initializeDatabase`)
- **Constants**: UPPER_SNAKE_CASE (`DB_PATH`, `LOGGER`)
- **Variables**: camelCase (`dbPath`, `customDbPath`)
- **Private methods**: camelCase, descriptive (`getDbPath`, `executeSQLScript`)
- **Test methods**: `testMethodName_scenario()` (`testGetConnection_createsDatabaseDirectory()`)

### Types and Data Structures
- **Java 21**: Use records for immutable data carriers (`record Expense(long id, String description, Money amount)`)
- **IDs**: Use `long` for database IDs
- **Monetary values**: Use `org.javamoney.moneta.Money`, store as INTEGER cents in SQLite
- **File paths**: Use `java.nio.file.Path` (not `String` or `java.io.File`)
- **Dates**: Use `java.time.LocalDateTime` in Java, convert to ISO-8601 strings for SQLite
- **Collections**: Prefer `List<T>` and `Map<K,V>` from `java.util`
- **Local variables**: Use `var` for type inference, especially in try-with-resources

### Error Handling
- Methods declare `throws SQLException` for database operations
- Wrap `IOException` in `SQLException` with descriptive message before rethrowing
- Always log errors with SLF4J, include context (paths, object states)
- Catch specific exceptions, never catch `Exception` broadly
- Use custom exceptions (DataAccessException, TransactionException) to wrap underlying errors

### Class Design
- Prefer static utility methods where stateless (e.g., `DatabaseManager.getConnection()`)
- Use package-private (no modifier) for methods used in testing
- Private methods for implementation details
- Keep classes focused on single responsibility
- DatabaseManager: static factory methods for connections
- Records: immutable data models

### Testing Guidelines
- Use JUnit 5 (`@Test`, `@BeforeEach`, `@AfterEach`)
- Use AssertJ for assertions with static imports (`import static org.assertj.core.api.Assertions.*`)
- Use `@TempDir` from `org.junit.jupiter.api.io.TempDir` for filesystem tests
- Test naming: `testMethodName_scenario_expected()`
- Arrange-Act-Assert pattern in test methods
- Use `catchThrowable(() -> methodCall())` for exception testing
- Group related tests in test classes by feature
- Reset static state in `@AfterEach` (e.g., `DatabaseManager.setDbPath(null)`)

### Code Organization
- Place static fields at top of class
- Public methods before private methods
- Use blank lines between method definitions
- Keep methods under 20 lines when possible
- Extract complex logic to private helper methods
- Comment in Spanish as per project language preference

### Database Operations
- Always close `Connection`, `Statement`, `ResultSet` with try-with-resources
- Use `PreparedStatement` for parameterized queries
- Check for table existence before creation (use `DatabaseMetaData.getTables()`)
- Use SQLite INTEGER PRIMARY KEY AUTOINCREMENT for IDs
- Store monetary amounts as INTEGER (cents) in SQLite, convert to Money in Java
- Enable WAL mode for better concurrency (PRAGMA journal_mode=WAL)
- SQL schema files: place in `src/main/resources`, use `DatabaseSetup.initialize()`

### DAO Pattern
- Define interfaces in `persistence.dao` package (e.g., `ExpenseDao`)
- Implementations in `persistence.dao.jdbc` package (e.g., `JdbcExpenseDao`)
- DAOs accept `Connection` in constructor
- Use custom exceptions (DataAccessException) to wrap SQLException
- Return `Optional<T>` for single-entity lookups that may not exist

### Mapper Pattern
- Create static mapper classes for database ↔ domain conversion (e.g., `ExpenseRowMapper`)
- Place mappers in `persistence.mapper` package
- Use utility mappers for conversions (e.g., `MoneyMapper.toMinor()`)
- Mapers handle type conversions (String → LocalDateTime, Integer → Money)

### Transaction Management
- Use `TransactionManager` for transaction boundaries
- Operations implement `TransactionalOperation<T>` functional interface
- TransactionManager handles commit/rollback automatically
- Pass `Connection` from TransactionManager to DAOs during operations

### Comments and Documentation
- Minimal inline comments - code should be self-documenting
- Use descriptive method and variable names
- Comment complex business logic in Spanish
- Javadoc not required for simple public methods
- Package-info.java can describe package purpose

### Dependencies (Available for Use)
- **CLI**: `info.picocli:picocli` - Command line parsing
- **Money**: `org.javamoney.moneta:moneta-core` - Monetary operations
- **Output**: `de.vandermeer:asciitable` - ASCII table formatting
- **Logging**: `org.slf4j` with `logback-classic` - Logging
- **Database**: `org.xerial:sqlite-jdbc` - SQLite JDBC driver
- **Testing**: `junit-jupiter`, `assertj-core`, `mockito-core`, `mockito-junit-jupiter`

### Git Commit Conventions
- Use Conventional Commits format: `type(scope): description`
- Types: `feat`, `fix`, `refactor`, `test`, `chore`, `build`, `docs`
- Keep subject line under 72 characters
- Add detailed body if needed (what and why, not how)

### Database Schema Conventions
- Table names: lowercase, singular (`expense`, `category`, `tag`)
- Columns: snake_case (`created_at`, `updated_at`)
- ID column: `id INTEGER PRIMARY KEY AUTOINCREMENT`
- Timestamps: store as TEXT in ISO-8601 format (`2024-02-08T14:30:00Z`)
- Money: store as INTEGER (cents) to avoid floating-point issues
- Foreign keys: reference table name + `_id` (`category_id`)

### Logging Guidelines
- Use SLF4J with parameterized logging: `LOGGER.info("Processing expense: {}", expense)`
- Log levels:
  - ERROR: System failures that prevent operation (database connection, file permissions)
  - WARN: Recoverable issues (missing optional config, deprecated usage)
  - INFO: Important business events (expense created, database initialized)
  - DEBUG: Detailed execution flow (method entry/exit, query details)
- Never log sensitive data (passwords, tokens, full financial data)
- Include context in log messages (IDs, paths, operation names)


