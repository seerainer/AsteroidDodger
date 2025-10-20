package io.github.seerainer.game.entities;

public enum WeaponType {
    SINGLE("Single Shot", 1, 0.1f), DOUBLE("Double Shot", 2, 0.12f), TRIPLE("Triple Shot", 3, 0.15f),
    RAPID("Rapid Fire", 1, 0.05f), SPREAD("Spread Shot", 5, 0.2f);

    private final String displayName;
    private final int bulletCount;
    private final float cooldown;

    WeaponType(final String displayName, final int bulletCount, final float cooldown) {
	this.displayName = displayName;
	this.bulletCount = bulletCount;
	this.cooldown = cooldown;
    }

    public int getBulletCount() {
	return bulletCount;
    }

    public float getCooldown() {
	return cooldown;
    }

    public String getDisplayName() {
	return displayName;
    }
}
