package io.github.seerainer.game.entities;

import java.util.Random;

import io.github.seerainer.game.GameApp;

public class Asteroid extends Entity {
    private static final Random random = GameApp.getRandom();
    private final float velocityY;
    private final float velocityX;
    private final int size;
    private final AsteroidType type;
    private int hitPoints;
    private final int maxHitPoints;

    public Asteroid(final float x, final float y) {
	this(x, y, AsteroidType.NORMAL);
    }

    public Asteroid(final float x, final float y, final AsteroidType type) {
	super(x, y, 0, 0);
	this.type = type;
	// Random size between 20-50 pixels (smaller for splitter children)
	this.size = type == AsteroidType.SPLITTER ? 15 + random.nextInt(26) : 20 + random.nextInt(31);
	this.width = size;
	this.height = size;

	// Random downward speed (150-400 pixels per second) modified by type
	final var baseSpeed = 150f + random.nextFloat() * 250f;
	this.velocityY = baseSpeed * type.getSpeedMultiplier();

	// Small random horizontal drift (-50 to 50 pixels per second)
	this.velocityX = -50f + random.nextFloat() * 100f;

	// Set hit points based on type
	this.hitPoints = type.getHitPoints();
	this.maxHitPoints = type.getHitPoints();
    }

    public boolean collidesWith(final Entity other) {
	return x < other.getX() + other.getWidth() && x + width > other.getX() && y < other.getY() + other.getHeight()
		&& y + height > other.getY();
    }

    public int getHitPoints() {
	return hitPoints;
    }

    public int getMaxHitPoints() {
	return maxHitPoints;
    }

    public int getSize() {
	return size;
    }

    public AsteroidType getType() {
	return type;
    }

    public boolean isOffScreen(final int canvasHeight) {
	return y > canvasHeight;
    }

    @Override
    public void render() {
	// Handled by GameApp
    }

    public boolean takeDamage(final int damage) {
	hitPoints -= damage;
	return hitPoints <= 0;
    }

    @Override
    public void update() {
	// Handled by updatePosition
    }

    public void updatePosition(final double deltaTime, final float speedMultiplier) {
	this.x += velocityX * deltaTime * speedMultiplier;
	this.y += velocityY * deltaTime * speedMultiplier;
    }
}