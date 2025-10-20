package io.github.seerainer.game;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

import io.github.seerainer.game.entities.Asteroid;
import io.github.seerainer.game.entities.AsteroidType;
import io.github.seerainer.game.entities.Bullet;
import io.github.seerainer.game.entities.Particle;
import io.github.seerainer.game.entities.Player;
import io.github.seerainer.game.entities.PowerUp;
import io.github.seerainer.game.entities.PowerUpType;
import io.github.seerainer.game.entities.Star;
import io.github.seerainer.game.entities.WeaponType;
import io.github.seerainer.game.entities.WeaponUpgrade;
import io.github.seerainer.game.input.InputHandler;
import io.github.seerainer.game.util.Time;

public class GameApp {

    private static final int FRAME_MS = 7; // ~144 FPS
    private static final float STAR_SPAWN_INTERVAL = 0.05f;
    private static final int MAX_STARS = 128;
    // Level system
    private static final int ASTEROIDS_PER_LEVEL = 15;
    private static final float LEVEL_SPEED_MULTIPLIER = 1.15f;
    private static final float LEVEL_SPAWN_REDUCTION = 0.85f;
    // Upgrade spawn intervals
    private static final float WEAPON_SPAWN_INTERVAL = 15.0f;
    private static final float POWERUP_SPAWN_INTERVAL = 12.0f;
    private static final Random random = new SecureRandom();
    private volatile boolean running;
    private GameWindow gameWindow;
    private Display display;
    private Thread uiThread;
    // Game state
    private GameState gameState;
    private Player player;
    private InputHandler input;
    private List<Asteroid> asteroids;
    private List<Bullet> bullets;
    private List<Particle> particles;
    private List<Star> stars;
    private List<WeaponUpgrade> weaponUpgrades;
    private List<PowerUp> powerUps;
    // Spawning and difficulty
    private float spawnTimer;
    private final float initialSpawnInterval = 1.5f;
    private final float minSpawnInterval = 0.2f;
    private float spawnInterval;
    private float difficultyTimer;
    private int asteroidsDodged;
    private int asteroidsDestroyed;
    // Level system
    private int currentLevel;
    private int asteroidsDestroyedThisLevel;
    private float asteroidSpeedMultiplier;
    // Star spawning
    private float starSpawnTimer;
    // Upgrade spawning
    private float weaponSpawnTimer;
    private float powerUpSpawnTimer;
    // Screen shake
    private float screenShakeIntensity;
    private float screenShakeTimer;
    // UI resources
    private Font titleFont;
    private Font uiFont;
    private Font smallFont;
    private Color[] asteroidColors;
    private Color[] asteroidTypeColors;

    public static final Random getRandom() {
	return random;
    }

    private void addScreenShake(final float intensity) {
	screenShakeIntensity = intensity;
	screenShakeTimer = 0.3f;
    }

    private void advanceLevel() {
	currentLevel++;
	asteroidsDestroyedThisLevel = 0;

	// Increase asteroid speed only up to level 10
	if (currentLevel <= 10) {
	    asteroidSpeedMultiplier *= LEVEL_SPEED_MULTIPLIER;
	    spawnInterval = Math.max(minSpawnInterval, spawnInterval * LEVEL_SPAWN_REDUCTION);
	} else {
	    // After level 10, keep speed constant but spawn asteroids faster
	    spawnInterval = Math.max(minSpawnInterval * 0.3f, spawnInterval * 0.95f);
	}

	// Reset difficulty timer to prevent double-dipping
	difficultyTimer = 0;

	// Give player a health bonus every 10 levels
	if (currentLevel % 10 == 0) {
	    player.addHealth(1);
	}
    }

    private void cleanup() {
	if (titleFont != null && !titleFont.isDisposed()) {
	    titleFont.dispose();
	}
	if (uiFont != null && !uiFont.isDisposed()) {
	    uiFont.dispose();
	}
	if (smallFont != null && !smallFont.isDisposed()) {
	    smallFont.dispose();
	}
	if (asteroidColors != null) {
	    for (final var color : asteroidColors) {
		if (color != null && !color.isDisposed()) {
		    color.dispose();
		}
	    }
	}
	if (asteroidTypeColors != null) {
	    for (final var color : asteroidTypeColors) {
		if (color != null && !color.isDisposed()) {
		    color.dispose();
		}
	    }
	}
	if (gameWindow != null) {
	    gameWindow.dispose();
	}
    }

    private void createExplosion(final float x, final float y, final int particleCount) {
	for (var i = 0; i < particleCount; i++) {
	    final var angle = random.nextFloat() * (float) (2 * Math.PI);
	    final var speed = 50f + random.nextFloat() * 150f;
	    final var vx = (float) Math.cos(angle) * speed;
	    final var vy = (float) Math.sin(angle) * speed;
	    final var lifetime = 0.3f + random.nextFloat() * 0.5f;
	    particles.add(new Particle(x, y, vx, vy, lifetime));
	}
    }

    private void drawAnimatedStars(final GC gc, final int shakeX, final int shakeY) {
	gc.setForeground(display.getSystemColor(SWT.COLOR_WHITE));

	stars.forEach((final var star) -> {
	    final var brightness = star.getBrightness();
	    gc.setAlpha(brightness);

	    final var x = Math.round(star.getX() + shakeX);
	    final var y = Math.round(star.getY() + shakeY);

	    if (star.getLayer() == 2) {
		gc.drawPoint(x, y);
		gc.drawPoint(x - 1, y);
		gc.drawPoint(x + 1, y);
		gc.drawPoint(x, y - 1);
		gc.drawPoint(x, y + 1);
	    } else if (star.getLayer() == 1) {
		gc.drawPoint(x, y);
		gc.drawPoint(x - 1, y);
		gc.drawPoint(x + 1, y);
	    } else {
		gc.drawPoint(x, y);
	    }
	});

	gc.setAlpha(255);
    }

