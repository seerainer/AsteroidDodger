package io.github.seerainer.game.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class PlayerTest {

    @SuppressWarnings("static-method")
    @Test
    @Tag("unit")
    void movesAndTracksHealthAndScore() {
	final var p = new Player(10f, 20f);
	assertEquals(10f, p.getX());
	assertEquals(20f, p.getY());
	assertEquals(3, p.getHealth()); // 3 lives
	assertEquals(0, p.getScore());
	assertTrue(p.isAlive());

	p.move(5f, -2.5f);
	assertEquals(15f, p.getX());
	assertEquals(17.5f, p.getY());

	p.takeDamage(1);
	assertEquals(2, p.getHealth());
	assertTrue(p.isAlive());
	assertTrue(p.isInvulnerable()); // Should be invulnerable after damage

	p.takeDamage(10); // Should not take damage while invulnerable
	assertEquals(2, p.getHealth());

	// Update to remove invulnerability
	p.updateInvulnerability(3.0); // More than INVULNERABILITY_TIME
	assertFalse(p.isInvulnerable());

	p.takeDamage(2);
	assertEquals(0, p.getHealth());
	assertFalse(p.isAlive());

	p.addScore(42);
	assertEquals(42, p.getScore());
    }

    @SuppressWarnings("static-method")
    @Test
    @Tag("unit")
    void resetWorks() {
	final var p = new Player(10f, 20f);
	p.addScore(100);
	p.takeDamage(2);
	p.shoot();

	p.reset(50f, 60f);
	assertEquals(50f, p.getX());
	assertEquals(60f, p.getY());
	assertEquals(3, p.getHealth());
	assertEquals(0, p.getScore());
	assertFalse(p.isInvulnerable());
	assertTrue(p.canShoot());
    }

    @SuppressWarnings("static-method")
    @Test
    @Tag("unit")
    void shootingCooldownWorks() {
	final var p = new Player(10f, 20f);
	assertTrue(p.canShoot());

	p.shoot();
	assertFalse(p.canShoot()); // Should be on cooldown

	p.updateShootCooldown(0.2); // Update past cooldown time
	assertTrue(p.canShoot());
    }
}