package ftc;

import java.awt.*;

public class GraphTool {
	public static final int VIEW = 0, MEASURE_TIME_START = 1, MEASURE_TIME_END = 2, SLOPE_CALC_START = 3, SLOPE_CALC_END = 4;
	public int currentTool = VIEW;
	private Main main;

	public String text = "";

	private int measureTimeStartTime = 0;
	public int measureTimeStartX = 0;

	public Point slopeCalcStart = new Point(0, 0);

	public GraphTool(Main main) {
		this.main = main;
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
		return  calculateSlopeCalc(x, y);
	}
}