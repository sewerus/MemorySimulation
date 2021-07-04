package com.mygdx.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class Reference {
	private float timeStart;
	private float length;
	private float progress;
	private int id, pageId;

	public Reference(int id, int pageId) {
		this.timeStart = (float) 1.5*id + 1;
		this.length = 1.5F;
		this.progress = 0F;
		this.id = id;
		this.pageId = pageId;
	}

	protected int id() {
		return id;
	}

	protected int pageId() {
		return pageId;
	}

	protected float length() {
		return length;
	}

	protected float timeStart() {
		return timeStart;
	}
	
	protected float progress() {
		return progress;
	}

	public float currentPosition(float time) {
		if (timeStart - time > 0) {
			return timeStart - time;
		} else {
			return 0;
		}
	}

	public void increaseProgress(float timeStep) {
		if (!isDone(timeStep)) {
			progress += timeStep;
		}
	}

	public boolean isDone(float timeStep) {
		if (progress + timeStep >= length - timeStep) {
			progress = length;
			return true;
		} else {
			return false;
		}
	}

	private float howMuchToEnd() {
		return length - progress;
	}

	public void draw(float time, int all, int x, int y, Color color, ShapeRenderer shapeRenderer,
			SpriteBatch batch, BitmapFont font) {
		// body
		shapeRenderer.rect(x + currentPosition(time) * 50, y + 20 * (all - pageId - 1) + 15, howMuchToEnd() * 50, 15, color,
				color, color, color);
	}
}
