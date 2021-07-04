package com.mygdx.game;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.utils.Timer;

public class Processor {
	private ArrayList<Integer> pagesTable = new ArrayList<Integer>();
	private ArrayList<Boolean> referenceBits = new ArrayList<Boolean>(); // to
																			// SecondChance
																			// algorhythm
	private ArrayList<Reference> references = new ArrayList<Reference>();
	private ArrayList<Color> colors = new ArrayList<Color>();
	private String dataReferences; // to statistics
	private ArrayList<String> dataFrames = new ArrayList<String>(); // to
																	// statistics
	private int pageFaultCounter;
	private int pagesTableSize;
	private float timer, timeStep;
	private int border;
	private int x, y, width, height;
	private int method;
	private boolean isOn;
	private int whatToDraw; // 0 - prepare references, 1 - which method, 2 - run
	private int nowRunning = -1;

	public Processor() {
		timer = 0F;
		timeStep = 0.01F;
		border = 5;
		this.isOn = false;
		this.x = 200;
		this.width = Gdx.graphics.getWidth() - x;
		this.whatToDraw = 0;
		this.pagesTableSize = askIntegerNumber("Podaj liczbê ramek w tablicy stron: ");
		this.pageFaultCounter = 0;
		this.dataReferences = "";
		for (int i = 0; i < pagesTableSize; i++) {
			dataFrames.add("");
		}
	}

	private int askTwoOptions(String text) {
		TextListener textListener = new TextListener();
		Timer wait = new Timer();
		String input;
		int answer = 0;

		Gdx.input.getTextInput(textListener, text, "", "");
		while (textListener.text == null) {
			wait.delay(5);
		}
		input = textListener.text;
		textListener.text = null;

		while (answer == 0) {
			if ((input.equals("1") || input.equals("2")) && !input.isEmpty()) {
				answer = Integer.parseInt(input);
				break;
			} else {
				Gdx.input.getTextInput(textListener, "Z³y wybór", "", "");
				while (textListener.text == null) {
					wait.delay(5);
				}
				input = textListener.text;
				textListener.text = null;
			}
		}
		return answer;
	}

	private int askIntegerNumber(String text) {
		TextListener textListener = new TextListener();
		Timer wait = new Timer();
		String input;
		int answer = -1;
		Gdx.input.getTextInput(textListener, text, "", "");
		while (textListener.text == null) {
			wait.delay(5);
		}
		input = textListener.text;
		textListener.text = null;

		while (answer == -1) {
			while (true) {
				try {
					answer = Integer.parseInt(input);
					if (answer < 0) {
						Gdx.input.getTextInput(textListener, "Z³y wybór", "", "");
						while (textListener.text == null) {
							wait.delay(5);
						}
						input = textListener.text;
						textListener.text = null;
					}
					break;
				} catch (NumberFormatException e) {
					Gdx.input.getTextInput(textListener, "Z³y wybór", "", "");
					while (textListener.text == null) {
						wait.delay(5);
					}
					input = textListener.text;
					textListener.text = null;
				}
			}
		}
		return answer;
	}

	public void timeStep() {
		if (isOn) {
			timer += timeStep;
		}
	}

	private void randTasks() {
		Random generator = new Random();
		int referencesNumber = askIntegerNumber("Jak d³ugi ci¹g odniesieñ wylosowaæ?");
		int memorySize = askIntegerNumber("Liczba stron w pamiêci fizycznej?");

		for (int i = 0; i < referencesNumber; i++) {
			references.add(new Reference(i, generator.nextInt(memorySize)));
		}
	}

	private void readTasks() {
		try {
			FileReader fileReader = new FileReader("data.txt");
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String line = bufferedReader.readLine();
			int id = 0;
			while (line != null) {
				int startLogin = 0;
				int endLogin = 0;

				while (endLogin < line.length()) {

					while (Character.isDigit(line.charAt(endLogin))) {
						endLogin++;
						if (endLogin == line.length()) {
							break;
						}
					}
					int pageId = Integer.parseInt(line.substring(startLogin, endLogin));
					endLogin++;
					startLogin = endLogin;
					references.add(new Reference(id, pageId));
					id++;
				}

				line = bufferedReader.readLine();
			}
			bufferedReader.close();
		} catch (IOException e) {
			int answer = askTwoOptions("Niepowodzenie! 1 -> losuj, 2 -> zakoñcz");
			switch (answer) {
			case 1:
				randTasks();
				break;
			case 2:
				Gdx.app.exit();
			}
			e.printStackTrace();
		}
	}

