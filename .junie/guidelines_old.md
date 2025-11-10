# Development Guidelines

## Project Overview

This is a Kotlin-based project template following hexagonal architecture principles, built with Gradle and using HTTP4K framework for web services.

## Build/Configuration Instructions

### Prerequisites

- **JVM Version**: 21 (configured in `gradle.properties`)
- **Kotlin Version**: 2.2.0
- **Gradle**: Uses Gradle wrapper (./gradlew)

### Project Structure

The project follows a hexagonal architecture with these modules:

- `domain` - Core business logic
- `presentation:web` - Web API layer using HTTP4K
- `presentation:acceptance-test` - End-to-end tests
- `infrastructure:example-adapter` - External integrations

### Build Configuration

- **Version Catalog**: Dependencies managed via `gradle/libs.versions.toml`
- **Build Logic**: Shared conventions in `build-logic/` directory
- **Custom Plugins**: Located in `gradle/plugins/` directory
- **Performance**: Gradle caching and parallel builds enabled

### Key Build Commands

```bash
# Build the entire project
./gradlew build

# Run all tests with coverage reports
./gradlew test


# Run Tests for Specific Module
./gradlew :domain:test
./gradlew :presentation:web:test
./gradlew :presentation:acceptance-test:test

# Run Single Test Class
./gradlew test --tests "ExampleTest"

# Run Tests with Coverage
# Coverage reports are generated at `build/reports/jacoco/test/html/index.html`
./gradlew test jacocoTestReport

# Check for dependency updates
./gradlew dependencyUpdates --no-parallel

# Run static analysis
./gradlew detekt
```

### Module-specific Build Files

Each module uses convention plugins from `build-logic`:

- `kotlin-common-conventions` - Base Kotlin setup
- `kotlin-domain-conventions` - Domain-specific configuration
- `kotlin-adapter-conventions` - Adapter-specific configuration

## Testing Information

### Testing Framework Stack

- **JUnit 6**: Primary testing framework
- **Kotest**: Assertion library with fluent DSL
- **MockK**: Mocking framework for Kotlin
- **HTTP4K Testing**: Specialized testing utilities for HTTP4K applications


### Test Structure and Conventions

#### Test File Location

- Source: `src/main/kotlin`
- Tests: `src/test/kotlin`

#### Test Example

```kotlin
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName

@DisplayName("Example Test Suite")
class ExampleTest {

    @Test
    @DisplayName("Should demonstrate basic assertions")
    fun `should demonstrate basic kotest assertions`() {
        val result = "Hello World"

        result shouldBe "Hello World"
        result shouldContain "World"
        result.length shouldBe 11
    }
}
```

#### Adding New Tests

1. Create test classes in `src/test/kotlin` directory
2. Use `@Test` annotation from JUnit 6
3. Use Kotest matchers for assertions (`shouldBe`, `shouldContain`, etc.)
4. Use descriptive test names with backticks for readability

### Test Configuration

- **Platform**: Uses JUnit Platform via `useJUnitPlatform()`
- **Logging**: Tests log PASSED, SKIPPED, and FAILED events
- **Coverage**: JaCoCo integration with automatic report generation
- **Parallel Execution**: Enabled via Gradle configuration

## Development Information

### Code Style and Quality

#### Detekt Configuration

- **Configuration File**: `config/detekt/detekt.yml`
- **Key Rules**:
    - Max line length: 200 characters
    - Cyclomatic complexity threshold: 15
    - Long method threshold: 60 lines
    - Long parameter list: 6 parameters max
    - Nested block depth: 4 levels max

#### Kotlin Code Style

- **Style Guide**: Official Kotlin code style (`kotlin.code.style = official`)
- **Naming Conventions**:
    - Classes: PascalCase
    - Functions/Variables: camelCase
    - Constants: UPPER_SNAKE_CASE
    - Package names: lowercase with dots

#### Running Code Quality Checks

```bash
# Run Detekt static analysis
./gradlew detekt

# Check dependency vulnerabilities
./gradlew dependencyCheckAnalyze
```

### Architecture Patterns

#### Hexagonal Architecture

- **Domain**: Pure business logic, no external dependencies
- **Presentation**: HTTP4K-based web layer, acceptance tests
- **Infrastructure**: External integrations and adapters

#### Key Libraries

- **HTTP4K**: Web framework with functional approach
- **Result4K**: Functional error handling
- **Jackson**: JSON serialization
- **Kotest**: Testing assertions

### Development Workflow

#### Dependency Management

- All dependencies defined in `gradle/libs.versions.toml`
- Version updates managed via Ben Manes plugin
- Security scanning with OWASP dependency check

#### Build Optimizations

- **Gradle Daemon**: Enabled for faster builds
- **Build Cache**: Enabled for incremental builds
- **Parallel Execution**: Enabled for multi-module builds
- **Configuration Cache**: Available for faster configuration

### Debugging and Troubleshooting

#### Common Issues

1. **Build Failures**: Check JVM version (must be 21)
2. **Test Failures**: Ensure proper Kotest syntax and imports
3. **Detekt Issues**: Fix code style violations or update configuration

#### IDE Configuration

- **IntelliJ IDEA**: Import as Gradle project
- **Code Style**: Uses official Kotlin style guide
- **Detekt Plugin**: Install for real-time code analysis

### Performance Considerations

- JaCoCo coverage reports generated after each test run
- Build performance optimized with caching and parallel execution
- Test execution optimized with JUnit Platform

### Version Information

- **Kotlin**: 2.x
- **HTTP4K**: 6.x
- **JUnit**: 6.x
- **Kotest**: 6.x
- **Detekt**: 2.x


# Project-specific guidelines:
- Run `gradle check` before and after making changes.

## Process:
- Check if the feature already exists.
- Strictly follow test-driven development as if you meant it using the steps below:
    - Add a little test
    - Run all the tests and watch the new one fail
    - Make a little change
    - Run all the tests and watch the new one pass
    - Refactor to remove duplication
    - Repeat until done
- Do not write production code without a failing test
- Minimise the amount of code that doesn't compile
- Minimise the time when code doesn't compile or tests don't pass
- After finishing the changes when the tests are passing, check if there are any warnings in the IDE and fix them
- Prefer consistency to making something better only in one part of the code.

## Code style:
- Declare public functions, classes first at the top of the file.
- Don't write comments unless the code is doing something unusual, and the comment explains why it was done this way.
- Remove unused variables, parameters, functions, classes
- Optimise imports
- Use full variable and parameter, e.g. for `Request` type use `request` instead of `req`
- Inline local variables with a single usage (if this doesn't make code worse in some other way)
- Assert on the whole object rather than each field individually (unless there is a reason to assert on each field)
- Make sure the style of the new code matches existing code.

## Kotlin code style:
- prefer functional code style when working with collections
    - use `forEach` instead of `for` loops
    - avoid using `continue` and `break`
- prefer read-only collections to mutable collections
- import enum values if possible
- use expression functions if possible
- use not-nullable types if possible
- don't use `internal` to expose functions and classes for testing
