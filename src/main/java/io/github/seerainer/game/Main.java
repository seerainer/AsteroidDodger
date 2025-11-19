package io.github.seerainer.game;

public class Main {
    private Main() {
	throw new IllegalStateException("Main class");
    }

    public static void main() {
	new GameApp().start();
    }
}