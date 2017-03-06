package ftc.enums;

public enum GraphMenuButton {
	Measure_Time (0, 0, 100, 50),
	Slope_Calculator (100, 0, 100, 50),
	Encoder_Position(-100, 0, 100, 50),
	Speed(-200, 0, 100, 50);

	public int x, y, width, height;

	GraphMenuButton(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
}