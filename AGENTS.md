# Asteroid Dodger - Agent Guidelines

This document provides coding agents with essential information about this project's structure, conventions, and workflows.

## Project Overview

**Asteroid Dodger** is a Java 25 desktop game built with Eclipse SWT (Standard Widget Toolkit) and Gradle. The game supports both JVM and GraalVM native compilation for high-performance execution.

- **Language:** Java 25
- **Build System:** Gradle 9.4.1
- **GUI Framework:** Eclipse SWT 3.133.0
- **Testing:** JUnit Jupiter 6.0.3 + AssertJ 3.27.7
- **Main Class:** `io.github.seerainer.game.Main`
- **Package Root:** `io.github.seerainer.game`

## Build, Test, and Run Commands

### Basic Commands
```bash
./gradlew build                # Build project with all checks
./gradlew run                  # Run the game application
./gradlew clean                # Clean build artifacts
./gradlew check                # Run all verification tasks
```

### Testing Commands
```bash
./gradlew test                 # Run all tests (unit + integration)
./gradlew unitTest             # Run unit tests only (@Tag("unit"))
./gradlew integrationTest      # Run integration tests (@Tag("integration"))
./gradlew allTests             # Run all test suites sequentially
```

**Running a Single Test:**
```bash
# By test class
./gradlew test --tests "io.github.seerainer.game.entities.PlayerTest"

# By test method
./gradlew test --tests "io.github.seerainer.game.entities.PlayerTest.testTakeDamage"

# By pattern (all tests in entities package)
./gradlew test --tests "io.github.seerainer.game.entities.*"
```

### Native Compilation
```bash
./gradlew nativeCompile        # Compile to native executable
./gradlew nativeRun            # Run native binary
./gradlew nativeTest           # Run native test binary
```

### Distribution
```bash
./gradlew installDist          # Install as distribution
./gradlew distZip              # Create ZIP distribution
./gradlew distTar              # Create TAR distribution
```

## Code Style Guidelines

### Indentation and Formatting
- **Indentation:** Use TABS (not spaces)
- **Brace Style:** K&R style (opening brace on same line)
- **Line Length:** No strict limit, but keep code readable
- **No automatic formatter configured** - follow existing patterns

### Java Language Features
- **Use `final`:** Extensively use `final` for method parameters and local variables
- **Type Inference:** Use `var` for local variables when type is obvious
- **Switch Expressions:** Prefer `switch` expressions with `->` syntax
- **Records/Sealed Classes:** Not currently used in codebase
- **Modern Features:** Java 25 features are available but use conservatively

### Import Organization
1. Java standard library (`java.*`)
2. Third-party libraries (`org.eclipse.swt.*`)
3. Project internal (`io.github.seerainer.game.*`)

**Rules:**
- No wildcard imports for project code
- Explicit imports preferred
- Static imports used sparingly
- Group with blank lines between sections

**Example:**
```java
import java.security.SecureRandom;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;

import io.github.seerainer.game.entities.Player;
import io.github.seerainer.game.input.InputHandler;
```

### Naming Conventions

**Classes:** PascalCase
- `GameApp`, `InputHandler`, `WeaponUpgrade`
- Enums use `*Type` suffix: `AsteroidType`, `PowerUpType`
- Tests use `*Test` suffix: `PlayerTest`, `GameAppTest`

**Methods:** camelCase with prefixes
- `get*` - Getters: `getX()`, `getHealth()`
- `set*` - Setters: `setPosition()`
- `is*` - Boolean queries: `isAlive()`, `isRunning()`
- `has*` - Boolean state: `hasShield()`, `hasScoreMultiplier()`
- `update*` - Update methods: `updatePosition()`, `updatePowerUps()`
- `draw*` - Rendering: `drawHUD()`, `drawPlayer()`
- `spawn*` - Entity creation: `spawnAsteroid()`, `spawnBullet()`
- `add*` - State modification: `addScore()`, `addHealth()`

**Variables:** camelCase
- Descriptive names over abbreviations
- Common abbreviations: `dt` (delta time), `gc` (graphics context), `x`/`y` (coordinates)
- Constants: `UPPER_SNAKE_CASE`
- Boolean fields: `isFullScreen`, `hasShield`, `invulnerable`
- Timer fields: `*Timer` suffix: `shootCooldownTimer`, `invulnerabilityTimer`

**Fields:**
- Private by default
- Use `final` for immutable fields
- Static constants at top of class

### Type Safety and Nullability
- Avoid `null` through design (initialize objects upfront)
- Check SWT resource disposal: `if (font != null && !font.isDisposed())`
- Use boundary checks: `Math.min()`, `Math.max()`
- Throw exceptions for programming errors: `IllegalArgumentException`, `IllegalStateException`

