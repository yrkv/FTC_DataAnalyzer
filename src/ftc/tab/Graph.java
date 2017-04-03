package ftc.tab;

import ftc.GraphTool;
import ftc.Main;
import ftc.enums.GraphMenuButton;
import ftc.enums.GraphState;

import java.awt.*;

public class Graph {
	private Main main;
	public GraphState state = GraphState.Time_Enc;
	public GraphTool tool;
	public String[] motorNames = {};

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

	public Graph(Main main) {
		this.main = main;
		tool = new GraphTool(main);
	}

	public void drawMenu(Graphics g) {
		g.setColor(Color.WHITE);
		g.fillRect(main.leftPanelWidth + 1, 0, main.frame.getWidth() - main.leftPanelWidth - 1, 20);
		g.setColor(Color.BLACK);
		g.drawLine(main.leftPanelWidth, 20, main.frame.getWidth(), 20);

		drawKey(g);
		tool.render(g);

		g.setColor(Color.WHITE);
		g.fillRect(main.leftPanelWidth, main.frame.getHeight() - main.bottomPanelHeight,
				main.frame.getWidth() - main.leftPanelWidth, 50);
		g.setColor(Color.BLACK);
		g.drawLine(main.leftPanelWidth, main.frame.getHeight() - main.bottomPanelHeight,
				main.frame.getWidth(), main.frame.getHeight() - main.bottomPanelHeight);

		GraphMenuButton[] graphMenuButtons = GraphMenuButton.values();
		for (GraphMenuButton button: graphMenuButtons) {
			if (button.x < 0) button.x = main.frame.getWidth() + button.x - main.leftPanelWidth;
			g.drawRect(button.x + main.leftPanelWidth, button.y + main.frame.getHeight() - main.bottomPanelHeight, button.width, button.height);
			g.drawString(button.name().replace('_', ' '), button.x + main.leftPanelWidth + 5, button.y + main.frame.getHeight() - 15);
		}
	}

	private void drawKey(Graphics g) {
		for (int i = 0; i < motorNames.length; i++) {
			if (main.motorsActive[i])
				g.setColor(motorColors[i]);
			else
				g.setColor(new Color(motorColors[i].getRed(), motorColors[i].getGreen(), motorColors[i].getBlue(), 80));
			g.fillRect(5, main.frame.getHeight() - i*20 - 15, 10, 10);
			g.setColor(Color.BLACK);
			g.drawString(motorNames[i].substring(1, motorNames[i].length() - 1), 17, main.frame.getHeight() - i * 20 - 5);
		}
	}

	public void render(Graphics g) {
		drawPowers(g);
		drawGrid(g);
		switch (state) {
			case Time_Enc:
				drawTE(g);
				break;
			case Time_Speed:
				drawTS(g);
				break;
		}
	}

	private void drawPowers(Graphics g) {
		if (main.data.size() > 0) {
			for (int j = 0; j < main.data.get(0).motorPositions.length; j++) {
				int[] xPoints = new int[main.data.size() + 2];
				int[] yPoints = new int[main.data.size() + 2];

				for (int i = 0; i < main.data.size(); i++) {
					xPoints[i+1] = main.timeToX(main.data.get(i).time);
					yPoints[i+1] = main.frame.getHeight() - main.bottomPanelHeight - 25 + (int) (25 * main.data.get(i).motorPowers[j]);
					xPoints[i+1] = (int) (xPoints[i+1] * main.zoom) + main.xShift;
				}
				xPoints[0] = (int) (main.timeToX(main.minTime) * main.zoom + main.xShift);
				xPoints[xPoints.length - 1] = (int) (main.timeToX(main.maxTime) * main.zoom + main.xShift);
				yPoints[0] = main.frame.getHeight() - main.bottomPanelHeight - 25;
				yPoints[yPoints.length - 1] = main.frame.getHeight() - main.bottomPanelHeight - 25;

				int a = main.motorsActive[j] ? 100 : 20;
				g.setColor(new Color(motorColors[j].getRed(), motorColors[j].getGreen(), motorColors[j].getBlue(), a));
				g.fillPolygon(xPoints, yPoints, main.data.size()+2);
			}
		}
	}

	private void drawGrid(Graphics g) {
		Color gridColor = new Color(0xf0f0f0);
		for (double x = ((main.xShift)/main.zoom % 50) * main.zoom; x < main.frame.getWidth(); x += 50 * main.zoom) {
			g.setColor(gridColor);
			g.drawLine((int) x, 0, (int) x, main.frame.getHeight());
			g.setColor(Color.BLACK);
			String str = "" + (main.XToTime(x) - (int) main.minTime);
			g.drawString(str, (int) x + 2, main.frame.getHeight() - main.bottomPanelHeight - 52);

		}
		for (double y = ((main.yShift)/main.zoom % 50 - 20) * main.zoom; y < main.frame.getHeight(); y += 50 * main.zoom) {
			g.setColor(gridColor);
			g.drawLine(0, (int) y, main.frame.getWidth(), (int) y);
			g.setColor(Color.BLACK);
			String str = "" + main.YToEnc(y) / ((state == GraphState.Time_Speed) ? main.yScale : 1);
			g.drawString(str, main.leftPanelWidth + 52, (int) y - 2);
		}
		g.setColor(Color.BLACK);

		g.drawLine(0, main.frame.getHeight() - main.bottomPanelHeight - 50, main.frame.getWidth(), main.frame.getHeight() - 100);
		g.drawLine(main.leftPanelWidth + 50, 0, main.leftPanelWidth + 50, main.frame.getHeight());
	}

	private void drawTE(Graphics g) {
		if (main.data.size() > 0) {
			for (int j = 0; j < main.data.get(0).motorPositions.length; j++) {
				if (!main.motorsActive[j]) continue;
				int[] xPoints = new int[main.data.size()];
				int[] yPoints = new int[main.data.size()];

				for (int i = 0; i < main.data.size(); i++) {
					xPoints[i] = main.timeToX(main.data.get(i).time);
					yPoints[i] = main.encToY(main.data.get(i).motorPositions[j]);

					xPoints[i] = (int) (xPoints[i] * main.zoom) + main.xShift;
					yPoints[i] = (int) (yPoints[i] * main.zoom) + main.yShift;
				}

				g.setColor(motorColors[j]);
				g.drawPolyline(xPoints, yPoints, main.data.size());
			}
		}
	}

	private void drawTS(Graphics g) {
		if (main.data.size() > 0) {
			for (int j = 0; j < main.data.get(0).motorPositions.length; j++) {
				if (!main.motorsActive[j]) continue;
				int[] xPoints = new int[main.motorSpeedsSmooth[j].length];
				int[] yPoints = new int[main.motorSpeedsSmooth[j].length];

				for (int i = 0; i < main.motorSpeedsSmooth[j].length; i++) {
					xPoints[i] = main.timeToX(main.data.get(i).time);
					yPoints[i] = main.encToY(main.motorSpeedsSmooth[j][i]*main.yScale);
					xPoints[i] = (int) (xPoints[i] * main.zoom) + main.xShift;
					yPoints[i] = (int) (yPoints[i] * main.zoom) + main.yShift;
				}

				g.setColor(motorColors[j]);
				g.drawPolyline(xPoints, yPoints, main.motorSpeedsSmooth[j].length);
			}
		}
	}
}