    private void drawGameOver(final GC gc, final Rectangle client) {
	// Background
	gc.setBackground(display.getSystemColor(SWT.COLOR_BLACK));
	gc.fillRectangle(client);

	// Draw static stars for game over screen
	drawStars(gc, client);

	// Game Over text
	gc.setFont(titleFont);
	gc.setForeground(display.getSystemColor(SWT.COLOR_RED));
	var text = "GAME OVER";
	var extent = gc.textExtent(text);
	gc.drawText(text, client.width / 2 - extent.x / 2, client.height / 2 - 120, true);

	// Stats
	gc.setFont(uiFont);
	gc.setForeground(display.getSystemColor(SWT.COLOR_WHITE));

	text = "Level Reached: " + currentLevel;
	extent = gc.textExtent(text);
	gc.drawText(text, client.width / 2 - extent.x / 2, client.height / 2 - 60, true);

	text = "Final Score: " + player.getScore();
	extent = gc.textExtent(text);
	gc.drawText(text, client.width / 2 - extent.x / 2, client.height / 2 - 30, true);

	text = "Asteroids Dodged: " + asteroidsDodged;
	extent = gc.textExtent(text);
	gc.drawText(text, client.width / 2 - extent.x / 2, client.height / 2, true);

	text = "Asteroids Destroyed: " + asteroidsDestroyed;
	extent = gc.textExtent(text);
	gc.drawText(text, client.width / 2 - extent.x / 2, client.height / 2 + 30, true);

	// Restart hint
	gc.setForeground(display.getSystemColor(SWT.COLOR_YELLOW));
	text = "Press R or SPACE to restart";
	extent = gc.textExtent(text);
	gc.drawText(text, client.width / 2 - extent.x / 2, client.height / 2 + 80, true);

	text = "Press ESC to quit";
	extent = gc.textExtent(text);
	gc.drawText(text, client.width / 2 - extent.x / 2, client.height / 2 + 110, true);
    }

    private void drawMenu(final GC gc, final Rectangle client) {
	// Background
	gc.setBackground(display.getSystemColor(SWT.COLOR_BLACK));
	gc.fillRectangle(client);

	// Draw animated stars for menu background
	drawAnimatedStars(gc, 0, 0);

	// Title
	gc.setFont(titleFont);
	gc.setForeground(display.getSystemColor(SWT.COLOR_CYAN));
	var text = "ASTEROID DODGER";
	var extent = gc.textExtent(text);
	gc.drawText(text, client.width / 2 - extent.x / 2, client.height / 3, true);

	// Instructions
	gc.setFont(uiFont);
	gc.setForeground(display.getSystemColor(SWT.COLOR_WHITE));

	text = "Survive the asteroid field!";
	extent = gc.textExtent(text);
	gc.drawText(text, client.width / 2 - extent.x / 2, client.height / 2 - 60, true);

	text = "Collect weapon upgrades and power-ups";
	extent = gc.textExtent(text);
	gc.drawText(text, client.width / 2 - extent.x / 2, client.height / 2 - 30, true);

	// Controls
	gc.setFont(smallFont);
	var yOffset = client.height / 2 + 20;

	text = "Controls:";
	extent = gc.textExtent(text);
	gc.drawText(text, client.width / 2 - extent.x / 2, yOffset, true);
	yOffset += 25;

	text = "Move: Arrow Keys";
	extent = gc.textExtent(text);
	gc.drawText(text, client.width / 2 - extent.x / 2, yOffset, true);
	yOffset += 20;

	text = "Shoot: SPACE";
	extent = gc.textExtent(text);
	gc.drawText(text, client.width / 2 - extent.x / 2, yOffset, true);
	yOffset += 20;

	text = "Pause: P";
	extent = gc.textExtent(text);
	gc.drawText(text, client.width / 2 - extent.x / 2, yOffset, true);

	// Start prompt
	gc.setFont(uiFont);
	gc.setForeground(display.getSystemColor(SWT.COLOR_YELLOW));
	text = "Press SPACE or ENTER to start";
	extent = gc.textExtent(text);

	// Pulsing effect
	final var pulse = (float) (Math.sin(System.currentTimeMillis() / 300.0) * 0.3 + 0.7);
	gc.setAlpha((int) (pulse * 255));
	gc.drawText(text, client.width / 2 - extent.x / 2, client.height - 100, true);
	gc.setAlpha(255);

	// ESC hint
	gc.setFont(smallFont);
	gc.setForeground(display.getSystemColor(SWT.COLOR_WHITE));
	text = "Press ESC to quit";
	extent = gc.textExtent(text);
	gc.drawText(text, client.width / 2 - extent.x / 2, client.height - 60, true);
    }

