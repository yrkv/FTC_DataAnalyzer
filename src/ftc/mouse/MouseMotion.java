package ftc.mouse;

import ftc.GraphTool;
import ftc.enums.MenuState;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

public class MouseMotion implements MouseMotionListener {
	private Mouse mouse;
	private int lastX = 0, lastY = 0;

	public MouseMotion(Mouse mouse) {
		this.mouse = mouse;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		mouse.x = e.getX();
		mouse.y = e.getY();
		if (mouse.main.menuState == MenuState.GRAPHS && mouse.main.graphTool.currentTool == GraphTool.VIEW) {
			mouse.main.yShift -= (lastY - e.getY()) / mouse.main.zoom;
			mouse.main.xShift -= (lastX - e.getX()) / mouse.main.zoom;
			lastX = e.getX();
			lastY = e.getY();
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		mouse.x = e.getX();
		mouse.y = e.getY();
		lastX = e.getX();
		lastY = e.getY();
	}
}
