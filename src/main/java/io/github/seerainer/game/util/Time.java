package io.github.seerainer.game.util;

public class Time {
    private static long lastTime;
    private static double deltaTime;

    private Time() {
	throw new IllegalStateException("Utility class");
    }

    public static double getDeltaTime() {
	return deltaTime;
    }

    public static void init() {
	lastTime = System.nanoTime();
    }

    public static void update() {
	final var currentTime = System.nanoTime();
	deltaTime = (currentTime - lastTime) / 1_000_000_000.0; // Convert to seconds
	lastTime = currentTime;
    }
}