    private void drawHUD(final GC gc, final Rectangle client) {
	gc.setFont(uiFont);
	gc.setForeground(display.getSystemColor(SWT.COLOR_WHITE));

	// Level
	gc.drawText("Level: " + currentLevel, 10, 10, true);

	// Score
	gc.drawText("Score: " + player.getScore(), 10, 30, true);

	// Lives (draw hearts)
	var heartX = 10;
	final var heartY = 50;
	for (var i = 0; i < player.getHealth(); i++) {
	    gc.setForeground(display.getSystemColor(SWT.COLOR_RED));
	    gc.drawText("♥", heartX, heartY, true);
	    heartX += 20;
	}

	// Combo counter
	if (player.getComboCount() > 1) {
	    gc.setFont(titleFont);
	    gc.setForeground(display.getSystemColor(SWT.COLOR_YELLOW));
	    final var comboText = player.getComboCount() + "x COMBO!";
	    final var extent = gc.textExtent(comboText);
	    gc.drawText(comboText, client.width / 2 - extent.x / 2, 80, true);
	    gc.setFont(uiFont);
	}

	// Active power-ups display
	var powerUpY = 10;
	gc.setFont(smallFont);

	if (player.hasShield()) {
	    gc.setForeground(display.getSystemColor(SWT.COLOR_CYAN));
	    gc.drawText(new StringBuilder().append("SHIELD: ").append((int) Math.ceil(player.getShieldTimer()))
		    .append("s").toString(), client.width - 120, powerUpY, true);
	    powerUpY += 20;
	}

	if (player.hasScoreMultiplier()) {
	    gc.setForeground(display.getSystemColor(SWT.COLOR_YELLOW));
	    gc.drawText(new StringBuilder().append("2x SCORE: ")
		    .append((int) Math.ceil(player.getScoreMultiplierTimer())).append("s").toString(),
		    client.width - 120, powerUpY, true);
	    powerUpY += 20;
	}

	if (player.hasSlowMotion()) {
	    gc.setForeground(display.getSystemColor(SWT.COLOR_MAGENTA));
	    gc.drawText(new StringBuilder().append("SLOW-MO: ").append((int) Math.ceil(player.getSlowMotionTimer()))
		    .append("s").toString(), client.width - 120, powerUpY, true);
	    powerUpY += 20;
	}

	if (player.getWeaponUpgradeTimeRemaining() > 0) {
	    gc.setForeground(display.getSystemColor(SWT.COLOR_GREEN));
	    gc.drawText(
		    new StringBuilder().append(player.getCurrentWeapon().getDisplayName()).append(": ")
			    .append((int) Math.ceil(player.getWeaponUpgradeTimeRemaining())).append("s").toString(),
		    client.width - 120, powerUpY, true);
	}

	// Controls hint
	gc.setFont(smallFont);
	gc.setAlpha(180);
	gc.setForeground(display.getSystemColor(SWT.COLOR_WHITE));
	gc.drawText("P: Pause | ESC: Quit", client.width - 150, client.height - 25, true);
	gc.setAlpha(255);
    }

    private void drawPauseScreen(final GC gc, final Rectangle client) {
	// Semi-transparent overlay
	gc.setAlpha(200);
	gc.setBackground(display.getSystemColor(SWT.COLOR_BLACK));
	gc.fillRectangle(client);
	gc.setAlpha(255);

	gc.setFont(titleFont);
	gc.setForeground(display.getSystemColor(SWT.COLOR_YELLOW));
	var text = "PAUSED";
	var extent = gc.textExtent(text);
	gc.drawText(text, client.width / 2 - extent.x / 2, client.height / 2 - extent.y / 2, true);

	gc.setFont(uiFont);
	gc.setForeground(display.getSystemColor(SWT.COLOR_WHITE));
	text = "Press P to resume";
	extent = gc.textExtent(text);
	gc.drawText(text, client.width / 2 - extent.x / 2, client.height / 2 + 40, true);
    }

    private void drawStars(final GC gc, final Rectangle client) {
	// Draw static star field for game over screen
	gc.setForeground(display.getSystemColor(SWT.COLOR_WHITE));
	final var starCount = 50;
	for (var i = 0; i < starCount; i++) {
	    final var seed = i * 12345L; // Deterministic positions
	    final var x = (int) ((seed * 73) % client.width);
	    final var y = (int) ((seed * 97) % client.height);
	    final var brightness = (int) (100 + (seed % 156));
	    gc.setAlpha(brightness);
	    gc.drawPoint(x, y);
	}
	gc.setAlpha(255);
    }

    GameWindow getGameWindow() {
	return gameWindow;
    }

    private void handleInput(final double dt) {
	if (input == null) {
	    return;
	}

	// ESC to quit
	if (input.isKeyPressed(SWT.ESC)) {
	    if (gameWindow != null && !gameWindow.isDisposed()) {
		gameWindow.getShell().dispose();
	    }
	    return;
	}

	// F11 to toggle full screen
	if (input.isKeyJustPressed(SWT.F11) && (gameWindow != null && !gameWindow.isDisposed())) {
	    gameWindow.toggleFullScreen();
	}

	// Start game from menu
	if (gameState == GameState.MENU && (input.isKeyJustPressed(' ') || input.isKeyJustPressed(SWT.CR)
		|| input.isKeyJustPressed(SWT.KEYPAD_CR))) {
	    gameState = GameState.PLAYING;
	    return;
	}

	// R to restart when game over - use isKeyJustPressed to avoid rapid restarts
	if (gameState == GameState.GAME_OVER
		&& (input.isKeyJustPressed('r') || input.isKeyJustPressed('R') || input.isKeyJustPressed(' '))) {
	    resetGame();
	    return;
	}

	// P to pause - use isKeyJustPressed to avoid rapid toggling
	if (input.isKeyJustPressed('p') || input.isKeyJustPressed('P')) {
	    if (gameState == GameState.PLAYING) {
		gameState = GameState.PAUSED;
	    } else if (gameState == GameState.PAUSED) {
		gameState = GameState.PLAYING;
	    }
	}

	// Movement and shooting during play
	if (gameState != GameState.PLAYING) {
	    return;
	}
	updatePlayerMovement(dt);
	handleShooting();
    }

