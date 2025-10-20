package io.github.seerainer.game;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class GameWindow {
    private final Shell shell;
    private final Canvas canvas;
    private final Display display;
    private boolean isFullScreen;
    private Cursor hiddenCursor;
    private final Cursor defaultCursor;

    public GameWindow(final Display display) {
	this.display = display;
	this.shell = new Shell(display, SWT.SHELL_TRIM);
	this.shell.setText("Asteroid Dodger");
	this.shell.setSize(800, 600);
	this.isFullScreen = true;
	this.shell.setMaximized(isFullScreen);
	this.shell.setBackground(display.getSystemColor(SWT.COLOR_BLACK));
	this.canvas = new Canvas(shell, SWT.DOUBLE_BUFFERED);
	final var area = shell.getClientArea();
	this.canvas.setBounds(area);
	this.canvas.setBackground(display.getSystemColor(SWT.COLOR_BLACK));
	this.shell.addListener(SWT.Resize, _ -> {
	    final var client = shell.getClientArea();
	    canvas.setBounds(client);
	});

	// Create hidden cursor (1x1 transparent cursor)
	createHiddenCursor();

	// Save the default cursor
	this.defaultCursor = shell.getCursor();
    }

    private void createHiddenCursor() {
	// Create a 1x1 transparent image for the cursor
	final var paletteData = new PaletteData(0xFF, 0xFF00, 0xFF0000);
	final var imageData = new ImageData(1, 1, 24, paletteData);
	imageData.setPixel(0, 0, 0);
	imageData.setAlpha(0, 0, 0);
	this.hiddenCursor = new Cursor(display, imageData, 0, 0);
    }

    public void dispose() {
	if (hiddenCursor != null && !hiddenCursor.isDisposed()) {
	    hiddenCursor.dispose();
	}
    }

    public Canvas getCanvas() {
	return canvas;
    }

    public Shell getShell() {
	return shell;
    }

    public boolean isDisposed() {
	return shell.isDisposed();
    }

    public void open() {
	shell.setFullScreen(isFullScreen);
	updateCursor();
	shell.open();
    }

    public void toggleFullScreen() {
	isFullScreen = !isFullScreen;
	shell.setFullScreen(isFullScreen);
	updateCursor();
    }

    private void updateCursor() {
	if (isFullScreen) {
	    shell.setCursor(hiddenCursor);
	    canvas.setCursor(hiddenCursor);
	} else {
	    shell.setCursor(defaultCursor);
	    canvas.setCursor(defaultCursor);
	}
    }
}