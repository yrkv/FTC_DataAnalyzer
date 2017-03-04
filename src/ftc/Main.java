package ftc;

import ftc.enums.*;
import ftc.enums.Button;
import ftc.mouse.Mouse;

import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Scanner;

public class Main extends JFrame {
	public JFrame frame = new JFrame();
	private String name = "";
	private double initVoltage;
	private ArrayList<State> data = new ArrayList<>();
	public MenuState menuState = MenuState.MAIN_MENU;
	private Mouse mouse = new Mouse(this);
	private File folder = new File("res/records");
	public File[] listOfFiles = folder.listFiles();
	private String[] motorNames = {};
	private String[] colorSensorNames = {};
	private Color[] motorColors = {
			new Color(255, 0, 0),
			new Color(0, 255, 0),
			new Color(0, 0, 255),
			new Color(0, 255, 255),
			new Color(255, 0, 255),
			new Color(255, 120, 0),
			new Color(0, 160, 160),
			new Color(0, 0, 0),
	};
	private double minTime;
	private double maxTime;
	private int minEncPos;
	private int maxEncPos;

	public GraphTool graphTool = new GraphTool(this);


	public int xShift = 0, yShift = 0;
	public double zoom = 1;

	public static void main(String[] args) throws IOException {
		Main main = new Main();
	}

	public Main() throws IOException {
		frame();

		while (true) {
			frame.repaint();
		}
	}

	public void readFile(File file) throws FileNotFoundException {
		data = new ArrayList<>();
		Scanner f = new Scanner(file);

		initVoltage = Double.parseDouble(f.nextLine());
		motorNames = f.nextLine().split(",");
		colorSensorNames = f.nextLine().split(",");
		name = file.getName();

		while (f.hasNextLine()) {
			f.nextLine();
			data.add(new State(f.nextLine(), f.nextLine(), f.nextLine(), f.nextLine()));
		}

		minEncPos = Integer.MAX_VALUE;
		maxEncPos = Integer.MIN_VALUE;

		minTime = Integer.MAX_VALUE;
		maxTime = Integer.MIN_VALUE;

		for (int i = 0; i < data.size(); i++) {
			for (int j = 0; j < data.get(0).motorPositions.length; j++) {
				if (data.get(i).motorPositions[j] < minEncPos)
					minEncPos = data.get(i).motorPositions[j];
				if (data.get(i).motorPositions[j] > maxEncPos)
					maxEncPos = data.get(i).motorPositions[j];
			}
		}

		for (int i = 0; i < data.size(); i++) {
			if (data.get(i).time < minTime)
				minTime = data.get(i).time;
			if (data.get(i).time > maxTime)
				maxTime = data.get(i).time;
		}

		f.close();
	}

