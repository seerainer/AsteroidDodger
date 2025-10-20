package io.github.seerainer.game.entities;

import java.util.Random;

import io.github.seerainer.game.GameApp;

public class Star extends Entity {
    private static final Random random = GameApp.getRandom();
    private final float velocityY;
    private final int brightness;
    private final int layer; // 0 = far (slow), 2 = close (fast)

    public Star(final float x, final float y) {
	super(x, y, 1, 1);

	// Three layers of parallax
	this.layer = random.nextInt(3);

	// Speed based on layer (parallax effect)
	// Far stars: 20-40 px/s, mid: 50-80 px/s, close: 100-150 px/s
	this.velocityY = switch (layer) {
	case 0 -> 20f + random.nextFloat() * 20f; // Far layer
	case 1 -> 50f + random.nextFloat() * 30f; // Mid layer
	default -> 100f + random.nextFloat() * 50f; // Close layer
	};

	// Brightness based on layer
	this.brightness = switch (layer) {
	case 0 -> 80 + random.nextInt(60); // Dimmer far stars
	case 1 -> 120 + random.nextInt(80); // Medium brightness
	default -> 180 + random.nextInt(76); // Brighter close stars
	};
    }

    public int getBrightness() {
	return brightness;
    }

    public int getLayer() {
	return layer;
    }

    public boolean isOffScreen(final int canvasHeight) {
	return y > canvasHeight;
    }

    @Override
    public void render() {
	// Handled by GameApp
    }

    @Override
    public void update() {
	// Handled by updatePosition
    }

    public void updatePosition(final double deltaTime) {
	this.y += velocityY * deltaTime;
    }
}