	private int phisicalMemorySize() {
		int maxId = 0;
		for (int i = 0; i < references.size(); i++) {
			if (maxId < references.get(i).pageId()) {
				maxId = references.get(i).pageId();
			}
		}
		return maxId + 1; // because of 0
	}

	private void prepareColors() {
		for (int i = 0; i < phisicalMemorySize() + 1; i++) {
			colors.add(new Color((int) (Math.random() * 0x1000000)));
		}
	}

	public void prepareTasks(ShapeRenderer shapeRenderer, SpriteBatch batch, BitmapFont font) {
		shapeRenderer.begin(ShapeType.Filled);
		// readTasks
		shapeRenderer.rect(Gdx.graphics.getWidth() / 2 - 220, Gdx.graphics.getHeight() / 2 + 25, 210, 30, Color.BLUE,
				Color.BLUE, Color.BLUE, Color.BLUE);
		// randTasks
		shapeRenderer.rect(Gdx.graphics.getWidth() / 2 + 10, Gdx.graphics.getHeight() / 2 + 25, 130, 30, Color.BLUE,
				Color.BLUE, Color.BLUE, Color.BLUE);
		shapeRenderer.end();

		font.setColor(Color.WHITE);
		batch.begin();
		// readTasks
		font.draw(batch, "WCZYTAJ DANE Z PLIKU [F]", Gdx.graphics.getWidth() / 2 - 210,
				Gdx.graphics.getHeight() / 2 + 45);
		// randTasks
		font.draw(batch, "LOSUJ DANE [R]", Gdx.graphics.getWidth() / 2 + 20, Gdx.graphics.getHeight() / 2 + 45);
		batch.end();

		if (Gdx.input.isTouched()) {
			if (Gdx.input.getY() > Gdx.graphics.getHeight() / 2 - 55
					&& Gdx.input.getY() < Gdx.graphics.getHeight() / 2 - 25) {
				// readTasks
				if (Gdx.input.getX() > Gdx.graphics.getWidth() / 2 - 220
						&& Gdx.input.getX() < Gdx.graphics.getWidth() / 2 - 10) {
					readTasks();
					prepareColors();
					whatToDraw = 1;
					y = Gdx.graphics.getHeight() - phisicalMemorySize() * 20 - 50;
					height = phisicalMemorySize() * 20 + 25;
				}
				// randTasks
				if (Gdx.input.getX() > Gdx.graphics.getWidth() / 2 + 10
						&& Gdx.input.getX() < Gdx.graphics.getWidth() / 2 + 140) {
					randTasks();
					prepareColors();
					whatToDraw = 1;
					y = Gdx.graphics.getHeight() - phisicalMemorySize() * 20 - 50;
					height = phisicalMemorySize() * 20 + 25;
				}
			}
		}
		if (Gdx.input.isKeyJustPressed(Keys.F)) {
			readTasks();
			prepareColors();
			whatToDraw = 1;
			y = Gdx.graphics.getHeight() - phisicalMemorySize() * 20 - 50;
			height = phisicalMemorySize() * 20 + 25;
		}
		if (Gdx.input.isKeyJustPressed(Keys.R)) {
			randTasks();
			prepareColors();
			whatToDraw = 1;
			y = Gdx.graphics.getHeight() - phisicalMemorySize() * 20 - 50;
			height = phisicalMemorySize() * 20 + 25;
		}
	}

	public void addReference() {
		isOn = false;
		int answer = 0;

		answer = askTwoOptions("1 -> wpisz rêcznie, 2 -> losuj");

		switch (answer) {
		case 1:
			int pageId = askIntegerNumber("Podaj numer strony:");
			while (colors.size() <= pageId) {
				colors.add(new Color((int) (Math.random() * 0x1000000)));
			}
			references.add(new Reference(references.size(), pageId));
			break;
		case 2:
			Random generator = new Random();
			references.add(new Reference(references.size(), generator.nextInt(phisicalMemorySize())));
			break;
		}
		isOn = true;
	}