    private void handleShooting() {
	// SPACE to shoot
	if ((!input.isKeyPressed(' ') || !player.canShoot())) {
	    return;
	}
	player.shoot();

	// Create bullets based on weapon type
	final var weaponType = player.getCurrentWeapon();
	final var centerX = player.getX() + player.getWidth() / 2;
	final var bulletY = player.getY();

	switch (weaponType) {
	case SINGLE -> bullets.add(new Bullet(centerX - 2, bulletY));
	case DOUBLE -> {
	    bullets.add(new Bullet(centerX - 10, bulletY));
	    bullets.add(new Bullet(centerX + 6, bulletY));
	}
	case TRIPLE -> {
	    bullets.add(new Bullet(centerX - 14, bulletY));
	    bullets.add(new Bullet(centerX - 2, bulletY));
	    bullets.add(new Bullet(centerX + 10, bulletY));
	}
	case RAPID -> bullets.add(new Bullet(centerX - 2, bulletY));
	case SPREAD -> {
	    // Create 5 bullets in a spread pattern
	    for (var i = 0; i < 5; i++) {
		final var angle = Math.toRadians(-60 + i * 30); // -60 to +60 degrees
		final var bullet = new Bullet(centerX - 2, bulletY);
		bullet.setAngle((float) angle);
		bullets.add(bullet);
	    }
	}
	default -> throw new IllegalArgumentException("Unexpected value: " + weaponType);
	}
    }

    private void initializeGame() {
	input = new InputHandler(display);
	asteroids = new ArrayList<>();
	bullets = new ArrayList<>();
	particles = new ArrayList<>();
	stars = new ArrayList<>();
	weaponUpgrades = new ArrayList<>();
	powerUps = new ArrayList<>();

	// Create fonts
	final var fontData = display.getSystemFont().getFontData();
	titleFont = new Font(display, fontData[0].getName(), 32, SWT.BOLD);
	uiFont = new Font(display, fontData[0].getName(), 14, SWT.NORMAL);
	smallFont = new Font(display, fontData[0].getName(), 11, SWT.NORMAL);

	// Create asteroid colors (shades of gray/brown)
	asteroidColors = new Color[] { new Color(display, 100, 100, 100), new Color(display, 120, 110, 100),
		new Color(display, 90, 85, 80), new Color(display, 110, 100, 90) };

	// Type-specific colors
	asteroidTypeColors = new Color[] { new Color(display, 100, 100, 100), // NORMAL - gray
		new Color(display, 255, 100, 100), // FAST - red
		new Color(display, 100, 100, 255), // TANK - blue
		new Color(display, 150, 100, 200) // SPLITTER - purple
	};

	resetGame();
    }

    boolean isRunning() {
	return running;
    }