	private void frame() {
		frame.setUndecorated(true);
		frame.setBackground(Color.WHITE);
		frame.setVisible(true);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setSize(frame.getWidth(), frame.getHeight());
		frame.setLocationRelativeTo(null);

		JPanel panel = new JPanel()
		{
			public void paintComponent(Graphics g)
			{

				try {
					render(g);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};

		frame.add(panel);
	}

	private void render(Graphics g) throws IOException {
		switch (menuState) {
			case MAIN_MENU:
				drawMainMenu(g);
				break;
			case GRAPHS:
				drawGrid(g);
				drawGraphs(g);
				break;
			case MAP:
				drawMap(g);
				break;
			case OVERVIEW:
				drawDataView(g);
				break;
		}
		drawMenu(g);
		if (menuState == MenuState.GRAPHS)
			drawGraphMenu(g);
	}

	private void drawMainMenu(Graphics g) {
		for (int i = 0; i < listOfFiles.length; i++) {

			String name = listOfFiles[i].getName();
			if (this.name.equals(name))
				g.setColor(Color.LIGHT_GRAY);
			else
				g.setColor(Color.WHITE);
			g.fillRect(100, 20*i, 100, 20);
			g.setColor(Color.BLACK);
			g.drawRect(100, 20*i, 100, 20);
			g.drawString(name, 105, 20*i + 15);
		}
	}

	private void drawGraphs(Graphics g) {
		if (data.size() > 0) {
			for (int j = 0; j < data.get(0).motorPositions.length; j++) {
				int[] xPoints = new int[data.size()];
				int[] yPoints = new int[data.size()];

				for (int i = 0; i < data.size(); i++) {
					xPoints[i] = timeToX(data.get(i).time);
					yPoints[i] = (int) (((maxEncPos - data.get(i).motorPositions[j]) * (frame.getHeight() - 100) / (maxEncPos - minEncPos)) + 50 + yShift);

					xPoints[i] = (int) (xPoints[i] * zoom);
					yPoints[i] = (int) (yPoints[i] * zoom);
				}

				g.setColor(motorColors[j]);
				g.drawPolyline(xPoints, yPoints, data.size());
			}
		}
	}

	private void drawGraphMenu(Graphics g) {
		g.setColor(Color.WHITE);
		g.fillRect(101, 0, frame.getWidth() - 101, 20);
		g.setColor(Color.BLACK);
		g.drawLine(100, 20, frame.getWidth(), 20);

		drawGraphKey(g);
		drawGraphTool(g);

		g.setColor(Color.WHITE);
		g.fillRect(100, frame.getHeight() - 50, frame.getWidth() - 100, 50);
		g.setColor(Color.BLACK);
		g.drawLine(100, frame.getHeight() - 100, 100, frame.getHeight());
		g.drawLine(100, frame.getHeight() - 50, frame.getWidth(), frame.getHeight() - 50);

		GraphMenuButton[] graphMenuButtons = GraphMenuButton.values();
		for (GraphMenuButton button: graphMenuButtons) {
			g.drawRect(button.x + 100, button.y + frame.getHeight() - 50, button.width, button.height);
			g.drawString(button.name(), button.x + 105, button.y + frame.getHeight() - 15);
		}
	}

	private void drawGraphKey(Graphics g) {
		for (int i = 0; i < motorNames.length; i++) {
			g.setColor(motorColors[i]);
			g.fillRect(5, frame.getHeight() - i*20 - 15, 10, 10);
			g.setColor(Color.BLACK);
			g.drawString(motorNames[i].substring(1, motorNames[i].length() - 1), 17, frame.getHeight() - i * 20 - 5);
		}
	}

	private void drawMap(Graphics g) throws IOException {
		BufferedImage img = ImageIO.read(new File("res/image00.png"));

		int size = (frame.getWidth() - 100) > frame.getHeight() ? frame.getHeight() : (frame.getWidth() - 100);
		g.drawImage(img, 100, 0, size, size, null);
	}

	private void drawDataView(Graphics g) {

	}

	private void drawMenu(Graphics g) {
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, 100, frame.getHeight());

		Button[] values = Button.values();
		for (int i = 0; i < values.length; i++) {
			Button button = values[i];
			g.setColor(Color.LIGHT_GRAY);
			if (menuState == button.menuState)
				g.fillRect(button.x, button.y, button.width, button.height);
			g.setColor(Color.BLACK);
			g.drawLine(100, 0, 100, frame.getHeight());
			g.drawRect(button.x, button.y, button.width, button.height);
			g.drawString(button.name(), button.x + 5, button.y + button.height - 5);
		}
	}

	private void drawGraphTool(Graphics g) {
		g.drawString(graphTool.text, 105, 15);

		switch (graphTool.currentTool) {
			case GraphTool.MEASURE_TIME_START:
				g.setColor(new Color(230, 230, 0));
				g.drawLine(mouse.x, 0, mouse.x, frame.getHeight());
				break;
			case GraphTool.MEASURE_TIME_END:
				graphTool.calculateMeasureTime(mouse.x);
				g.setColor(new Color(230, 230, 0));
				int x1 = graphTool.measureTimeStartX;
				int x2 = mouse.x;
				if (x1 > x2) {
					int temp = x1;
					x1 = x2;
					x2 = temp;
				}
				g.drawLine(x1, 0, x1, frame.getHeight());
				g.drawLine(x2, 0, x2, frame.getHeight());
				g.setColor(new Color(255, 255, 0, 50));
				g.fillRect(x1, 0, x2 - x1, frame.getHeight());
				break;
		}
	}

	private void drawGrid(Graphics g) {
		Color gridColor = new Color(0xf0f0f0);
		for (double x = (xShift % 50 - 50) * zoom; x < frame.getWidth(); x += 50 * zoom) {
			g.setColor(gridColor);
			g.drawLine((int) x, 0, (int) x, frame.getHeight());
			g.setColor(Color.BLACK);
			String str = "" + XToTime(x);
			g.drawString(str, (int) x + 2, frame.getHeight() - 102);

		}
		for (double y = (yShift % 50 - 50) * zoom; y < frame.getHeight(); y += 50 * zoom) {
			g.setColor(gridColor);
			g.drawLine(0, (int) y, frame.getWidth(), (int) y);
			g.setColor(Color.BLACK);
			String str = "" + (int) (maxEncPos - (y / zoom - 50 - yShift) * (maxEncPos - minEncPos) / (frame.getHeight() - 100));
			g.drawString(str, 152, (int) y - 2);
		}
		g.setColor(Color.BLACK);

		g.drawLine(0, frame.getHeight() - 100, frame.getWidth(), frame.getHeight() - 100);
		g.drawLine(150, 0, 150, 1080);
	}

	public int XToTime(double x) {
		return (int) ((x / zoom - 100 - xShift) * (maxTime - minTime) / (frame.getWidth() - 100) + minTime);
	}

	public int timeToX(double time) {
		return (int) (((time - minTime) * (frame.getWidth() - 100) / (maxTime - minTime)) + 100 + xShift);
	}
}
