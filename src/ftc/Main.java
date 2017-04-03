package ftc;

import ftc.enums.*;
import ftc.enums.Button;
import ftc.mouse.Mouse;
import ftc.tab.Graph;
import ftc.tab.MainMenu;
import ftc.tab.Map;
import ftc.tab.Overview;

import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Comparator;
import java.util.Scanner;

public class Main extends JFrame {
	public Graph graph = new Graph(this);
	private MainMenu mainMenu = new MainMenu(this);
	public Map map = new Map(this);
	public Overview overview = new Overview(this);

	public JFrame frame = new JFrame();
	public ArrayList<State> data = new ArrayList<>();
	public double[][] motorSpeeds;
	public double[][] motorSpeedsSmooth;
	private int motorSpeedsSmoothness = 1;
	public MenuState menuState = MenuState.MAIN_MENU;
	public Mouse mouse = new Mouse(this);
	private File folder = new File("res/records");
	public File[] listOfFiles = folder.listFiles();
	public boolean[] motorsActive;
	private String[] colorSensorNames = {};

	public double minTime;
	public double maxTime;
	public int minEncPos;
	public int maxEncPos;

	public ArrayList<String> pathReconstruction;

	public int leftPanelWidth = 100;
	public int bottomPanelHeight = 50;

	public int xShift = 100, yShift = -50;
	public double zoom = 1;
	public double fakeZoom = 0;
	public double tempZoom = 0;
	private long lastZoom = 0;

	public double yScale = 100;

	public static void main(String[] args) throws IOException {
		Main main = new Main();
	}

	public Main() throws IOException {
		frame();

		while (true) {
			frame.repaint();
			changeZoom();
		}
	}

	public void readFile(File file) throws FileNotFoundException {
		data = new ArrayList<>();
		Scanner f = new Scanner(file);

//		initVoltage = Double.parseDouble(f.nextLine());
		overview.initVoltage = Double.parseDouble(f.nextLine());
		graph.motorNames = f.nextLine().split(",");
		colorSensorNames = f.nextLine().split(",");
		mainMenu.currentFile = file.getName();
		motorsActive = new boolean[graph.motorNames.length];
		for (int i = 0; i < motorsActive.length; i++)
			motorsActive[i] = true;

		while (f.hasNextLine()) {
			f.nextLine();
			data.add(new State(f.nextLine(), f.nextLine(), f.nextLine(), f.nextLine()));
		}

		minTime = data.get(0).time;

		motorSpeeds = new double[graph.motorNames.length][data.size() - 1];
		motorSpeedsSmooth = new double[graph.motorNames.length][data.size() - motorSpeedsSmoothness - 1];

		for (int j = 0; j < graph.motorNames.length; j++) {
			for (int i = 0; i < data.size() - 1; i++) {
				motorSpeeds[j][i] = (data.get(i+1).motorPositions[j] - data.get(i).motorPositions[j])
						/ (data.get(i+1).time - data.get(i).time);
			}

			for (int i = 0; i < motorSpeeds[j].length - motorSpeedsSmoothness; i++) {
				double sum = 0;
				for (int k = i; k < i+motorSpeedsSmoothness; k++)
					sum += motorSpeeds[j][k];
				motorSpeedsSmooth[j][i] = sum / motorSpeedsSmoothness;
			}
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

		double dir = 90;

		double countsPerInch = 270 / Math.PI;

		int leftWheelIndex = 2, rightWheelIndex = 5;

		double[] xpoints = new double[data.size()];
		double[] ypoints = new double[data.size()];

		int last = -1;
		double totalDist = 0;
		double lastDir = 90;

		pathReconstruction = new ArrayList<>();

		for (int i = 0; i < data.size()-1; i++) {

			dir = 90 + data.get(i).navXHeading;
			if  ((motorSpeeds[leftWheelIndex][i] > 0 && motorSpeeds[rightWheelIndex][i] > 0)
					|| (motorSpeeds[leftWheelIndex][i] < 0 && motorSpeeds[rightWheelIndex][i] < 0)) { // robot is moving forward
				double dist = (motorSpeeds[leftWheelIndex][i] + motorSpeeds[rightWheelIndex][i]) / 2 / countsPerInch;

				if (last == 0)
					totalDist += dist;
				if (last == 1) {
					if (Math.abs(lastDir - dir) > 0)
						pathReconstruction.add(String.format("turn to %.2f %.2f", (dir-90), data.get(i).time- minTime));
					lastDir = dir;
				}

				xpoints[i+1] = xpoints[i] + dist;
				ypoints[i+1] = ypoints[i] + dist;
				last = 0;
			} else {
				xpoints[i+1] = xpoints[i];
				ypoints[i+1] = ypoints[i];

				if (last == 0) {
					if (totalDist > 0.1) {
						pathReconstruction.add(String.format("move %.2f %.2f", totalDist * 12.4, data.get(i).time- minTime));
						totalDist = 0;
					}
				}

				last = 1;
			}

		}


		for (int i = 0; i < pathReconstruction.size() - 1; i++) {
			String[] splitPath1 = pathReconstruction.get(i).split(" ");
			String[] splitPath2 = pathReconstruction.get(i+1).split(" ");
			if (splitPath1.length == 3 && splitPath2.length == 3) {
				pathReconstruction.remove(i+1);
				pathReconstruction.set(i, String.format("move %.2f %s", (Double.parseDouble(splitPath1[1]) + Double.parseDouble(splitPath2[1])), splitPath2[2]));
				i--;
			}
			if (splitPath1.length == 4 && splitPath2.length == 4) {
				pathReconstruction.remove(i);
			}
		}

		f.close();
	}

	private void frame() {
		BufferedImage img = new BufferedImage(50, 50, BufferedImage.TYPE_INT_ARGB);
		Graphics imgG = img.getGraphics();

		imgG.setColor(Color.BLACK);
		imgG.drawLine(5, 0, 5, 10);
		imgG.drawLine(0, 5, 10, 5);

		frame.setCursor(frame.getToolkit().createCustomCursor(img, new Point(5, 5), "null"));

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
				mainMenu.render(g);
				break;
			case GRAPH:
				graph.render(g);
				break;
			case MAP:
				map.render(g);
				break;
			case OVERVIEW:
				overview.render(g);
				break;
		}
		drawMenu(g);
		if (menuState == MenuState.GRAPH)
			graph.drawMenu(g);
	}