	private void updateDataFrames() {
		for (int i = 0; i < pagesTableSize; i++) {
			if (i < pagesTable.size()) {
				if (method == 4) {
					if (referenceBits.get(i)) {
						dataFrames.set(i, dataFrames.get(i) + Integer.toString(pagesTable.get(i)) + "[1]   ");
					} else {
						dataFrames.set(i, dataFrames.get(i) + Integer.toString(pagesTable.get(i)) + "[0]   ");
					}
				} else {
					dataFrames.set(i, dataFrames.get(i) + Integer.toString(pagesTable.get(i)) + "      ");

				}
			} else {
				if (method == 4) {
					dataFrames.set(i, dataFrames.get(i) + "         ");
				} else {
					dataFrames.set(i, dataFrames.get(i) + "        ");
				}
			}
		}
	}

	private void whichMethod(ShapeRenderer shapeRenderer, SpriteBatch batch, BitmapFont font) {
		shapeRenderer.begin(ShapeType.Filled);
		// FIFO
		shapeRenderer.rect(Gdx.graphics.getWidth() / 2 - 250, Gdx.graphics.getHeight() / 2 - 15, 70, 30, Color.BROWN,
				Color.BROWN, Color.BROWN, Color.BROWN);
		// OPT
		shapeRenderer.rect(Gdx.graphics.getWidth() / 2 - 170, Gdx.graphics.getHeight() / 2 - 15, 70, 30, Color.BROWN,
				Color.BROWN, Color.BROWN, Color.BROWN);
		// LRU
		shapeRenderer.rect(Gdx.graphics.getWidth() / 2 + -90, Gdx.graphics.getHeight() / 2 - 15, 70, 30, Color.BROWN,
				Color.BROWN, Color.BROWN, Color.BROWN);
		// SecondChance
		shapeRenderer.rect(Gdx.graphics.getWidth() / 2 + -10, Gdx.graphics.getHeight() / 2 - 15, 140, 30, Color.BROWN,
				Color.BROWN, Color.BROWN, Color.BROWN);
		shapeRenderer.end();

		font.setColor(Color.WHITE);
		batch.begin();
		// FIFO
		font.draw(batch, "FIFO [F]", Gdx.graphics.getWidth() / 2 - 240, Gdx.graphics.getHeight() / 2 + 5);
		// OPT
		font.draw(batch, "OPT [O]", Gdx.graphics.getWidth() / 2 - 160, Gdx.graphics.getHeight() / 2 + 5);
		// LRU
		font.draw(batch, "LRU [L]", Gdx.graphics.getWidth() / 2 - 80, Gdx.graphics.getHeight() / 2 + 5);
		// SecondChance
		font.draw(batch, "Druga Szansa [D]", Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2 + 5);
		batch.end();

		if (Gdx.input.isTouched()) {
			if (Gdx.input.getY() < Gdx.graphics.getHeight() / 2 + 15
					&& Gdx.input.getY() > Gdx.graphics.getHeight() / 2 - 15) {
				// FIFO
				if (Gdx.input.getX() > Gdx.graphics.getWidth() / 2 - 250
						&& Gdx.input.getX() < Gdx.graphics.getWidth() / 2 - 200) {
					method = 1;
					whatToDraw = 2;
				}
				// OPT
				if (Gdx.input.getX() > Gdx.graphics.getWidth() / 2 - 170
						&& Gdx.input.getX() < Gdx.graphics.getWidth() / 2 - 110) {
					method = 2;
					whatToDraw = 2;
				}
				// LRU
				if (Gdx.input.getX() > Gdx.graphics.getWidth() / 2 - 90
						&& Gdx.input.getX() < Gdx.graphics.getWidth() / 2 - 20) {
					method = 3;
					whatToDraw = 2;
				}
				// SecondChance
				if (Gdx.input.getX() > Gdx.graphics.getWidth() / 2 - 10
						&& Gdx.input.getX() < Gdx.graphics.getWidth() / 2 + 130) {
					method = 4;
					whatToDraw = 2;
					for (int i = 0; i < pagesTableSize; i++) {
						referenceBits.add(true);
					}
				}
			}
		}
		if (Gdx.input.isKeyJustPressed(Keys.F)) {
			method = 1;
			whatToDraw = 2;
		}
		if (Gdx.input.isKeyJustPressed(Keys.O)) {
			method = 2;
			whatToDraw = 2;
		}
		if (Gdx.input.isKeyJustPressed(Keys.L)) {
			method = 3;
			whatToDraw = 2;
		}
		if (Gdx.input.isKeyJustPressed(Keys.D)) {
			method = 4;
			whatToDraw = 2;
			for (int i = 0; i < pagesTableSize; i++) {
				referenceBits.add(true);
			}
		}
	}