    private void render(final GC gc) {
	final var client = gameWindow.getCanvas().getClientArea();

	// Apply screen shake
	final int shakeX;
	final int shakeY;
	if (screenShakeTimer > 0) {
	    shakeX = (int) ((random.nextFloat() - 0.5f) * 2 * screenShakeIntensity);
	    shakeY = (int) ((random.nextFloat() - 0.5f) * 2 * screenShakeIntensity);
	} else {
	    shakeX = 0;
	    shakeY = 0;
	}

	// Clear background
	gc.setBackground(display.getSystemColor(SWT.COLOR_BLACK));
	gc.fillRectangle(client);

	// Draw animated stars background
	drawAnimatedStars(gc, shakeX, shakeY);

	switch (gameState) {
	case PLAYING:
	case PAUSED: {
	    // Draw asteroids with type-specific colors
	    asteroids.forEach((final Asteroid asteroid) -> {
		// Color based on type
		final var typeIndex = asteroid.getType().ordinal();
		gc.setBackground(asteroidTypeColors[typeIndex]);

		final var x = Math.round(asteroid.getX() + shakeX);
		final var y = Math.round(asteroid.getY() + shakeY);
		final var size = asteroid.getSize();
		gc.fillOval(x, y, size, size);

		// Health bar for tank asteroids
		if (asteroid.getType() == AsteroidType.TANK && asteroid.getHitPoints() < asteroid.getMaxHitPoints()) {
		    final var barWidth = size;
		    final var barHeight = 3;
		    final var healthPercent = (float) asteroid.getHitPoints() / asteroid.getMaxHitPoints();

		    gc.setBackground(display.getSystemColor(SWT.COLOR_RED));
		    gc.fillRectangle(x, y - 5, barWidth, barHeight);
		    gc.setBackground(display.getSystemColor(SWT.COLOR_GREEN));
		    gc.fillRectangle(x, y - 5, (int) (barWidth * healthPercent), barHeight);
		}

		// Visual detail
		gc.setForeground(display.getSystemColor(SWT.COLOR_DARK_GRAY));
		gc.drawOval(x, y, size, size);
		gc.drawOval(x + size / 4, y + size / 4, size / 3, size / 3);
	    });
	    // Draw power-ups with pulsing effect
	    powerUps.forEach((final PowerUp powerUp) -> {
		final var px = Math.round(powerUp.getX() + shakeX);
		final var py = Math.round(powerUp.getY() + shakeY);
		final var pw = (int) powerUp.getWidth();
		final var ph = (int) powerUp.getHeight();

		// Pulsing effect
		final var pulse = (float) (Math.sin(powerUp.getPulseTimer() * 8) * 0.15 + 1);
		final var scaledW = (int) (pw * pulse);
		final var scaledH = (int) (ph * pulse);
		final var offsetX = (pw - scaledW) / 2;
		final var offsetY = (ph - scaledH) / 2;

		// Color based on type
		final var powerUpType = powerUp.getPowerUpType();
		switch (powerUpType) {
		case SHIELD -> gc.setBackground(display.getSystemColor(SWT.COLOR_CYAN));
		case HEALTH -> gc.setBackground(display.getSystemColor(SWT.COLOR_GREEN));
		case SCORE_MULTIPLIER -> gc.setBackground(display.getSystemColor(SWT.COLOR_YELLOW));
		case SLOW_MOTION -> gc.setBackground(display.getSystemColor(SWT.COLOR_MAGENTA));
		default -> throw new IllegalArgumentException("Unexpected value: " + powerUpType);
		}

		// Draw as circle
		gc.fillOval(px + offsetX, py + offsetY, scaledW, scaledH);
		gc.setForeground(display.getSystemColor(SWT.COLOR_WHITE));
		gc.drawOval(px + offsetX, py + offsetY, scaledW, scaledH);

		// Draw icon/letter
		gc.setFont(smallFont);
		final var letter = switch (powerUpType) {
		case SHIELD -> "S";
		case HEALTH -> "+";
		case SCORE_MULTIPLIER -> "2x";
		case SLOW_MOTION -> "⏱";
		};
		final var extent = gc.textExtent(letter);
		gc.drawText(letter, px + pw / 2 - extent.x / 2, py + ph / 2 - extent.y / 2, true);
	    });
	    // Draw weapon upgrades
	    weaponUpgrades.forEach((final WeaponUpgrade upgrade) -> {
		final var ux = Math.round(upgrade.getX() + shakeX);
		final var uy = Math.round(upgrade.getY() + shakeY);
		final var uw = (int) upgrade.getWidth();
		final var uh = (int) upgrade.getHeight();

		final var weaponType = upgrade.getWeaponType();
		switch (weaponType) {
		case DOUBLE -> gc.setBackground(display.getSystemColor(SWT.COLOR_GREEN));
		case TRIPLE -> gc.setBackground(display.getSystemColor(SWT.COLOR_BLUE));
		case RAPID -> gc.setBackground(display.getSystemColor(SWT.COLOR_RED));
		case SPREAD -> gc.setBackground(display.getSystemColor(SWT.COLOR_MAGENTA));
		default -> gc.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
		}

		final var centerX = ux + uw / 2;
		final var centerY = uy + uh / 2;
		final var radius = uw / 2;

		final int[] diamond = { centerX, centerY - radius, centerX + radius, centerY, centerX, centerY + radius,
			centerX - radius, centerY };
		gc.fillPolygon(diamond);
		gc.setForeground(display.getSystemColor(SWT.COLOR_WHITE));
		gc.drawPolygon(diamond);

		gc.setFont(uiFont);
		final var letter = switch (weaponType) {
		case DOUBLE -> "2";
		case TRIPLE -> "3";
		case RAPID -> "R";
		case SPREAD -> "S";
		default -> "?";
		};
		final var extent = gc.textExtent(letter);
		gc.drawText(letter, centerX - extent.x / 2, centerY - extent.y / 2, true);
	    });
	    // Draw bullets
	    gc.setBackground(display.getSystemColor(SWT.COLOR_YELLOW));
	    gc.setForeground(display.getSystemColor(SWT.COLOR_WHITE));
	    bullets.forEach((final Bullet bullet) -> {
		final var bx = Math.round(bullet.getX() + shakeX);
		final var by = Math.round(bullet.getY() + shakeY);
		final var bw = (int) bullet.getWidth();
		final var bh = (int) bullet.getHeight();
		gc.fillRectangle(bx, by, bw, bh);
		gc.drawRectangle(bx, by, bw, bh);
	    });
	    // Draw particles
	    gc.setBackground(display.getSystemColor(SWT.COLOR_YELLOW));
	    particles.forEach((final Particle particle) -> {
		final var alpha = (int) (particle.getAlpha() * 255);
		if (alpha > 0) {
		    gc.setAlpha(alpha);
		    gc.fillOval(Math.round(particle.getX() + shakeX), Math.round(particle.getY() + shakeY),
			    (int) particle.getWidth(), (int) particle.getHeight());
		}
	    });
	    gc.setAlpha(255);
	    // Draw player with shield effect
	    if (player.hasShield()) {
		// Pulsing shield circle
		final var shieldPulse = (float) (Math.sin(System.currentTimeMillis() / 100.0) * 0.2 + 1);
		final var shieldRadius = (int) (player.getWidth() * shieldPulse);
		gc.setAlpha(100);
		gc.setBackground(display.getSystemColor(SWT.COLOR_CYAN));
		gc.fillOval(Math.round(player.getX() + player.getWidth() / 2 - shieldRadius / 2 + shakeX),
			Math.round(player.getY() + player.getHeight() / 2 - shieldRadius / 2 + shakeY), shieldRadius,
			shieldRadius);
		gc.setAlpha(255);
	    }
	    // Draw player
	    if (player.isInvulnerable() && (System.currentTimeMillis() / 100) % 2 == 0) {
		gc.setAlpha(128);
	    }
	    gc.setBackground(display.getSystemColor(SWT.COLOR_CYAN));
	    final var px = Math.round(player.getX() + shakeX);
	    final var py = Math.round(player.getY() + shakeY);
	    final var pw = Math.round(player.getWidth());
	    final var ph = Math.round(player.getHeight());
	    final int[] triangle = { px + pw / 2, py, px, py + ph, px + pw, py + ph };
	    gc.fillPolygon(triangle);
	    gc.setForeground(display.getSystemColor(SWT.COLOR_WHITE));
	    gc.drawPolygon(triangle);
	    gc.setAlpha(255);
	    // Draw HUD
	    drawHUD(gc, client);
	    if (gameState == GameState.PAUSED) {
		drawPauseScreen(gc, client);
	    }
	    break;
	}
	case GAME_OVER:
	    drawGameOver(gc, client);
	    break;
	case MENU:
	    drawMenu(gc, client);
	    break;
	case null:
	default:
	    break;
	}
    }

