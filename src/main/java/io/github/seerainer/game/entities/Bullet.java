package io.github.seerainer.game.entities;

public class Bullet extends Entity {
    private static final float SPEED = 800f; // pixels per second
    private float velocityY;
    private float velocityX;

    public Bullet(final float x, final float y) {
	super(x, y, 4, 12);
	this.velocityY = -SPEED; // Move upward
	this.velocityX = 0;
    }

    public boolean collidesWith(final Entity other) {
	return x < other.getX() + other.getWidth() && x + width > other.getX() && y < other.getY() + other.getHeight()
		&& y + height > other.getY();
    }

    public boolean isOffScreen() {
	return y + height < 0;
    }

    @Override
    public void render() {
	// Handled by GameApp
    }

    public void setAngle(final float angle) {
	// Calculate velocity components based on angle
	// Angle 0 is straight up (-90 degrees in standard math)
	this.velocityX = (float) (SPEED * Math.sin(angle));
	this.velocityY = (float) (-SPEED * Math.cos(angle));
    }

    @Override
    public void update() {
	// Handled by updatePosition
    }

    public void updatePosition(final double deltaTime) {
	this.x += velocityX * deltaTime;
	this.y += velocityY * deltaTime;
    }
}