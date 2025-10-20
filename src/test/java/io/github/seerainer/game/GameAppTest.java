package io.github.seerainer.game;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class GameAppTest {
    private GameApp gameApp;

    @Test
    @Tag("integration")
    public void startCreatesWindowAndRuns() throws InterruptedException {
	gameApp = new GameApp();
	assertNotNull(gameApp);
	assertFalse(gameApp.isRunning());

	gameApp.start();
	// Wait briefly for UI thread to create the window
	final var deadline = System.currentTimeMillis() + 3000;
	while (gameApp.getGameWindow() == null && System.currentTimeMillis() < deadline) {
	    Thread.sleep(50);
	}
	assertNotNull(gameApp.getGameWindow(), "GameWindow should be created after start()");
	assertTrue(gameApp.isRunning(), "Game should report running after start()");

	// Stop and ensure it shuts down
	gameApp.stop();
	// Allow background thread to update state
	Thread.sleep(100);
	assertFalse(gameApp.isRunning(), "Game should not be running after stop()");
    }

    @Test
    @Tag("integration")
    public void startIsIdempotent() throws InterruptedException {
	gameApp = new GameApp();
	gameApp.start();
	gameApp.start(); // should be a no-op
	final var deadline = System.currentTimeMillis() + 3000;
	while (gameApp.getGameWindow() == null && System.currentTimeMillis() < deadline) {
	    Thread.sleep(50);
	}
	assertNotNull(gameApp.getGameWindow());
	gameApp.stop();
	Thread.sleep(100);
	assertFalse(gameApp.isRunning());
    }

    @AfterEach
    void tearDown() {
	if (gameApp != null && gameApp.isRunning()) {
	    gameApp.stop();
	}
    }
}