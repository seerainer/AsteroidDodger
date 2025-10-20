package io.github.seerainer.game.entities;

public enum PowerUpType {
    SHIELD("Shield", 10.0f), // Temporary invulnerability
    HEALTH("Health", 0f), // Restore 1 health
    SCORE_MULTIPLIER("2x Score", 15.0f), // Double score for duration
    SLOW_MOTION("Slow-Mo", 8.0f); // Slow down asteroids

    private final String displayName;
    private final float duration;

    PowerUpType(final String displayName, final float duration) {
	this.displayName = displayName;
	this.duration = duration;
    }

    public String getDisplayName() {
	return displayName;
    }

    public float getDuration() {
	return duration;
    }
}
