package io.github.seerainer.game.input;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;

public class InputHandler {

    private final Set<Integer> keys = ConcurrentHashMap.newKeySet();
    private final Set<Integer> keysJustPressed = ConcurrentHashMap.newKeySet();
    private final Set<Integer> previousKeys = ConcurrentHashMap.newKeySet();

    public InputHandler(final Display display) {
	initializeKeyListener(display);
    }

    private void initializeKeyListener(final Display display) {
	display.addFilter(SWT.KeyDown, event -> keys.add(Integer.valueOf(event.keyCode)));
	display.addFilter(SWT.KeyUp, event -> keys.remove(Integer.valueOf(event.keyCode)));
    }

    /**
     * Returns true only on the first frame a key is pressed, not while held.
     */
    public boolean isKeyJustPressed(final int keyCode) {
	return keysJustPressed.contains(Integer.valueOf(keyCode));
    }

    public boolean isKeyPressed(final int keyCode) {
	return keys.contains(Integer.valueOf(keyCode));
    }

    /**
     * Updates the key state tracking. Call this once per frame.
     */
    public void update() {
	keysJustPressed.clear();

	// Find keys that are pressed now but weren't pressed in the previous frame
	keys.stream().filter((final Integer key) -> !previousKeys.contains(key)).forEach(keysJustPressed::add);

	// Update previous keys for next frame
	previousKeys.clear();
	previousKeys.addAll(keys);
    }
}