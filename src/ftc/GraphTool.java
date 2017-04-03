package ftc;

import ftc.enums.GraphState;

import java.awt.*;

public class GraphTool {
	public static final int VIEW = 0,
			MEASURE_TIME_START = 1,
			MEASURE_TIME_END = 2,
			SLOPE_CALC_START = 3,
			SLOPE_CALC_END = 4,
			MEASURE_X = 5,
			MEASURE_Y = 6,
			GYRO = 7;

	public Color color1 = new Color(230, 230, 0);
	public Color color2 = new Color(255, 255, 0, 50);

	public int currentTool = VIEW;
	private Main main;

	public String text = "";

	private int measureTimeStartTime = 0;
	public int measureTimeStartX = 0;

	public Point slopeCalcStart = new Point(0, 0);

	public GraphTool(Main main) {
		this.main = main;
	}

	public void render(Graphics g) {
		g.drawString(text, main.leftPanelWidth + 5, 15);

		switch (currentTool) {
			case MEASURE_TIME_START:
				g.setColor(color1);
				g.drawLine(main.mouse.x, 0, main.mouse.x, main.frame.getHeight());
				break;
			case GraphTool.MEASURE_TIME_END:
				calculateMeasureTime(main.mouse.x);
				g.setColor(color1);
				int x1 = measureTimeStartX;
				int x2 = main.mouse.x;
				if (x1 > x2) {
					int temp = x1;
					x1 = x2;
					x2 = temp;
				}
				g.drawLine(x1, 0, x1, main.frame.getHeight());
				g.drawLine(x2, 0, x2, main.frame.getHeight());
				g.setColor(color2);
				g.fillRect(x1, 0, x2 - x1, main.frame.getHeight());
				break;
			case GraphTool.SLOPE_CALC_START:
				g.setColor(color1);
				g.fillOval(main.mouse.x-5, main.mouse.y-5, 10, 10);
				break;
			case GraphTool.SLOPE_CALC_END:
				calculateSlopeCalc(main.mouse.x, main.mouse.y);
				g.setColor(color1);
				g.fillOval(main.mouse.x-5, main.mouse.y-5, 10, 10);
				g.fillOval(slopeCalcStart.x-5, slopeCalcStart.y-5, 10, 10);
				g.setColor(color2);
				Graphics2D g2 = (Graphics2D) g;
				g2.setStroke(new BasicStroke(4));
				g.drawLine(main.mouse.x, main.mouse.y, slopeCalcStart.x, slopeCalcStart.y);
				g2.setStroke(new BasicStroke());
				break;
			case GraphTool.MEASURE_X:
				calculateMeasureX(main.mouse.x);
				g.setColor(color1);
				g.drawLine(main.mouse.x, 21, main.mouse.x, main.frame.getHeight());
				break;
			case GraphTool.MEASURE_Y:
				calculateMeasureY(main.mouse.y);
				g.setColor(color1);
				g.drawLine(main.leftPanelWidth, main.mouse.y, main.frame.getWidth(), main.mouse.y);
				break;
			case GraphTool.GYRO:
				calculateGyro(main.mouse.x);
				g.setColor(color1);
				g.drawLine(main.mouse.x, 21, main.mouse.x, main.frame.getHeight());
				break;
		}
	}

	public void startMeasureTime(int x) {
		currentTool = MEASURE_TIME_END;
		measureTimeStartX = x;
		measureTimeStartTime = main.XToTime(x);
	}

	public int calculateMeasureTime(int x) {
		int t = Math.abs(main.XToTime(x) - measureTimeStartTime);
		text = "" + t;
		return t;
	}

	public int endMeasureTime(int x) {
		currentTool = VIEW;
		return calculateMeasureTime(x);
	}

	public void startSlopeCalc(int x, int y) {
		currentTool = SLOPE_CALC_END;
		slopeCalcStart = new Point(x, y);
	}

	public double calculateSlopeCalc(int x, int y) {
		double slope = (double) (main.YToEnc(y) - main.YToEnc(slopeCalcStart.getY())) / (main.XToTime(x) - main.XToTime(slopeCalcStart.getX()));
		text = String.format("%.2f", slope);
		return slope;
	}

	public double endSlopeCalc(int x, int y) {
		currentTool = VIEW;
		return calculateSlopeCalc(x, y);
	}

	public double calculateMeasureX(int x) {
		text = main.XToTime(x) - main.minTime + "";
		return main.XToTime(x) - main.minTime;
	}

	public double endMeasureX(int x) {
		currentTool = VIEW;
		return calculateMeasureX(x);
	}

	public double calculateMeasureY(int y) {
		double value = 0;
		if (main.graph.state == GraphState.Time_Enc) {
			value = main.YToEnc(y);
		} else if (main.graph.state == GraphState.Time_Speed) {
			value = main.YToEnc(y) / main.yScale;
		}
		text = value + "";
		return value;
	}

	public double endMeasureY(int y) {
		currentTool = VIEW;
		return calculateMeasureY(y);
	}

	public double calculateGyro(int x) {
		int time = main.XToTime(x);

		int i = 0;
		for (; i < main.data.size() - 1; i++) {
			if (main.data.get(i).time >= time)
				break;
		}

		text = String.format("%.2f", main.data.get(i).navXHeading);
		return main.data.get(i).navXHeading;
	}

	public double endGyro(int x) {
		currentTool = VIEW;
		return calculateGyro(x);
	}
}