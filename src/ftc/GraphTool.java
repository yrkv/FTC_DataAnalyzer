package ftc;

public class GraphTool {
	public static final int VIEW = 0, MEASURE_TIME_START = 1, MEASURE_TIME_END = 2, SLOPE_CALC_START = 3, SLOPE_CALC_END = 4;
	public int currentTool = VIEW;
	private Main main;

	public String text = "";

	private int measureTimeStart = 0;
	public int measureTimeStartX = 0;

	public GraphTool(Main main) {
		this.main = main;
	}

	public void startMeasureTime(int x) {
		currentTool = MEASURE_TIME_START;
		measureTimeStartX = x;
		measureTimeStart = main.XToTime(x);
	}

	public int calculateMeasureTime(int x) {
		int t = Math.abs(main.XToTime(x) - measureTimeStart);
		text = "" + t;
		return t;
	}

	public int endMeasureTime(int x) {
		currentTool = VIEW;
		return  calculateMeasureTime(x);
	}

	public void startSlopeCalc(int x, int y) {

	}

	public int calculateSlopeCalc(int x, int y) {
		return 0;
	}

	public int endSlopeCalc(int x, int y) {
		return 0;
	}
}