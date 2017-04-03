package ftc.tab;

import ftc.Main;

import java.awt.*;

public class MainMenu {
	private Main main;
	public String currentFile = "";

	public MainMenu(Main main) {
		this.main = main;
	}

	public void render(Graphics g) {
		for (int i = 0; i < main.listOfFiles.length; i++) {

			String name = main.listOfFiles[i].getName();
			if (currentFile.equals(name))
				g.setColor(Color.LIGHT_GRAY);
			else
				g.setColor(Color.WHITE);
			g.fillRect(main.leftPanelWidth, 20*i, 100, 20);
			g.setColor(Color.BLACK);
			g.drawRect(main.leftPanelWidth, 20*i, 100, 20);
			g.drawString(name, main.leftPanelWidth + 5, 20*i + 15);
		}
	}
}