    private void resetGame() {
	final var area = gameWindow.getCanvas().getClientArea();
	if (player == null) {
	    player = new Player(area.width / 2f - 16f, area.height - 80f);
	} else {
	    player.reset(area.width / 2f - 16f, area.height - 80f);
	}

	asteroids.clear();
	bullets.clear();
	particles.clear();
	stars.clear();
	weaponUpgrades.clear();
	powerUps.clear();

	// Initialize some stars to fill the screen
	for (var i = 0; i < 80; i++) {
	    final var x = random.nextFloat() * area.width;
	    final var y = random.nextFloat() * area.height;
	    stars.add(new Star(x, y));
	}

	spawnTimer = 0;
	spawnInterval = initialSpawnInterval;
	difficultyTimer = 0;
	asteroidsDodged = 0;
	asteroidsDestroyed = 0;
	starSpawnTimer = 0;
	weaponSpawnTimer = WEAPON_SPAWN_INTERVAL;
	powerUpSpawnTimer = POWERUP_SPAWN_INTERVAL;
	currentLevel = 1;
	asteroidsDestroyedThisLevel = 0;
	asteroidSpeedMultiplier = 1.0f;
	screenShakeIntensity = 0;
	screenShakeTimer = 0;
	gameState = GameState.MENU;
    }

    private void scheduleNextFrame() {
	if (display == null || display.isDisposed()) {
	    return;
	}
	display.timerExec(FRAME_MS, () -> {
	    if (!running || gameWindow == null || gameWindow.isDisposed()) {
		return;
	    }
	    Time.update();
	    update();
	    if (!gameWindow.getCanvas().isDisposed()) {
		gameWindow.getCanvas().redraw();
	    }
	    scheduleNextFrame();
	});
    }

    private void spawnAsteroid(final int canvasWidth) {
	final var x = random.nextFloat() * (canvasWidth - 50);

	// Determine asteroid type based on level
	var type = AsteroidType.NORMAL;
	final var typeRoll = random.nextFloat();

	if (currentLevel >= 3) {
	    if (typeRoll < 0.15f) {
		type = AsteroidType.FAST;
	    } else if (typeRoll < 0.25f && currentLevel >= 5) {
		type = AsteroidType.TANK;
	    } else if (typeRoll < 0.35f && currentLevel >= 4) {
		type = AsteroidType.SPLITTER;
	    }
	} else if (currentLevel >= 2 && typeRoll < 0.1f) {
	    type = AsteroidType.FAST;
	}

	final var asteroid = new Asteroid(x, -50, type);
	asteroids.add(asteroid);

	// Spawn additional asteroids at higher levels (every 10 levels adds a chance
	// for multi-spawn)
	if (currentLevel < 10) {
	    return;
	}
	final var multiSpawnChance = Math.min(0.5f, (currentLevel - 10) / 200.0f); // Up to 50% chance
	if (random.nextFloat() >= multiSpawnChance) {
	    return;
	}
	// Spawn 1-2 additional asteroids
	final var extraCount = 1 + random.nextInt(2);
	for (var i = 0; i < extraCount; i++) {
	    final var extraX = random.nextFloat() * (canvasWidth - 50);
	    var extraType = AsteroidType.NORMAL;
	    final var extraTypeRoll = random.nextFloat();

	    if (currentLevel >= 3) {
		if (extraTypeRoll < 0.15f) {
		    extraType = AsteroidType.FAST;
		} else if (extraTypeRoll < 0.25f && currentLevel >= 5) {
		    extraType = AsteroidType.TANK;
		} else if (extraTypeRoll < 0.35f && currentLevel >= 4) {
		    extraType = AsteroidType.SPLITTER;
		}
	    }

	    asteroids.add(new Asteroid(extraX, -50 - (i + 1) * 30, extraType));
	}
    }

    private void spawnPowerUp(final int canvasWidth) {
	final var x = random.nextFloat() * (canvasWidth - 50);
	final var types = PowerUpType.values();
	final var powerUpType = types[random.nextInt(types.length)];
	powerUps.add(new PowerUp(x, -50, powerUpType));
    }

    private void spawnWeaponUpgrade(final int canvasWidth) {
	final var x = random.nextFloat() * (canvasWidth - 50);
	final WeaponType[] types = { WeaponType.DOUBLE, WeaponType.TRIPLE, WeaponType.RAPID, WeaponType.SPREAD };
	final var weaponType = types[random.nextInt(types.length)];
	weaponUpgrades.add(new WeaponUpgrade(x, -50, weaponType));
    }