	// ----------------------------------
	// ----------------------- ALGORHYTMS
	private void FIFO() {
		for (int i = 0; i < references.size(); i++) {
			if (references.get(i).currentPosition(timer) == 0 && references.get(i).progress() == 0F
					&& !references.get(i).isDone(timeStep)) {
				dataReferences += Integer.toString(references.get(i).pageId()) + "      ";
				if (!pagesTable.contains(references.get(i).pageId())) {
					if (pagesTable.size() == pagesTableSize) {
						pagesTable.remove(0);
					}
					pageFaultCounter++;
					pagesTable.add(references.get(i).pageId());
				}
				updateDataFrames();
			}
			if (references.get(i).currentPosition(timer) == 0) {
				nowRunning = i;
			}
		}
	}

	private void OPT() {
		for (int i = 0; i < references.size(); i++) {
			if (references.get(i).currentPosition(timer) == 0 && references.get(i).progress() == 0F
					&& !references.get(i).isDone(timeStep)) {
				dataReferences += Integer.toString(references.get(i).pageId()) + "      ";
				if (!pagesTable.contains(references.get(i).pageId())) {
					if (pagesTable.size() == pagesTableSize) {
						// find victim - index in pagesTable for new Reference
						int victim = 0;
						int whenLast = 0;
						for (int j = 0; j < pagesTable.size(); j++) {
							int whenThis = i + 1;
							while (whenThis < references.size()) {
								if (references.get(whenThis).pageId() != pagesTable.get(j)) {
									whenThis++;
								} else {
									break;
								}
							}
							if (whenThis > whenLast) {
								whenLast = whenThis;
								victim = j;
							}
							if (whenThis == references.size()) { // won't be
																	// references
																	// at all
								break;
							}
						}
						pagesTable.remove(victim);
						pagesTable.add(victim, references.get(i).pageId());
					} else {
						pagesTable.add(references.get(i).pageId());
					}
					pageFaultCounter++;
				}
				updateDataFrames();
			}
			if (references.get(i).currentPosition(timer) == 0) {
				nowRunning = i;
			}
		}
	}

	private void LRU() {
		for (int i = 0; i < references.size(); i++) {
			if (references.get(i).currentPosition(timer) == 0 && references.get(i).progress() == 0F
					&& !references.get(i).isDone(timeStep)) {
				dataReferences += Integer.toString(references.get(i).pageId()) + "      ";
				if (!pagesTable.contains(references.get(i).pageId())) {
					if (pagesTable.size() == pagesTableSize) {
						// find victim - index in pagesTable for new Reference
						int victim = 0;
						int whenLast = i;
						for (int j = 0; j < pagesTable.size(); j++) {
							int whenThis = i - 1;
							while (whenThis >= 0) {
								if (references.get(whenThis).pageId() != pagesTable.get(j)) {
									whenThis--;
								} else {
									break;
								}
							}
							if (whenThis < whenLast) {
								whenLast = whenThis;
								victim = j;
							}
							System.out.println(references.get(i).pageId() + " " + pagesTable.get(j) + " " + whenThis);
							if (whenThis == -1) { // wasn't references at all
													// before
								break;
							}
						}
						pagesTable.remove(victim);
						pagesTable.add(victim, references.get(i).pageId());
					} else {
						pagesTable.add(references.get(i).pageId());
					}
					pageFaultCounter++;
				}
				updateDataFrames();
			}
			if (references.get(i).currentPosition(timer) == 0) {
				nowRunning = i;
			}
		}
	}

	private void SecondChance() {
		for (int i = 0; i < references.size(); i++) {
			if (references.get(i).currentPosition(timer) == 0 && references.get(i).progress() == 0F
					&& !references.get(i).isDone(timeStep)) {
				dataReferences += Integer.toString(references.get(i).pageId()) + "       ";
				if (!pagesTable.contains(references.get(i).pageId())) {
					if (pagesTable.size() == pagesTableSize) {
						// find victim - index in pagesTable for new Reference
						int victim = -1;
						while (victim == -1) {
							for (int j = 0; j < pagesTable.size(); j++) {
								if (referenceBits.get(j)) {
									referenceBits.set(j, false);
								} else {
									victim = j;
									break;
								}
							}
						}
						pagesTable.remove(victim);
						referenceBits.remove(victim);
					}
					pagesTable.add(references.get(i).pageId());
					referenceBits.add(true);
					pageFaultCounter++;
				}
				referenceBits.set(pagesTable.indexOf(references.get(i).pageId()), true);
				updateDataFrames();
			}
			if (references.get(i).currentPosition(timer) == 0) {
				nowRunning = i;
			}
		}
	}

