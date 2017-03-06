package ftc.mouse;

import ftc.GraphTool;
import ftc.Main;
import ftc.enums.Button;
import ftc.enums.GraphMenuButton;
import ftc.enums.GraphState;
import ftc.enums.MenuState;
import org.jetbrains.annotations.Nullable;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.FileNotFoundException;

public class MouseEvents implements MouseListener {
	private Mouse mouse;

	public MouseEvents(Mouse mouse) {
		this.mouse = mouse;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		Button clicked = whichButton(e.getX(), e.getY());
		if (clicked != null) {
			mouse.main.menuState = clicked.menuState;
		}
		if (mouse.main.menuState == MenuState.MAIN_MENU) {
			if (e.getX() - Main.leftPanelWidth > 0 && e.getX() - Main.leftPanelWidth < 100 && e.getY() < mouse.main.listOfFiles.length * 20) {
				try {
					mouse.main.readFile(mouse.main.listOfFiles[e.getY() / 20]);
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				}
			}
		}
		if (mouse.main.menuState == MenuState.GRAPH) {
			GraphTool tool = mouse.main.graphTool;
			GraphMenuButton graphClicked = whichGraphButton(e.getX(), e.getY());
			if (graphClicked != null) { // The user clicked on a graph menu button
				switch (graphClicked) {
					case Measure_Time:
						tool.currentTool = GraphTool.MEASURE_TIME_START;
						break;
					case Slope_Calculator:
						tool.currentTool = GraphTool.SLOPE_CALC_START;
						break;
					case Encoder_Position:
						mouse.main.graphState = GraphState.Time_Enc;
						break;
					case Speed:
						mouse.main.graphState = GraphState.Time_Speed;
						break;
				}
			} else if (whichButton(e.getX(), e.getY()) == null) { // the user clicked inside the main graphing window
				switch (tool.currentTool) {
					case GraphTool.MEASURE_TIME_START:
						tool.startMeasureTime(e.getX());
						break;
					case GraphTool.MEASURE_TIME_END:
						tool.endMeasureTime(e.getX());
						break;
					case GraphTool.SLOPE_CALC_START:
						tool.startSlopeCalc(e.getX(), e.getY());
						break;
					case GraphTool.SLOPE_CALC_END:
						tool.endSlopeCalc(e.getX(), e.getY());
						break;
				}
			}
		}
	}

	@Nullable
	private Button whichButton(int x, int y) {
		for (Button button : Button.values()) {
			if ((x > button.x && x < button.x + button.width)
				&& (y > button.y && y < button.y + button.height))
				return button;
		}

		return null;
	}

	@Nullable
	private GraphMenuButton whichGraphButton(int x, int y) {
		for (GraphMenuButton button: GraphMenuButton.values()) {
			if (x > button.x + Main.leftPanelWidth && x < button.x + Main.leftPanelWidth + button.width
					&& y > button.y + mouse.main.frame.getHeight() - Main.bottomPanelHeight && y < button.y + mouse.main.frame.getHeight() - Main.bottomPanelHeight + button.height) {
				return  button;
			}
		}
		return null;
	}

	@Override
	public void mousePressed(MouseEvent e) {
		mouse.dragging = true;
		if (e.isShiftDown())
			mouse.main.zoom += 1;
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		mouse.dragging = false;
		if (e.isShiftDown())
			mouse.main.zoom -= 1;
	}

	@Override
	public void mouseEntered(MouseEvent e) {

	}

	@Override
	public void mouseExited(MouseEvent e) {

	}
}