    synchronized void start() {
	if (running) {
	    return;
	}
	running = true;
	uiThread = new Thread(() -> {
	    display = new Display();
	    gameWindow = new GameWindow(display);

	    // Initialize game resources
	    initializeGame();

	    // Paint listener: render entire game
	    gameWindow.getCanvas().addPaintListener((final var e) -> render(e.gc));
	    gameWindow.open();

	    // Time init and schedule first frame
	    Time.init();
	    scheduleNextFrame();

	    // SWT event loop
	    while (!gameWindow.isDisposed()) {
		if (!display.readAndDispatch()) {
		    display.sleep();
		}
	    }

	    // Cleanup
	    cleanup();
	    display.dispose();
	    synchronized (GameApp.this) {
		running = false;
	    }
	}, "swt-ui");
	uiThread.setDaemon(false);
	uiThread.start();
    }

    synchronized void stop() {
	if (!running) {
	    return;
	}
	if (display != null && !display.isDisposed()) {
	    display.asyncExec(() -> {
		if (gameWindow != null && !gameWindow.isDisposed()) {
		    gameWindow.getShell().dispose();
		}
	    });
	}
	try {
	    if (uiThread != null && Thread.currentThread() != uiThread) {
		uiThread.join(5000);
	    }
	} catch (final InterruptedException ignored) {
	    // Ignore
	}
	running = false;
    }

    private void update() {
	final var dt = Time.getDeltaTime();

	// Update input state tracking
	if (input != null) {
	    input.update();
	}

	handleInput(dt);

	if (gameState == GameState.PLAYING) {
	    updatePlaying(dt);
	} else if (gameState == GameState.MENU) {
	    // Update stars in menu for animated background
	    final var canvas = gameWindow.getCanvas();
	    if (!canvas.isDisposed()) {
		final var client = canvas.getClientArea();

		// Spawn stars
		starSpawnTimer += dt;
		if (starSpawnTimer >= STAR_SPAWN_INTERVAL && stars.size() < MAX_STARS) {
		    starSpawnTimer = 0;
		    final var x = random.nextFloat() * client.width;
		    stars.add(new Star(x, -5));
		}

		// Update stars
		final var starIt = stars.iterator();
		while (starIt.hasNext()) {
		    final var star = starIt.next();
		    star.updatePosition(dt);
		    if (star.isOffScreen(client.height)) {
			starIt.remove();
		    }
		}
	    }
	}
    }

    private void updatePlayerMovement(final double dt) {
	final var speed = 400f; // pixels per second
	var dx = 0f;
	var dy = 0f;

	if (input.isKeyPressed(SWT.ARROW_LEFT)) {
	    dx -= 1f;
	}
	if (input.isKeyPressed(SWT.ARROW_RIGHT)) {
	    dx += 1f;
	}
	if (input.isKeyPressed(SWT.ARROW_UP)) {
	    dy -= 1f;
	}
	if (input.isKeyPressed(SWT.ARROW_DOWN)) {
	    dy += 1f;
	}

	// Normalize diagonal movement
	if (dx != 0 && dy != 0) {
	    final var inv = (float) (1 / Math.sqrt(2));
	    dx *= inv;
	    dy *= inv;
	}

	final var moveX = (float) (dx * speed * dt);
	final var moveY = (float) (dy * speed * dt);
	player.move(moveX, moveY);

	// Clamp to canvas bounds
	final var client = gameWindow.getCanvas().getClientArea();
	final var maxX = Math.max(0, client.width - player.getWidth());
	final var maxY = Math.max(0, client.height - player.getHeight());
	var clampedX = player.getX();
	var clampedY = player.getY();
	if (clampedX < 0) {
	    clampedX = 0;
	} else if (clampedX > maxX) {
	    clampedX = maxX;
	}
	if (clampedY < 0) {
	    clampedY = 0;
	} else if (clampedY > maxY) {
	    clampedY = maxY;
	}
	if (clampedX != player.getX() || clampedY != player.getY()) {
	    player.setPosition(clampedX, clampedY);
	}
    }

