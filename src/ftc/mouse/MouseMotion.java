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
		if (mouse.main.menuState == MenuState.GRAPH && mouse.main.graph.tool.currentTool == GraphTool.VIEW) {
			mouse.main.yShift -= (lastY - e.getY());
			mouse.main.xShift -= (lastX - e.getX());
			lastX = e.getX();
			lastY = e.getY();
		}
		if (mouse.main.menuState == MenuState.MAP) {
			if (mouse.x > mouse.main.frame.getWidth() - 50
					&& mouse.x < mouse.main.frame.getWidth()) {
				mouse.main.map.sliderTime = (int) (1.0 * mouse.y / (mouse.main.frame.getHeight()+50) * (mouse.main.maxTime - mouse.main.minTime));
			} else if (mouse.x > mouse.main.leftPanelWidth) {
				int size = (mouse.main.frame.getWidth() - mouse.main.leftPanelWidth) > mouse.main.frame.getHeight() ?
						mouse.main.frame.getHeight() : (mouse.main.frame.getWidth() - mouse.main.leftPanelWidth);

				double pixelsToInch = size / 144.0;
				if (!e.isShiftDown()) {
					mouse.main.map.startX = (e.getX() - mouse.main.leftPanelWidth) / pixelsToInch;
					mouse.main.map.startY = (e.getY()) / pixelsToInch;
				} else if (e.isShiftDown()) {
					double dX = e.getX() - mouse.main.leftPanelWidth - mouse.main.map.startX * pixelsToInch;
					double dY = e.getY() - mouse.main.map.startY * pixelsToInch;

					mouse.main.map.startDir = (int) (-Math.atan(dY / -dX) * 180 / Math.PI);
					if (dX >= 0)
						mouse.main.map.startDir += 180;

					mouse.main.map.startDir = (int)Math.round(mouse.main.map.startDir / 15.0) * 15;
				}
			}
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
