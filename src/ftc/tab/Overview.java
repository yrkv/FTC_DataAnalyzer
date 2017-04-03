package ftc.tab;

import ftc.Main;

import java.awt.*;

public class Overview {
	private Main main;
	public double initVoltage;
	public int yOffset = 0;

	public Overview(Main main) {
		this.main = main;
	}

	public void render(Graphics g) {
		g.drawString("Initial Voltage: " + initVoltage, main.leftPanelWidth + 5, 15 + yOffset);
		g.drawRect(main.leftPanelWidth, yOffset, 120, 20);
		int height = 20;

		for (int i = 0; i < main.pathReconstruction.size(); i++) {
			String[] splitString = main.pathReconstruction.get(i).split(" ");
			String string = splitString[0] + " " + splitString[1];
			if (splitString.length == 4) {
				string += " " + splitString[2];
			}
			g.drawString(string, main.leftPanelWidth + 5, 20 * i + 35 + yOffset);
			g.drawString(splitString[splitString.length-1], main.leftPanelWidth + 125,
					20 * i + 35 + yOffset);
			height += 20;
			g.drawRect(main.leftPanelWidth, 20*i+20+yOffset, 120, 20);
		}


		if (yOffset + height < main.frame.getHeight()) {
			yOffset = main.frame.getHeight() - height;
		}
		if (yOffset > 0) yOffset = 0;

		g.drawRect(main.frame.getWidth() - 20, (int)(((double)main.frame.getHeight()-100)
				*(yOffset/(main.frame.getHeight()-height))), 20, 100);
	}
}
