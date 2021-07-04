package com.mygdx.game.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.mygdx.game.MemorySimulation;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 1200;
		config.height = 700;
		config.resizable = false;
		config.title = "Symulacja dzia³ania wybranych algorytmów zastêpowania stron";
		new LwjglApplication(new MemorySimulation(), config);
	}
}