	// ----------------------- ALGORHYTMS
	// ----------------------------------

	public void runProcess() {
		if (isOn) {
			switch (method) {
			case 1:
				FIFO();
				break;
			case 2:
				OPT();
				break;
			case 3:
				LRU();
				break;
			case 4:
				SecondChance();
				break;
			}
			if (nowRunning != -1) {
				references.get(nowRunning).increaseProgress(timeStep);
			}
		}
	}

	public void drawSimulation(ShapeRenderer shapeRenderer, SpriteBatch batch, BitmapFont font) {
		shapeRenderer.begin(ShapeType.Filled);
		// border
		shapeRenderer.rect(x - border, y - border, width + 2 * border, height + 2 * border, Color.BROWN, Color.BROWN,
				Color.BROWN, Color.BROWN);
		// gate
		shapeRenderer.rect(x - border, y, border, height, Color.RED, Color.RED, Color.RED, Color.RED);
		// processor
		shapeRenderer.rect(x, y, width, height, Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE);
		// pagesTable
		for (int i = 0; i < pagesTableSize; i++) {
			if (i + 1 > pagesTable.size()) {
				shapeRenderer.rect(40, Gdx.graphics.getHeight() - (i + 1) * 20 - 50, 50, 20, Color.WHITE, Color.WHITE,
						Color.WHITE, Color.WHITE);
			} else {
				// body
				shapeRenderer.rect(40, Gdx.graphics.getHeight() - (i + 1) * 20 - 50, 50, 20,
						colors.get(pagesTable.get(i)), colors.get(pagesTable.get(i)), colors.get(pagesTable.get(i)),
						colors.get(pagesTable.get(i)));
				shapeRenderer.setColor(colors.get(pagesTable.get(i)));
				shapeRenderer.circle(90, Gdx.graphics.getHeight() - (i + 1) * 20 - 40, 10);
				// line
				shapeRenderer.rectLine(95, Gdx.graphics.getHeight() - (i + 1) * 20 - 40, x - 25,
						y + 20 * (phisicalMemorySize() - 1 - references.get(pagesTable.get(i)).id()) + 23, 5,
						colors.get(pagesTable.get(i)), colors.get(pagesTable.get(i)));
			}
		}
		// references
		for (int i = 0; i < references.size(); i++) {
			if (!references.get(i).isDone(timeStep)) {
				references.get(i).draw(timer, phisicalMemorySize(), x, y, colors.get(references.get(i).pageId()),
						shapeRenderer, batch, font);
			}
		}
		for (int i = 0; i < phisicalMemorySize(); i++) {
			shapeRenderer.setColor(colors.get(i));
			shapeRenderer.circle(x - 18, y + 20 * (phisicalMemorySize() - i - 1) + 23, 10);
		}

		// end button
		shapeRenderer.rect(20,
				Gdx.graphics.getHeight()
						- Math.max((pagesTableSize + 1) * 20 + 50, (phisicalMemorySize() + 1) * 20 + 70) - 50
						- 20 * pagesTableSize - 80,
				190, 30, Color.RED, Color.RED, Color.RED, Color.RED);
		// statistics button
		shapeRenderer.rect(230,
				Gdx.graphics.getHeight()
						- Math.max((pagesTableSize + 1) * 20 + 50, (phisicalMemorySize() + 1) * 20 + 70) - 50
						- 20 * pagesTableSize - 80,
				240, 30, Color.BLUE, Color.BLUE, Color.BLUE, Color.BLUE);
		// start/pause button
		shapeRenderer.rect(490,
				Gdx.graphics.getHeight()
						- Math.max((pagesTableSize + 1) * 20 + 50, (phisicalMemorySize() + 1) * 20 + 70) - 50
						- 20 * pagesTableSize - 80,
				190, 30, Color.YELLOW, Color.YELLOW, Color.YELLOW, Color.YELLOW);
		// new task
		shapeRenderer.rect(700,
				Gdx.graphics.getHeight()
						- Math.max((pagesTableSize + 1) * 20 + 50, (phisicalMemorySize() + 1) * 20 + 70) - 50
						- 20 * pagesTableSize - 80,
				160, 30, Color.GREEN, Color.GREEN, Color.GREEN, Color.GREEN);

		shapeRenderer.end();

		// DESCRIPTIONS

		batch.begin();
		font.setColor(Color.WHITE);
		// pagesTable
		for (int i = 0; i < pagesTableSize; i++) {
			if (method == 4) {
				if (referenceBits.get(i)) {
					font.setColor(Color.GREEN);
				} else {
					font.setColor(Color.RED);
				}
			}
			font.draw(batch, Integer.toString(i) + ":", 20, Gdx.graphics.getHeight() - i * 20 - 55);
			font.draw(batch, Integer.toString(i) + ":", 130, Gdx.graphics.getHeight()
					- Math.max((pagesTableSize + 1) * 20 + 50, (phisicalMemorySize() + 1) * 20 + 70) - 50 - i * 20);
		}
		font.setColor(Color.WHITE);
		for (int i = 0; i < pagesTable.size(); i++) {
			font.draw(batch, Integer.toString(pagesTable.get(i)), 60, Gdx.graphics.getHeight() - i * 20 - 53);
		}
		// references numbers
		for (int i = 0; i < phisicalMemorySize(); i++) {
			font.draw(batch, Integer.toString(i), x - 21, y + 20 * (phisicalMemorySize() - i - 1) + 28);
		}

		font.setColor(Color.WHITE);
		font.draw(batch, "Odwolanie:", 20, Gdx.graphics.getHeight()
				- Math.max((pagesTableSize + 1) * 20 + 50, (phisicalMemorySize() + 1) * 20 + 70) - 20);
		font.draw(batch, dataReferences, 200, Gdx.graphics.getHeight()
				- Math.max((pagesTableSize + 1) * 20 + 50, (phisicalMemorySize() + 1) * 20 + 70) - 20);
		font.draw(batch, "Tablica stron:", 20, Gdx.graphics.getHeight()
				- Math.max((pagesTableSize + 1) * 20 + 50, (phisicalMemorySize() + 1) * 20 + 70) - 50);
		font.draw(batch, "Tablica stron", 20, Gdx.graphics.getHeight() - 20);
		for (int i = 0; i < pagesTableSize; i++) {
			font.draw(batch, dataFrames.get(i).toString(), 200, Gdx.graphics.getHeight()
					- Math.max((pagesTableSize + 1) * 20 + 50, (phisicalMemorySize() + 1) * 20 + 70) - 50 - i * 20);
		}
		font.draw(batch, "Liczba brakow stron: " + Integer.toString(pageFaultCounter), 20,
				Gdx.graphics.getHeight()
						- Math.max((pagesTableSize + 1) * 20 + 50, (phisicalMemorySize() + 1) * 20 + 70) - 50
						- 20 * pagesTableSize - 10);

		// BUTTONS

		// end button
		font.draw(batch, "ZAKONCZ PROGRAM [E]", 30,
				Gdx.graphics.getHeight()
						- Math.max((pagesTableSize + 1) * 20 + 50, (phisicalMemorySize() + 1) * 20 + 70) - 50
						- 20 * pagesTableSize - 60);
		// statistics button
		font.draw(batch, "GENERUJ WYNIKI DO PLIKU [G]", 240,
				Gdx.graphics.getHeight()
						- Math.max((pagesTableSize + 1) * 20 + 50, (phisicalMemorySize() + 1) * 20 + 70) - 50
						- 20 * pagesTableSize - 60);
		// start/pause button
		font.draw(batch, "START/PAUSE [SPACJA]", 500,
				Gdx.graphics.getHeight()
						- Math.max((pagesTableSize + 1) * 20 + 50, (phisicalMemorySize() + 1) * 20 + 70) - 50
						- 20 * pagesTableSize - 60);
		// new task
		font.draw(batch, "NOWY PROCES [N]", 710,
				Gdx.graphics.getHeight()
						- Math.max((pagesTableSize + 1) * 20 + 50, (phisicalMemorySize() + 1) * 20 + 70) - 50
						- 20 * pagesTableSize - 60);
		batch.end();
	}