### Error Handling
- Minimal try-catch blocks (only where necessary)
- Handle checked exceptions at boundaries
- Runtime exceptions for programming errors

**Examples:**
```java
// Prevent utility class instantiation
private Main() {
    throw new IllegalStateException("Main class");
}

// Exhaustive switch statements
default -> throw new IllegalArgumentException("Unexpected value: " + weaponType);

// Thread interruption (when appropriate to ignore)
catch (final InterruptedException ignored) {
    // Ignore
}
```

### Thread Safety
- Use `volatile` for shared state: `volatile boolean running`
- Use `synchronized` for critical sections
- Use concurrent collections: `ConcurrentHashMap.newKeySet()`
- Respect SWT Display thread model
- Use `Display.asyncExec()` for cross-thread UI updates

### Comments and Documentation
- Code should be self-documenting
- Use comments sparingly, only when necessary
- Javadoc for public APIs when needed
- No emoji in code or comments

## Project Structure

```
io.github.seerainer.game/
├── Main.java              # Entry point
├── GameApp.java          # Main game loop, update/render logic
├── GameWindow.java       # Window/canvas management
├── GameState.java        # Enum: MENU, PLAYING, PAUSED, GAME_OVER
├── entities/             # Game entities
│   ├── Entity.java       # Abstract base class
│   ├── Player.java       # Player entity
│   ├── Asteroid.java     # Asteroid entity
│   ├── Bullet.java       # Projectile entity
│   ├── PowerUp.java      # Power-up collectibles
│   ├── Particle.java     # Visual effects
│   └── *Type.java        # Enums
├── input/
│   └── InputHandler.java # Thread-safe input handling
└── util/
    └── Time.java         # Delta time calculation
```

## Testing Guidelines

### Test Organization
- **Location:** `src/test/java/io/github/seerainer/game/`
- **Tags:** Use `@Tag("unit")` or `@Tag("integration")`
- **Naming:** Test classes end with `Test` suffix

### Test Types
- **Unit Tests:** Fast, isolated, no threading/UI
- **Integration Tests:** Slower, may require SWT Display/threads (10min timeout)

### Test Configuration
- Parallel execution enabled
- Headless mode for UI testing
- 4GB max heap for tests
- Use AssertJ for fluent assertions

**Example:**
```java
@Test
@Tag("unit")
void testPlayerTakeDamage() {
    final var player = new Player(100, 100);
    player.takeDamage(1);
    assertThat(player.getHealth()).isEqualTo(2);
    assertThat(player.isInvulnerable()).isTrue();
}
```

## Common Patterns

### Entity System
- Base class: `Entity` (has position, size)
- Entities stored in lists managed by `GameApp`
- Iterator-based traversal allows safe removal during update

### Game Loop
- Fixed frame rate (~144 FPS, 7ms per frame)
- Delta-time based movement (frame-rate independent)
- Update, then render pattern

### Resource Management
- SWT resources (Font, Color, Cursor) must be disposed
- Dispose in reverse creation order
- Check `isDisposed()` before use

### State Management
- GameState enum for high-level states
- Boolean flags for features: `hasShield`, `invulnerable`
- Timer pattern: countdown timers (`float timer -= deltaTime`)

## Important Notes

- **Platform-specific:** SWT requires platform-specific artifacts (Windows/macOS/Linux, x86_64/aarch64)
- **macOS:** Requires `-XstartOnFirstThread` JVM argument
- **Fat JAR:** All dependencies bundled in single JAR
- **No linting tools:** Follow existing code patterns exactly
- **Eclipse IDE:** Project files committed (`.classpath`, `.project`)
- **License:** Unlicense (public domain)

## When Making Changes

1. **Read existing code first** - understand patterns before modifying
2. **Follow existing style** - match indentation, naming, structure
3. **Use `final` consistently** - on parameters and local variables
4. **Add tests** - unit tests for logic, integration tests for UI/threading
5. **Check resource disposal** - ensure SWT resources are cleaned up
6. **Respect threading** - use `Display.asyncExec()` for UI updates from other threads
7. **Test thoroughly** - run both unit and integration tests before committing

## Quick Reference

**File paths in error messages:** Use format `file_path:line_number` for easy navigation
```
Example: "Player collision detected in src/main/java/io/github/seerainer/game/GameApp.java:342"
```

**Common classes to reference:**
- Game loop: `GameApp.java`
- Player mechanics: `entities/Player.java`
- Input handling: `input/InputHandler.java`
- Window management: `GameWindow.java`
