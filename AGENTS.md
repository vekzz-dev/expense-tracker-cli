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
- **Monetary values**: Use `org.javamoney.moneta.Money`
- **File paths**: Use `java.nio.file.Path` (not `String` or `java.io.File`)
- **Dates**: Use ISO-8601 strings stored as TEXT in SQLite
- **Collections**: Prefer `List<T>` and `Map<K,V>` from `java.util`

### Error Handling
- Methods declare `throws SQLException` for database operations
- Wrap `IOException` in `SQLException` with descriptive message before rethrowing
- Always log errors with SLF4J, include context (paths, object states)
- Catch specific exceptions, never catch `Exception` broadly

### Class Design
- Prefer static utility methods where stateless (e.g., `DatabaseManager.getConnection()`)
- Use package-private (no modifier) for methods used in testing
- Private methods for implementation details
- Keep classes focused on single responsibility
- DatabaseManager: static factory methods for connections
- Records: immutable data models

### Testing Guidelines
- Use JUnit 5 (`@Test`, `@BeforeEach`, `@AfterEach`)
- Use AssertJ for assertions (`assertThat(...).isNotNull()`)
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

### CLI Command Patterns
- Use picocli `@Command` annotation on command classes
- Subcommands: extend from base command or use `@Command(name = "subcommand")`
- Options: `@Option(names = {"-a", "--amount"})`
- Parameters: `@Parameters(index = "0")`
- Command classes: PascalCase with suffix `Command` (e.g., `AddExpenseCommand`)
- Group commands by feature in subpackages

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

### Configuration Handling
- Store configuration in `~/.expense_tracker/config.properties` or environment variables
- Use `java.util.Properties` for reading config files
- Provide sensible defaults for all configuration options
- Document required vs optional configuration
- Validate configuration at startup, fail fast with clear error messages

### Testing Patterns
- Integration tests: use real SQLite in-memory database (`jdbc:sqlite::memory:`)
- Unit tests: mock external dependencies with Mockito
- Arrange-Act-Assert structure in test methods
- Use `@BeforeEach` for test setup, `@AfterEach` for cleanup
- Test both happy path and error scenarios
