package ftc.mouse;

import ftc.GraphTool;
import ftc.enums.MenuState;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

public class MouseWheel implements MouseWheelListener{
	private Mouse mouse;
	private int totalScroll = 0;

	public MouseWheel(Mouse mouse) {
		this.mouse = mouse;
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if (mouse.main.menuState == MenuState.GRAPH && mouse.main.graphTool.currentTool == GraphTool.VIEW) {
			totalScroll -= e.getUnitsToScroll() / 3;
			mouse.main.tempZoom = totalScroll;
		}
		if (mouse.main.menuState == MenuState.OVERVIEW) {
			mouse.main.overviewYOffset -= e.getUnitsToScroll() / 3 * 5;
//			if (mouse.main.overviewYOffset < 0) mouse.main.overviewYOffset = 0;
		}
	}
}