    private void updatePlaying(final double dt) {
	final var canvas = gameWindow.getCanvas();
	if (canvas.isDisposed()) {
	    return;
	}
	final var client = canvas.getClientArea();

	// Update player
	player.updateInvulnerability(dt);
	player.updateShootCooldown(dt);
	player.updateWeaponUpgrade(dt);
	player.updatePowerUps(dt);

	// Update screen shake
	if (screenShakeTimer > 0) {
	    screenShakeTimer -= dt;
	    screenShakeIntensity *= 0.9f;
	}

	// Increase difficulty over time
	difficultyTimer += dt;
	if (difficultyTimer >= 5.0f) {
	    difficultyTimer = 0;
	    spawnInterval = Math.max(minSpawnInterval, spawnInterval - 0.05f);
	}

	// Spawn asteroids
	spawnTimer += dt;
	if (spawnTimer >= spawnInterval) {
	    spawnTimer = 0;
	    spawnAsteroid(client.width);
	}

	// Spawn weapon upgrades
	weaponSpawnTimer -= dt;
	if (weaponSpawnTimer <= 0) {
	    weaponSpawnTimer = WEAPON_SPAWN_INTERVAL;
	    spawnWeaponUpgrade(client.width);
	}

	// Spawn power-ups
	powerUpSpawnTimer -= dt;
	if (powerUpSpawnTimer <= 0) {
	    powerUpSpawnTimer = POWERUP_SPAWN_INTERVAL;
	    spawnPowerUp(client.width);
	}

	// Spawn stars
	starSpawnTimer += dt;
	if (starSpawnTimer >= STAR_SPAWN_INTERVAL && stars.size() < MAX_STARS) {
	    starSpawnTimer = 0;
	    final var x = random.nextFloat() * client.width;
	    stars.add(new Star(x, -5));
	}

	// Calculate slow-mo effect
	final var slowMoMultiplier = player.hasSlowMotion() ? 0.5f : 1.0f;

	// Update stars
	final var starIt = stars.iterator();
	while (starIt.hasNext()) {
	    final var star = starIt.next();
	    star.updatePosition(dt);
	    if (star.isOffScreen(client.height)) {
		starIt.remove();
	    }
	}

	// Update weapon upgrades
	final var upgradeIt = weaponUpgrades.iterator();
	while (upgradeIt.hasNext()) {
	    final var upgrade = upgradeIt.next();
	    upgrade.updatePosition(dt);

	    if (upgrade.collidesWith(player)) {
		player.upgradeWeapon(upgrade.getWeaponType());
		createExplosion(upgrade.getX() + upgrade.getWidth() / 2, upgrade.getY() + upgrade.getHeight() / 2, 10);
		upgradeIt.remove();
		continue;
	    }

	    if (upgrade.isOffScreen(client.height)) {
		upgradeIt.remove();
	    }
	}

	// Update power-ups
	final var powerUpIt = powerUps.iterator();
	while (powerUpIt.hasNext()) {
	    final var powerUp = powerUpIt.next();
	    powerUp.updatePosition(dt);

	    if (powerUp.collidesWith(player)) {
		player.activatePowerUp(powerUp.getPowerUpType());
		createExplosion(powerUp.getX() + powerUp.getWidth() / 2, powerUp.getY() + powerUp.getHeight() / 2, 15);
		powerUpIt.remove();
		continue;
	    }

	    if (powerUp.isOffScreen(client.height)) {
		powerUpIt.remove();
	    }
	}

	// Collect new asteroids to add (from splitters)
	final var newAsteroids = new ArrayList<Asteroid>();

	// Update bullets
	final var bulletIt = bullets.iterator();
	while (bulletIt.hasNext()) {
	    final var bullet = bulletIt.next();
	    bullet.updatePosition(dt);

	    if (bullet.isOffScreen()) {
		bulletIt.remove();
		continue;
	    }

	    // Check collision with asteroids
	    var hitAsteroid = false;
	    final var asteroidIt2 = asteroids.iterator();
	    while (asteroidIt2.hasNext()) {
		final var asteroid = asteroidIt2.next();
		if (bullet.collidesWith(asteroid)) {
		    // Damage the asteroid
		    final var destroyed = asteroid.takeDamage(1);

		    if (destroyed) {
			createExplosion(asteroid.getX() + asteroid.getWidth() / 2,
				asteroid.getY() + asteroid.getHeight() / 2, 15);

			// Handle splitter asteroids - add children to list instead of directly
			if (asteroid.getType() == AsteroidType.SPLITTER && asteroid.getSize() > 20) {
			    final var count = 2 + random.nextInt(2);
			    for (var i = 0; i < count; i++) {
				final var angle = (float) (Math.PI * 2 * i / count);
				final var offsetX = (float) Math.cos(angle) * 20;
				final var offsetY = (float) Math.sin(angle) * 20;
				final var child = new Asteroid(asteroid.getX() + offsetX, asteroid.getY() + offsetY,
					AsteroidType.NORMAL);
				newAsteroids.add(child);
			    }
			}

			asteroidIt2.remove();
			asteroidsDestroyed++;
			asteroidsDestroyedThisLevel++;
			player.incrementCombo();

			// Score based on asteroid type
			final var baseScore = asteroid.getType().getScoreValue();
			player.addScore(baseScore * currentLevel);

			// Check if player advanced to next level
			if (asteroidsDestroyedThisLevel >= ASTEROIDS_PER_LEVEL) {
			    advanceLevel();
			}
		    } else {
			// Hit but not destroyed - smaller explosion
			createExplosion(bullet.getX(), bullet.getY(), 5);
		    }

		    hitAsteroid = true;
		    break;
		}
	    }

	    if (hitAsteroid) {
		bulletIt.remove();
	    }
	}

	// Add new splitter children now that iteration is complete
	asteroids.addAll(newAsteroids);

	// Update asteroids
	final var asteroidIt = asteroids.iterator();
	while (asteroidIt.hasNext()) {
	    final var asteroid = asteroidIt.next();
	    asteroid.updatePosition(dt, asteroidSpeedMultiplier * slowMoMultiplier);

	    if (asteroid.collidesWith(player)) {
		player.takeDamage(1);
		createExplosion(asteroid.getX() + asteroid.getWidth() / 2, asteroid.getY() + asteroid.getHeight() / 2,
			20);
		addScreenShake(15f);
		asteroidIt.remove();

		if (!player.isAlive()) {
		    gameState = GameState.GAME_OVER;
		}
		continue;
	    }

	    if (asteroid.isOffScreen(client.height)) {
		asteroidIt.remove();
		asteroidsDodged++;
		player.addScore(2 * currentLevel);
	    }
	}

	// Update particles
	final var particleIt = particles.iterator();
	while (particleIt.hasNext()) {
	    final var particle = particleIt.next();
	    particle.updatePosition(dt);
	    if (particle.isExpired()) {
		particleIt.remove();
	    }
	}

	// Add passive score
	player.addScore((int) (dt * currentLevel));
    }
}
