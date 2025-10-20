package io.github.seerainer.game.entities;

public abstract class Entity {
    protected float x;
    protected float y;
    protected float width;
    protected float height;

    public Entity(final float x, final float y, final float width, final float height) {
	this.x = x;
	this.y = y;
	this.width = width;
	this.height = height;
    }

    public float getHeight() {
	return height;
    }

    public float getWidth() {
	return width;
    }

    public float getX() {
	return x;
    }

    public float getY() {
	return y;
    }

    public abstract void render();

    public void setPosition(final float x, final float y) {
	this.x = x;
	this.y = y;
    }

    public abstract void update();
}