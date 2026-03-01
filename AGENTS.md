# AGENTS.md - Guidelines for Coding Agents

## Build Commands

### Gradle Commands
- `./gradlew build` - Full build including tests and shadow JAR
- `./gradlew test` - Run all tests
- `./gradlew test --tests "io.vekzz_dev.expense_tracker.ClassName"` - Run tests for specific class
- `./gradlew test --tests "io.vekzz_dev.expense_tracker.ClassName.testMethodName"` - Run single test method
- `./gradlew compileJava` - Compile main sources only
- `./gradlew shadowJar` - Create fat JAR with all dependencies (output: `build/libs/*.jar`)
- `./gradlew clean` - Clean build artifacts
- `./gradlew run` - Run the application (via `application` plugin)
- `./gradlew run --args="add --description 'Coffee' --amount 5.00"` - Run with CLI args

## Project Architecture

### Layered Architecture
```
cli/           → Command-line interface (Picocli commands)
service/       → Business logic layer (ExpenseService, ExpenseFilterService)
model/         → Domain models (Expense, ExpenseStatistics, ExpenseSummary)
persistence/   → Data access layer
  ├── dao/     → DAO interfaces
  ├── dao/jdbc/→ JDBC implementations
  ├── mapper/  → Row mappers for DB ↔ Domain conversion
  ├── factory/ → DAO factories (DaoFactory, JdbcDaoFactory)
  ├── db/      → Database management (DatabaseManager, DatabaseSetup)
  └── transaction/ → Transaction management
util/          → Utility classes (PeriodValidator, MoneyMapper)
exception/     → Custom exceptions
```

## Code Style Guidelines

### Package Structure
- Base package: `io.vekzz_dev.expense_tracker`
- Subpackages by layer: `model`, `service`, `persistence`, `util`, `exception`, `cli`
- Package names: lowercase, underscore separators (`io.vekzz_dev`)

### Imports
- Group imports: third-party libraries first, then Java stdlib
- Avoid wildcard imports except for `java.util.*` and `org.junit.jupiter.api.*`
- Use explicit imports for `java.nio.file`, `java.sql`, `java.io`
- Static imports for AssertJ: `import static org.assertj.core.api.Assertions.*`

### Naming Conventions
- **Classes**: PascalCase (`DatabaseManager`, `Expense`, `JdbcExpenseDao`)
- **Interfaces**: PascalCase, no prefix (`ExpenseDao`, `DaoFactory`)
- **Implementations**: Prefix with technology (`JdbcExpenseDao`)
- **Methods**: camelCase (`getConnection`, `initializeDatabase`)
- **Constants**: UPPER_SNAKE_CASE (`DB_PATH`, `LOGGER`)
- **Variables**: camelCase (`dbPath`, `customDbPath`)
- **Test methods**: `testMethodName_scenario_expected()` (`testInsert_insertsExpenseAndReturnsId()`)

### Types and Data Structures
- **Java 21**: Use records for immutable data carriers (`record Expense(long id, String description, Money amount)`)
- **IDs**: Use `long` for database IDs
- **Monetary values**: Use `org.javamoney.moneta.Money`, store as INTEGER cents in SQLite
- **File paths**: Use `java.nio.file.Path` (not `String` or `java.io.File`)
- **Dates/Times**: Use `java.time.LocalDateTime`, `LocalDate`, `YearMonth`, `Year`
- **Collections**: Prefer `List<T>`, `Map<K,V>`, `Optional<T>` from `java.util`
- **Local variables**: Use `var` for type inference, especially in try-with-resources

### Error Handling

#### Exception Hierarchy
- `DomainException` (abstract) → Business rule violations
- `InfrastructureException` (abstract) → Technical failures
- Custom exceptions: `ExpenseNotFoundException`, `InvalidExpenseException`, `InvalidPeriodTypeException`, `ExpenseAddingFailedException`, `ExpenseUpdateFailedException` extend `DomainException`
- `TransactionException`, `DataAccessException` extend `InfrastructureException`

#### Guidelines
- Never catch `Exception` broadly - catch specific exceptions
- Always log errors with SLF4J, include context (paths, IDs, states)
- TransactionManager re-throws `DomainException` and `InfrastructureException` without wrapping

### Class Design
- Prefer static utility methods where stateless (`DatabaseManager.getConnection()`)
- Use package-private (no modifier) for methods used in testing
- Private methods for implementation details
- Single responsibility per class
- Records for immutable data models with validation in compact constructor

### Testing Guidelines
- JUnit 5: `@Test`, `@BeforeEach`, `@AfterEach`
- AssertJ for assertions: `assertThat(result).isEqualTo(expected)`
- `@TempDir` for filesystem tests: `Path tempDir`
- `catchThrowable(() -> methodCall())` for exception testing
- Reset static state in `@AfterEach`: `DatabaseManager.setDbPath(null)`
- Arrange-Act-Assert pattern; integration tests use real database with `@TempDir`

### Code Organization
- Static fields at top of class; public methods before private methods
- Blank lines between method definitions; methods under 20 lines when possible
- Extract complex logic to private helper methods
- Comments in Spanish for complex business logic

## Database Operations

### Connection Management
- `DatabaseManager.getConnection()` - Static factory for connections
- Always use try-with-resources for `Connection`, `Statement`, `ResultSet`
- Enable WAL mode: `PRAGMA journal_mode=WAL`

### DAO Pattern
- Interfaces in `persistence.dao` package
- Implementations in `persistence.dao.jdbc` package
- DAOs accept `Connection` in constructor
- Return `Optional<T>` for single-entity lookups

### Mapper Pattern
- Static mapper classes for DB ↔ Domain conversion
- `ExpenseRowMapper` for result set mapping
- `MoneyMapper.toMinor()` for Money → cents conversion

### Transaction Management
```java
tx.execute(conn -> {
    var dao = factoryProvider.apply(conn).expenseDao();
    return dao.findById(id);
});
```
- `TransactionManager` handles commit/rollback automatically
- Domain/Infrastructure exceptions propagate without wrapping
- Pass `Connection` from TransactionManager to DAOs

### Schema Conventions
- Table names: lowercase, singular (`expense`); Columns: snake_case (`created_at`)
- ID: `id INTEGER PRIMARY KEY AUTOINCREMENT`
- Timestamps: TEXT in ISO-8601 format; Money: INTEGER (cents)

## Logging Guidelines
- SLF4J with parameterized logging: `LOGGER.info("Processing expense: {}", id)`
- Levels: ERROR (system failures), WARN (recoverable), INFO (business events), DEBUG (flow details)
- Never log sensitive data

## Dependencies
- **CLI**: `info.picocli:picocli:4.7.5`
- **Money**: `org.javamoney.moneta:moneta-core:1.4.4`
- **Output**: `de.vandermeer:asciitable:0.3.2`
- **Logging**: `org.slf4j:slf4j-api` + `ch.qos.logback:logback-classic`
- **Database**: `org.xerial:sqlite-jdbc:3.50.3.0`
- **Testing**: `junit-jupiter`, `assertj-core`, `mockito-core`, `mockito-junit-jupiter`

## Git Commit Conventions
- Format: `type(scope): description`
- Types: `feat`, `fix`, `refactor`, `test`, `chore`, `build`, `docs`
- Subject under 72 characters
