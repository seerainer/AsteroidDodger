# Asteroid Dodger (SWT 2D Game)

Asteroid Dodger is a small 2D arcade-style game built with the Standard Widget Toolkit (SWT) and Java. Pilot a spaceship through a field of falling asteroids, destroy what you can, pick up upgrades and power-ups, and try to survive as long as possible.

## Key features
- Player ship with directional movement and multiple weapon types (single, double, triple, rapid, spread).
- Weapon upgrades that drop during play and temporarily change firing behavior.
- Power-ups: Shield (temporary invulnerability), Health (restore one life), Score Multiplier (double score), Slow Motion (slows asteroids).
- Several asteroid types: Normal, Fast, Tank (multi-hit, displays health bar), Splitter (breaks into smaller asteroids when destroyed).
- Particle-based explosion effects and screen shake on collisions.
- Animated parallax starfield background with three depth layers.
- Level progression and dynamic difficulty scaling (faster asteroids and adjusted spawn rates over time).
- Combo scoring system that rewards consecutive asteroid destructions.
- Heads-up display (HUD) showing level, score, lives, active power-ups and timers.
- Pause and Game Over screens with stats and restart (R / SPACE) and quit (ESC) controls.
- Fullscreen toggle (F11) and a hidden cursor while fullscreen for an immersive experience.
- Frame-rate independent movement using delta time.
- Thread-safe input handling and UI loop using SWT timer and a dedicated UI thread.

## Controls
- Move: Arrow Keys
- Shoot: SPACE
- Pause / Resume: P
- Toggle Full Screen: F11
- Restart (on Game Over): R or SPACE
- Quit: ESC

## Build and run
1. Build with Gradle:
   Windows: `gradlew.bat build`
   Unix: `./gradlew build`

2. Run with Gradle:
   Windows: `gradlew.bat run`
   Unix: `./gradlew run`

## Project layout (important files)
- src/main/java/io/github/seerainer/game/
  - GameApp.java         - Main game loop, update and render logic
  - GameWindow.java      - Window and canvas management (fullscreen, hidden cursor)
  - GameState.java       - Game state enum (PLAYING, PAUSED, GAME_OVER)
  - util/Time.java       - Delta time calculation
  - input/InputHandler.java - Thread-safe key tracking with per-frame just-pressed detection
  - entities/            - Game entities and types:
    - Entity.java
    - Player.java
    - Bullet.java
    - Asteroid.java
    - AsteroidType.java
    - WeaponUpgrade.java
    - WeaponType.java
    - PowerUp.java
    - PowerUpType.java
    - Particle.java
    - Star.java

## Design notes
- Movement, spawning and visual effects are driven by a delta-time-aware update loop so gameplay remains consistent across frame rates.
- Collision detection uses simple axis-aligned bounding boxes (AABB) for efficient checks.
- SWT resources (fonts, colors, cursors) are created once and disposed of on exit to avoid resource leaks.
- Input handling is implemented with concurrent sets to minimize latency and enable safe use from the UI thread.

## License
This code is provided as a demonstration of a small Java/SWT game. See repository metadata for licensing details.

---

**Have fun playing and iterating on Asteroid Dodger!** 🚀💫