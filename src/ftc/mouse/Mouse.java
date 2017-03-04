package ftc.mouse;

import ftc.Main;

public class Mouse {
	protected Main main;
	private MouseMotion mouseMotion = new MouseMotion(this);
	private MouseEvents mouseEvents = new MouseEvents(this);
	private MouseWheel mouseWheel = new MouseWheel(this);

	public boolean dragging = false;
	public int x, y;

	public Mouse(Main main) {
		main.frame.addMouseListener(mouseEvents);
		main.frame.addMouseMotionListener(mouseMotion);
		main.frame.addMouseWheelListener(mouseWheel);
		this.main = main;
	}
}