	private void drawMenu(Graphics g) {
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, leftPanelWidth, frame.getHeight());

		Button[] values = Button.values();
		for (int i = 0; i < values.length; i++) {
			Button button = values[i];
			g.setColor(Color.LIGHT_GRAY);
			if (menuState == button.menuState)
				g.fillRect(button.x, button.y, button.width, button.height);
			g.setColor(Color.BLACK);
			g.drawRect(button.x, button.y, button.width, button.height);
			g.drawString(button.name().replace('_', ' '), button.x + 5, button.y + button.height - 5);
		}
		g.drawLine(leftPanelWidth, 0, leftPanelWidth, frame.getHeight());
	}

	private void changeZoom() {
		if (System.currentTimeMillis() > lastZoom) {
			if (fakeZoom > tempZoom) {
				fakeZoom -= 0.05;
			}
			if (fakeZoom < tempZoom) {
				fakeZoom += 0.05;
			}
			lastZoom = System.currentTimeMillis();
			zoom = Math.pow(2, fakeZoom / 10.0);
		}
	}

	public int XToTime(double x) {
		return (int) ((x / zoom - leftPanelWidth - (xShift) / zoom) * (maxTime - minTime) / (frame.getWidth() - leftPanelWidth) + minTime);
	}

	public int timeToX(double time) {
		return (int) (((time - minTime) * (frame.getWidth() - leftPanelWidth) / (maxTime - minTime)) + leftPanelWidth);
	}

	public int encToY(double enc) {
		return (int) ((maxEncPos - enc) * (frame.getHeight() - leftPanelWidth) / (maxEncPos - minEncPos)) + bottomPanelHeight;
	}

	public int YToEnc(double y) {
		return (int) (maxEncPos - (y / zoom - bottomPanelHeight - (yShift) / zoom) * (maxEncPos - minEncPos) / (frame.getHeight() - bottomPanelHeight - 50));
	}
}
