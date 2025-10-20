package io.github.seerainer.game.entities;

public class WeaponUpgrade extends Entity {
    private static final float FALL_SPEED = 100f;
    private final WeaponType weaponType;
    private final float velocityY;

    public WeaponUpgrade(final float x, final float y, final WeaponType weaponType) {
	super(x, y, 24, 24);
	this.weaponType = weaponType;
	this.velocityY = FALL_SPEED;
    }

    public boolean collidesWith(final Entity other) {
	return x < other.getX() + other.getWidth() && x + width > other.getX() && y < other.getY() + other.getHeight()
		&& y + height > other.getY();
    }

    public WeaponType getWeaponType() {
	return weaponType;
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
