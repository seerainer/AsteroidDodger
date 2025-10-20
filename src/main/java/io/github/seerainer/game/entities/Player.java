package io.github.seerainer.game.entities;

public class Player extends Entity {
    private static final float INVULNERABILITY_TIME = 2.0f;
    private static final float COMBO_WINDOW = 1.5f;
    private int health;
    private int score;
    private boolean invulnerable;
    private float invulnerabilityTimer;
    private float shootCooldownTimer;
    private WeaponType currentWeapon;
    private float weaponUpgradeTimer;
    // Power-up system
    private boolean hasShield;
    private float shieldTimer;
    private boolean hasScoreMultiplier;
    private float scoreMultiplierTimer;
    private boolean hasSlowMotion;
    private float slowMotionTimer;
    // Combo system
    private int comboCount;
    private float comboTimer;

    public Player(final float startX, final float startY) {
	super(startX, startY, 32f, 32f);
	this.health = 3;
	this.score = 0;
	this.invulnerable = false;
	this.invulnerabilityTimer = 0;
	this.shootCooldownTimer = 0;
	this.currentWeapon = WeaponType.SINGLE;
	this.weaponUpgradeTimer = 0;
	this.hasShield = false;
	this.shieldTimer = 0;
	this.hasScoreMultiplier = false;
	this.scoreMultiplierTimer = 0;
	this.hasSlowMotion = false;
	this.slowMotionTimer = 0;
	this.comboCount = 0;
	this.comboTimer = 0;
    }

    public void activatePowerUp(final PowerUpType powerUpType) {
	switch (powerUpType) {
	case SHIELD -> {
	    hasShield = true;
	    shieldTimer = powerUpType.getDuration();
	}
	case HEALTH -> {
	    health = Math.min(health + 1, 5); // Max health is 5
	}
	case SCORE_MULTIPLIER -> {
	    hasScoreMultiplier = true;
	    scoreMultiplierTimer = powerUpType.getDuration();
	}
	case SLOW_MOTION -> {
	    hasSlowMotion = true;
	    slowMotionTimer = powerUpType.getDuration();
	}
	default -> throw new IllegalArgumentException("Unexpected value: " + powerUpType);
	}
    }

    public void addHealth(final int amount) {
	this.health = Math.min(this.health + amount, 5); // Max health is 5
    }

    public void addScore(final int points) {
	var actualPoints = points;
	if (hasScoreMultiplier) {
	    actualPoints *= 2;
	}
	if (comboCount > 1) {
	    actualPoints = (int) (actualPoints * (1 + (comboCount - 1) * 0.25f));
	}
	this.score += actualPoints;
    }

    public boolean canShoot() {
	return shootCooldownTimer <= 0;
    }

    public int getComboCount() {
	return comboCount;
    }

    public WeaponType getCurrentWeapon() {
	return currentWeapon;
    }

    public int getHealth() {
	return health;
    }

    public int getScore() {
	return score;
    }

    public float getScoreMultiplierTimer() {
	return scoreMultiplierTimer;
    }

    public float getShieldTimer() {
	return shieldTimer;
    }

    public float getSlowMotionTimer() {
	return slowMotionTimer;
    }

    public float getWeaponUpgradeTimeRemaining() {
	return weaponUpgradeTimer;
    }

    public boolean hasScoreMultiplier() {
	return hasScoreMultiplier;
    }

    public boolean hasShield() {
	return hasShield;
    }

    public boolean hasSlowMotion() {
	return hasSlowMotion;
    }

    public void incrementCombo() {
	comboCount++;
	comboTimer = COMBO_WINDOW;
    }

    public boolean isAlive() {
	return health > 0;
    }

    public boolean isInvulnerable() {
	return invulnerable;
    }

    public void move(final float deltaX, final float deltaY) {
	this.x += deltaX;
	this.y += deltaY;
    }

    @Override
    public void render() {
	// No-op for now
    }

    public void reset(final float x1, final float y1) {
	this.x = x1;
	this.y = y1;
	this.health = 3;
	this.score = 0;
	this.invulnerable = false;
	this.invulnerabilityTimer = 0;
	this.shootCooldownTimer = 0;
	this.currentWeapon = WeaponType.SINGLE;
	this.weaponUpgradeTimer = 0;
	this.hasShield = false;
	this.shieldTimer = 0;
	this.hasScoreMultiplier = false;
	this.scoreMultiplierTimer = 0;
	this.hasSlowMotion = false;
	this.slowMotionTimer = 0;
	this.comboCount = 0;
	this.comboTimer = 0;
    }

    public void resetCombo() {
	comboCount = 0;
	comboTimer = 0;
    }

    public void shoot() {
	shootCooldownTimer = currentWeapon.getCooldown();
    }

    public void takeDamage(final int damage) {
	if (invulnerable || hasShield) {
	    return;
	}
	this.health -= damage;
	if (this.health < 0) {
	    this.health = 0;
	}
	if (this.health <= 0) {
	    return;
	}
	invulnerable = true;
	invulnerabilityTimer = INVULNERABILITY_TIME;
	resetCombo();
    }

    @Override
    public void update() {
	// No-op for now
    }

    public void updateInvulnerability(final double deltaTime) {
	if (!invulnerable) {
	    return;
	}
	invulnerabilityTimer -= deltaTime;
	if (invulnerabilityTimer > 0) {
	    return;
	}
	invulnerable = false;
	invulnerabilityTimer = 0;
    }

    public void updatePowerUps(final double deltaTime) {
	// Update shield
	if (hasShield) {
	    shieldTimer -= deltaTime;
	    if (shieldTimer <= 0) {
		hasShield = false;
		shieldTimer = 0;
	    }
	}

	// Update score multiplier
	if (hasScoreMultiplier) {
	    scoreMultiplierTimer -= deltaTime;
	    if (scoreMultiplierTimer <= 0) {
		hasScoreMultiplier = false;
		scoreMultiplierTimer = 0;
	    }
	}

	// Update slow motion
	if (hasSlowMotion) {
	    slowMotionTimer -= deltaTime;
	    if (slowMotionTimer <= 0) {
		hasSlowMotion = false;
		slowMotionTimer = 0;
	    }
	}

	// Update combo
	if (comboCount <= 0) {
	    return;
	}
	comboTimer -= deltaTime;
	if (comboTimer <= 0) {
	    resetCombo();
	}
    }

    public void updateShootCooldown(final double deltaTime) {
	if (shootCooldownTimer <= 0) {
	    return;
	}
	shootCooldownTimer -= deltaTime;
	if (shootCooldownTimer < 0) {
	    shootCooldownTimer = 0;
	}
    }

    public void updateWeaponUpgrade(final double deltaTime) {
	if (weaponUpgradeTimer <= 0) {
	    return;
	}
	weaponUpgradeTimer -= deltaTime;
	if (weaponUpgradeTimer > 0) {
	    return;
	}
	weaponUpgradeTimer = 0;
	currentWeapon = WeaponType.SINGLE; // Revert to default weapon
    }

    public void upgradeWeapon(final WeaponType weaponType) {
	this.currentWeapon = weaponType;
	this.weaponUpgradeTimer = 15.0f; // Weapon upgrade lasts 15 seconds
    }
}