	public void drawAndCheckButtons(ShapeRenderer shapeRenderer, SpriteBatch batch, BitmapFont font) {
		switch (whatToDraw) {
		case 0:
			prepareTasks(shapeRenderer, batch, font);
			break;
		case 1:
			whichMethod(shapeRenderer, batch, font);
			break;
		default:
			drawSimulation(shapeRenderer, batch, font);
			checkRunButtons();
			break;
		}
	}

	private void generateStatistics() {
		String data = "RAPORT SYMULACJI ALGORYTMU ZASTÊPOWANIE STRON" + System.getProperty("line.separator");
		switch (method) {
		case 1:
			data += "UZYTA METODA: FIFO" + System.getProperty("line.separator");
			break;
		case 2:
			data += "UZYTA METODA: OPT" + System.getProperty("line.separator");
			break;
		case 3:
			data += "UZYTA METODA: LRU" + System.getProperty("line.separator");
			break;

		case 4:
			data += "UZYTA METODA: Druga Szansa" + System.getProperty("line.separator");
			break;
		}
		data += "Odwo³ania:\t";
		if (method != 4) {
			data += dataReferences.replace("      ", "\t") + System.getProperty("line.separator");
		} else {
			data += dataReferences.replace("      ", "\t\t") + System.getProperty("line.separator");
		}
		data += "Tablica stron";
		if (method == 4) {
			data += " i bity odniesienia";
		}
		data += ":" + System.getProperty("line.separator");
		for (int i = 0; i < dataFrames.size(); i++) {
			data += "Ramka nr " + i + ":\t";
			if (i <= pagesTable.size()) {
				if (method != 4) {
					data += dataFrames.get(i).replaceAll("        ", "\t").replaceAll("      ", "\t")
							+ System.getProperty("line.separator");
				} else {
					data += dataFrames.get(i).replaceAll("         ", "\t\t").replaceAll("   ", "\t")
							+ System.getProperty("line.separator");
				}
			} else {
				data += System.getProperty("line.separator");
			}
		}
		data += "Liczba braków stron: " + Integer.toString(pageFaultCounter) + System.getProperty("line.separator");

		FileHandle handle = Gdx.files.local("result.txt");
		handle.writeString(data, false);
	}

