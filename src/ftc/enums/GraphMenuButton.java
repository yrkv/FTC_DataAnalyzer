package ftc.enums;

public enum GraphMenuButton {
	MEASURE_TIME (0, 0, 100, 50),
	SLOPE_CALC (100, 0, 100, 50);

	public int x, y, width, height;

	GraphMenuButton(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
}