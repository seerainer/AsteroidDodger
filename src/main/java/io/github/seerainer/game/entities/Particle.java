package io.github.seerainer.game.entities;

public class Particle extends Entity {
    private final float velocityX;
    private final float velocityY;
    private final float lifetime;
    private float age;

    public Particle(final float x, final float y, final float vx, final float vy, final float lifetime) {
	super(x, y, 3, 3);
	this.velocityX = vx;
	this.velocityY = vy;
	this.lifetime = lifetime;
	this.age = 0;
    }

    public float getAlpha() {
	return Math.max(0, 1.0f - (age / lifetime));
    }

    public boolean isExpired() {
	return age >= lifetime;
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
	this.x += velocityX * deltaTime;
	this.y += velocityY * deltaTime;
	this.age += deltaTime;
    }
}
