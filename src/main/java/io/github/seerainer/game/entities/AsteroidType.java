package io.github.seerainer.game.entities;

public enum AsteroidType {
    NORMAL(1.0f, 5, 1), // Standard asteroid
    FAST(1.3f, 7, 1), // Moves faster, worth more points
    TANK(0.6f, 10, 3), // Slower but takes 3 hits to destroy
    SPLITTER(0.8f, 6, 1); // Splits into smaller asteroids when destroyed

    private final float speedMultiplier;
    private final int scoreValue;
    private final int hitPoints;

    AsteroidType(final float speedMultiplier, final int scoreValue, final int hitPoints) {
	this.speedMultiplier = speedMultiplier;
	this.scoreValue = scoreValue;
	this.hitPoints = hitPoints;
    }

    public int getHitPoints() {
	return hitPoints;
    }

    public int getScoreValue() {
	return scoreValue;
    }

    public float getSpeedMultiplier() {
	return speedMultiplier;
    }
}
