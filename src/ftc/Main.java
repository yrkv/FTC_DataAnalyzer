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
	private double[][] motorSpeeds;
	private double[][] motorSpeedsSmooth;
	private int motorSpeedsSmoothness = 20;
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
	private Color toolColor1 = new Color(230, 230, 0);
	private Color toolColor2 = new Color(255, 255, 0, 50);

	private double startTime;

	private double minTime;
	private double maxTime;
	private int minEncPos;
	private int maxEncPos;

	private ArrayList<String> pathReconstruction;

	public GraphTool graphTool = new GraphTool(this);
	public GraphState graphState = GraphState.Time_Enc;

	public static int leftPanelWidth = 100;
	public static int bottomPanelHeight = 50;

	public int overviewYOffset = 0;
	public int xShift = 100, yShift = -50;
	public double zoom = 1;
	public double fakeZoom = 0;
	public double tempZoom = 0;
	private long lastZoom = 0;

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

		initVoltage = Double.parseDouble(f.nextLine());
		motorNames = f.nextLine().split(",");
		colorSensorNames = f.nextLine().split(",");
		name = file.getName();


		while (f.hasNextLine()) {
			f.nextLine();
			data.add(new State(f.nextLine(), f.nextLine(), f.nextLine(), f.nextLine()));
		}

		startTime = data.get(0).time;

		motorSpeeds = new double[motorNames.length][data.size() - 1];
		motorSpeedsSmooth = new double[motorNames.length][data.size() - motorSpeedsSmoothness - 1];

		for (int j = 0; j < motorNames.length; j++) {
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
						pathReconstruction.add(String.format("turn to %.2f %.2f", (dir-90), data.get(i).time-startTime));
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
						pathReconstruction.add(String.format("move %.2f %.2f", totalDist * 12.4, data.get(i).time-startTime));
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
			case GRAPH:
				drawGraphPowers(g);
				drawGrid(g);
				switch (graphState) {
					case Time_Enc:
						drawGraphsTE(g);
						break;
					case Time_Speed:
						drawGraphsTS(g);
						break;
				}
				break;
			case MAP:
				drawMap(g);
				break;
			case OVERVIEW:
				drawDataView(g);
				break;
		}
		drawMenu(g);
		if (menuState == MenuState.GRAPH)
			drawGraphMenu(g);
	}

	private void drawMainMenu(Graphics g) {
		for (int i = 0; i < listOfFiles.length; i++) {

			String name = listOfFiles[i].getName();
			if (this.name.equals(name))
				g.setColor(Color.LIGHT_GRAY);
			else
				g.setColor(Color.WHITE);
			g.fillRect(leftPanelWidth, 20*i, 100, 20);
			g.setColor(Color.BLACK);
			g.drawRect(leftPanelWidth, 20*i, 100, 20);
			g.drawString(name, leftPanelWidth + 5, 20*i + 15);
		}
	}

	private void drawGraphPowers(Graphics g) {
		if (data.size() > 0) {
			for (int j = 0; j < data.get(0).motorPositions.length; j++) {
				int[] xPoints = new int[data.size()];
				int[] yPoints = new int[data.size()];

				for (int i = 0; i < data.size(); i++) {
					xPoints[i] = timeToX(data.get(i).time);
					yPoints[i] = frame.getHeight() - bottomPanelHeight - 25 + (int) (25 * data.get(i).motorPowers[j]);
					xPoints[i] = (int) (xPoints[i] * zoom) + xShift;
				}

				g.setColor(new Color(motorColors[j].getRed(), motorColors[j].getGreen(), motorColors[j].getBlue(), 100));
				g.fillPolygon(xPoints, yPoints, data.size());
			}
		}
	}

	private void drawGraphsTE(Graphics g) {
		if (data.size() > 0) {
			for (int j = 0; j < data.get(0).motorPositions.length; j++) {
				int[] xPoints = new int[data.size()];
				int[] yPoints = new int[data.size()];

				for (int i = 0; i < data.size(); i++) {
					xPoints[i] = timeToX(data.get(i).time);
					yPoints[i] = encToY(data.get(i).motorPositions[j]);

					xPoints[i] = (int) (xPoints[i] * zoom) + xShift;
					yPoints[i] = (int) (yPoints[i] * zoom) + yShift;
				}

				g.setColor(motorColors[j]);
				g.drawPolyline(xPoints, yPoints, data.size());
			}
		}
	}

	private void drawGraphsTS(Graphics g) {
		double yScale = 100;

		if (data.size() > 0) {
			for (int j = 0; j < data.get(0).motorPositions.length; j++) {
				int[] xPoints = new int[motorSpeedsSmooth[j].length];
				int[] yPoints = new int[motorSpeedsSmooth[j].length];

				for (int i = 0; i < motorSpeedsSmooth[j].length; i++) {
					xPoints[i] = timeToX(data.get(i).time);
					yPoints[i] = encToY(motorSpeedsSmooth[j][i]*yScale);
					xPoints[i] = (int) (xPoints[i] * zoom) + xShift;
					yPoints[i] = (int) (yPoints[i] * zoom) + yShift;
				}

				g.setColor(motorColors[j]);
				g.drawPolyline(xPoints, yPoints, motorSpeedsSmooth[j].length);
			}
		}
	}

	private void drawGraphMenu(Graphics g) {
		g.setColor(Color.WHITE);
		g.fillRect(leftPanelWidth + 1, 0, frame.getWidth() - leftPanelWidth - 1, 20);
		g.setColor(Color.BLACK);
		g.drawLine(leftPanelWidth, 20, frame.getWidth(), 20);

		drawGraphKey(g);
		drawGraphTool(g);

		g.setColor(Color.WHITE);
		g.fillRect(leftPanelWidth, frame.getHeight() - bottomPanelHeight, frame.getWidth() - leftPanelWidth, 50);
		g.setColor(Color.BLACK);
		g.drawLine(leftPanelWidth, frame.getHeight() - bottomPanelHeight, frame.getWidth(), frame.getHeight() - bottomPanelHeight);

		GraphMenuButton[] graphMenuButtons = GraphMenuButton.values();
		for (GraphMenuButton button: graphMenuButtons) {
			if (button.x < 0) button.x = frame.getWidth() + button.x - leftPanelWidth;
			g.drawRect(button.x + leftPanelWidth, button.y + frame.getHeight() - bottomPanelHeight, button.width, button.height);
			g.drawString(button.name().replace('_', ' '), button.x + leftPanelWidth + 5, button.y + frame.getHeight() - 15);
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

		int size = (frame.getWidth() - leftPanelWidth) > frame.getHeight() ? frame.getHeight() : (frame.getWidth() - leftPanelWidth);
		g.drawImage(img, leftPanelWidth, 0, size, size, null);

		double countsPerInch = 270 / Math.PI;
		double pixelsToInch = size / 144.0;

		double dir = 90;

		int leftWheelIndex = 2, rightWheelIndex = 5;

		double[] xpoints = new double[data.size()];
		double[] ypoints = new double[data.size()];

		xpoints[0] = 84 * pixelsToInch;
		ypoints[0] = (144 - 3.5) * pixelsToInch;

		for (int i = 0; i < data.size()-1; i++) {

			dir = 90 + data.get(i).navXHeading;
			if  ((motorSpeeds[leftWheelIndex][i] > 0 && motorSpeeds[rightWheelIndex][i] > 0)
					|| (motorSpeeds[leftWheelIndex][i] < 0 && motorSpeeds[rightWheelIndex][i] < 0)) { // robot is moving forward
				double dist = pixelsToInch * (motorSpeeds[leftWheelIndex][i] + motorSpeeds[rightWheelIndex][i]) / 2 / countsPerInch * 12.4;
				xpoints[i+1] = xpoints[i] - Math.cos(dir * Math.PI / 180) * dist;
				ypoints[i+1] = ypoints[i] - Math.sin(dir * Math.PI / 180) * dist;

				if (xpoints[i+1] > pixelsToInch * 129.5)
					xpoints[i+1] = pixelsToInch * 129.5;
				if (xpoints[i+1] < 3.5)
					xpoints[i+1] = 3.5;
			} else {
				xpoints[i+1] = xpoints[i];
				ypoints[i+1] = ypoints[i];
			}

		}

		int[] xPoints = new int[data.size()];
		int[] yPoints = new int[data.size()];
		for (int i = 0; i <  data.size(); i++) {
			xPoints[i] = (int) xpoints[i] + leftPanelWidth;
			yPoints[i] = (int) ypoints[i];
		}


		Graphics2D g2 = (Graphics2D) g;
		g2.setStroke(new BasicStroke(3));

		g.setColor(toolColor1);
		g.drawPolyline(xPoints, yPoints, data.size());
		g2.setStroke(new BasicStroke());
	}

	private void drawDataView(Graphics g) {
		g.drawString("Initial Voltage: " + initVoltage, leftPanelWidth + 5, 15 + overviewYOffset);
		g.drawRect(leftPanelWidth, overviewYOffset, 120, 20);
		int height = 20;

		for (int i = 0; i < pathReconstruction.size(); i++) {
			String[] splitString = pathReconstruction.get(i).split(" ");
			String string = splitString[0] + " " + splitString[1];
			if (splitString.length == 4)
				string += " " + splitString[2];
			g.drawString(string, leftPanelWidth + 5, 20 * i + 35 + overviewYOffset);
			g.drawString(splitString[splitString.length-1], leftPanelWidth + 125, 20 * i + 35 + overviewYOffset);
			height += 20;
			g.drawRect(leftPanelWidth, 20*i+20+overviewYOffset, 120, 20);
		}

//		if (overviewYOffset + height > frame.getHeight()) {
//			overviewYOffset = frame.getHeight() - height;
//		}

		g.drawRect(frame.getWidth() - 20, (int)(((double)frame.getHeight()-100)*(overviewYOffset/(frame.getHeight()-height))), 20, 100);
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

	private void drawGraphTool(Graphics g) {
		g.drawString(graphTool.text, leftPanelWidth + 5, 15);

		switch (graphTool.currentTool) {
			case GraphTool.MEASURE_TIME_START:
				g.setColor(toolColor1);
				g.drawLine(mouse.x, 0, mouse.x, frame.getHeight());
				break;
			case GraphTool.MEASURE_TIME_END:
				graphTool.calculateMeasureTime(mouse.x);
				g.setColor(toolColor1);
				int x1 = graphTool.measureTimeStartX;
				int x2 = mouse.x;
				if (x1 > x2) {
					int temp = x1;
					x1 = x2;
					x2 = temp;
				}
				g.drawLine(x1, 0, x1, frame.getHeight());
				g.drawLine(x2, 0, x2, frame.getHeight());
				g.setColor(toolColor2);
				g.fillRect(x1, 0, x2 - x1, frame.getHeight());
				break;
			case GraphTool.SLOPE_CALC_START:
				g.setColor(toolColor1);
				g.fillOval(mouse.x-5, mouse.y-5, 10, 10);
				break;
			case GraphTool.SLOPE_CALC_END:
				graphTool.calculateSlopeCalc(mouse.x, mouse.y);
				g.setColor(toolColor1);
				g.fillOval(mouse.x-5, mouse.y-5, 10, 10);
				g.fillOval(graphTool.slopeCalcStart.x-5, graphTool.slopeCalcStart.y-5, 10, 10);
				g.setColor(toolColor2);
				Graphics2D g2 = (Graphics2D) g;
				g2.setStroke(new BasicStroke(4));
				g.drawLine(mouse.x, mouse.y, graphTool.slopeCalcStart.x, graphTool.slopeCalcStart.y);
				g2.setStroke(new BasicStroke());
				break;
		}
	}

	private void drawGrid(Graphics g) {
		Color gridColor = new Color(0xf0f0f0);
		for (double x = ((xShift)/zoom % 50) * zoom; x < frame.getWidth(); x += 50 * zoom) {
			g.setColor(gridColor);
			g.drawLine((int) x, 0, (int) x, frame.getHeight());
			g.setColor(Color.BLACK);
			String str = "" + (XToTime(x) - (int)startTime);
			g.drawString(str, (int) x + 2, frame.getHeight() - bottomPanelHeight - 52);

		}
		for (double y = ((yShift)/zoom % 50 - 20) * zoom; y < frame.getHeight(); y += 50 * zoom) {
			g.setColor(gridColor);
			g.drawLine(0, (int) y, frame.getWidth(), (int) y);
			g.setColor(Color.BLACK);
			String str = "" + YToEnc(y);
			g.drawString(str, leftPanelWidth + 52, (int) y - 2);
		}
		g.setColor(Color.BLACK);

		g.drawLine(0, frame.getHeight() - bottomPanelHeight - 50, frame.getWidth(), frame.getHeight() - 100);
		g.drawLine(leftPanelWidth + 50, 0, leftPanelWidth + 50, frame.getHeight());
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
