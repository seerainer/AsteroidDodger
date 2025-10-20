package io.github.seerainer.game.entities;

public class PowerUp extends Entity {
    private static final float FALL_SPEED = 120f;
    private final PowerUpType powerUpType;
    private final float velocityY;
    private float pulseTimer;

    public PowerUp(final float x, final float y, final PowerUpType powerUpType) {
	super(x, y, 20, 20);
	this.powerUpType = powerUpType;
	this.velocityY = FALL_SPEED;
	this.pulseTimer = 0;
    }

    public boolean collidesWith(final Entity other) {
	return x < other.getX() + other.getWidth() && x + width > other.getX() && y < other.getY() + other.getHeight()
		&& y + height > other.getY();
    }

    public PowerUpType getPowerUpType() {
	return powerUpType;
    }

    public float getPulseTimer() {
	return pulseTimer;
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
	this.pulseTimer += deltaTime;
    }
}
