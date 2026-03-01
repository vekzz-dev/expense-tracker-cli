# CHANGELOG

All notable changes to this project will be documented in this file.

## [1.0.0] - 2026-03-01

### Features
- Add summary header description to output
- Display $ symbol instead of USD in tables
- Redirect JUL logs to file and silence console output
- Implement custom exception handling for CLI
- Add file-based logging with daily rotation
- Complete main entry point with full dependency injection
- Provide expense summary command
- Build expense list command with multiple filters
- Introduce expense update command with partial modifications
- Enable expense deletion command
- Implement expense creation command
- Establish root expense command orchestrator
- Add ascii table formatter utility
- Add date formatter utility
- Extend expense update failure message handling
- Introduce partial update methods for expense record
- Add monetary string parsing with validation
- Introduce invalid amount format exception
- Implement expense lifecycle and summary orchestration
- Implement temporal filtering for expense queries
- Implement aggregate calculations for expense analytics
- Implement date parsing with format validation
- Define immutable records for expense statistics
- Introduce domain and infrastructure exception hierarchy
- Introduce factory for DAO instantiation
- Add convenience method for void operations
- Add validation and utility methods to Expense
- Implement boolean return values for update/delete
- Implement JDBC expense repository
- Define expense data access contract
- Implement database row to entity mapper
- Implement monetary amount conversion utility
- Add data access exception wrapper
- Introduce expense domain entity
- Integrate persistence layer with database initialization
- Extract database initialization logic into DatabaseSetup
- Implement transactional operation framework with rollback support
- Establish TransactionException for transaction failures
- Implement SQLite connection manager with auto-initialization
- Define expenses table schema with timestamps
- Initialize main application structure

### Bug Fixes
- Correct help message usage text
- Handle null values in partial expense updates
- Preserve domain exceptions during rollback
- Correct JdbcExpenseDao import after package refactor
- Correct mockito-inline version

### Refactors
- Align exception hierarchy with InfrastructureException
- Implement flexible expense update with partial modifications
- Delegate amount parsing to MoneyMapper
- Enhance validation error messages with prefix
- Enforce immutability with final modifiers
- Align imports with package structure
- Rename save to insert and add boolean return types
- Align mapper with package changes
- Align dao test methods with interface changes
- Correct package names to singular form
- Remove redundant imports from persistence test classes
- Streamline DatabaseManager to focus solely on connection management

### Documentation
- Add README with installation and usage guide
- Enhance AGENTS.md with architecture and coding guidelines
- Document persistence patterns and refine code standards
- Add AGENTS.md coding guidelines
- Add MIT license
- Update changelog for v1.0.0

### Tests
- Update assertions for CLI and UI changes
- Add ascii table formatter tests
- Write date formatter tests
- Include expense summary tests
- Generate expense list tests
- Implement expense update tests
- Develop expense deletion tests
- Create expense creation tests
- Add command orchestrator tests
- Verify services and validator behavior
- Validate void operation execution
- Verify not-found scenarios for CRUD operations
- Assert update and delete return values
- Validate expense CRUD operations
- Validate expense entity mapping
- Verify monetary conversion logic
- Introduce DatabaseSetup, TransactionManager and integration tests
- Update DatabaseManagerTest and relocate to db package
- Cover DatabaseManager connection and initialization logic

### Chores
- Upgrade sqlite-jdbc to 3.51.2.0
- Add jul-to-slf4j dependency for logging bridge
- Remove duplicate logback.xml from project root
- Silence logback internal configuration messages
- Translate exception messages to English
- Introduce Picocli testing support
- Remove wildcard imports in test files
- Add logback logging configuration
- Configure gradle build system
- Add gradle wrapper
- Add gitignore