	public void checkRunButtons() {
		if (Gdx.input.isTouched()) {
			if (Gdx.graphics.getHeight() - Gdx.input.getY() < Gdx.graphics.getHeight()
					- Math.max((pagesTableSize + 1) * 20 + 50, (phisicalMemorySize() + 1) * 20 + 70) - 50
					- 20 * pagesTableSize - 50
					&& Gdx.graphics.getHeight() - Gdx.input.getY() > Gdx.graphics.getHeight()
							- Math.max((pagesTableSize + 1) * 20 + 50, (phisicalMemorySize() + 1) * 20 + 70) - 50
							- 20 * pagesTableSize - 80) {
				// exit
				if (Gdx.input.getX() > 20 && Gdx.input.getX() < 210) {
					Gdx.app.exit();
				}
				// statistics
				if (Gdx.input.getX() > 230 && Gdx.input.getX() < 470) {
					generateStatistics();
					Timer timer = new Timer();
					timer.delay(500);
				}
				// start/pause
				if (Gdx.input.getX() > 490 && Gdx.input.getX() < 680) {
					isOn = !isOn;
					Timer timer = new Timer();
					timer.delay(500);
				}
				// new task
				if (Gdx.input.getX() > 700 && Gdx.input.getX() < 860) {
					addReference();
					y = Gdx.graphics.getHeight() - phisicalMemorySize() * 20 - 50;
					height = phisicalMemorySize() * 20 + 25;
				}
			}
		}
		// statistics - e
		if (Gdx.input.isKeyJustPressed(Keys.E)) {
			Gdx.app.exit();
		}
		// statistics - g
		if (Gdx.input.isKeyJustPressed(Keys.G)) {
			generateStatistics();
		}
		// start/pause - space
		if (Gdx.input.isKeyJustPressed(Keys.SPACE)) {
			isOn = !isOn;
		}
		// new task - n
		if (Gdx.input.isKeyJustPressed(Keys.N)) {
			addReference();
			y = Gdx.graphics.getHeight() - phisicalMemorySize() * 20 - 50;
			height = phisicalMemorySize() * 20 + 25;
		}
	}
}
