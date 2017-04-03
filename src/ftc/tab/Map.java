package ftc.tab;

import ftc.Main;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Map {
	private Main main;
	public int sliderTime = 0;
	public double startX = 84;
	public double startY = 144 - 3.5;
	public boolean drawCompass = false;
	public int startDir = 90;

	public Color pathColor = new Color(230, 230, 0);

	public Map(Main main) {
		this.main = main;
	}

	public void render(Graphics g) throws IOException {
		BufferedImage img = ImageIO.read(new File("res/image00.png"));

		int size = (main.frame.getWidth() - main.leftPanelWidth) > main.frame.getHeight() ?
				main.frame.getHeight() : (main.frame.getWidth() - main.leftPanelWidth);
		g.drawImage(img, main.leftPanelWidth, 0, size, size, null);

		g.drawRect(main.frame.getWidth() - 50, (int)(sliderTime / (main.maxTime - main.minTime) * (main.frame.getHeight())), 50, 50);
		g.drawRect(main.frame.getWidth() - 50, 0, 50, main.frame.getHeight());

		g.drawString(sliderTime+"", main.frame.getWidth() - 48, (int)(sliderTime / (main.maxTime - main.minTime) * (main.frame.getHeight()))+20);

		double countsPerInch = 270 / Math.PI;
		double pixelsToInch = size / 144.0;

		double dir = startDir;

		int leftWheelIndex = 2, rightWheelIndex = 5;

		double[] xpoints = new double[main.data.size()];
		double[] ypoints = new double[main.data.size()];

		xpoints[0] = startX * pixelsToInch;
		ypoints[0] = startY * pixelsToInch;

		drawCompass = (Math.sqrt(Math.pow(main.mouse.x - main.leftPanelWidth - xpoints[0], 2)
							   + Math.pow(main.mouse.y - ypoints[0], 2)) < 50) || main.mouse.x > main.frame.getWidth() - 50;

		double xPoint1 = 0;
		double yPoint1 = 0;
		double xPoint2 = 0;
		double yPoint2 = 0;

		boolean status = false;

		double dirDraw = 0;

		for (int i = 0; i < main.data.size()-1; i++) {

			dir = startDir + main.data.get(i).navXHeading;
			if  ((main.motorSpeeds[leftWheelIndex][i] > 0 && main.motorSpeeds[rightWheelIndex][i] > 0)
					|| (main.motorSpeeds[leftWheelIndex][i] < 0 && main.motorSpeeds[rightWheelIndex][i] < 0)) { // robot is moving forward
				double dist = pixelsToInch * (main.motorSpeeds[leftWheelIndex][i] + main.motorSpeeds[rightWheelIndex][i]) / 2 / countsPerInch * 12.4;
				xpoints[i+1] = xpoints[i] - Math.cos(dir * Math.PI / 180) * dist;
				ypoints[i+1] = ypoints[i] - Math.sin(dir * Math.PI / 180) * dist;

				if (xpoints[i+1] > pixelsToInch * 129.5)
					xpoints[i+1] = pixelsToInch * 129.5;
				if (xpoints[i+1] < 3.5)
					xpoints[i+1] = 3.5;
			} else {
				xpoints[i+1] = xpoints[i];
				ypoints[i+1] = ypoints[i];
			}

			if (!status && main.data.get(i+1).time - main.minTime > sliderTime) {
				xPoint1 = xpoints[i];
				yPoint1 = ypoints[i];
				xPoint2 = xpoints[i+1];
				yPoint2 = ypoints[i+1];
				dirDraw = dir;
				status = true;
			}
		}


		int[] xPoints = new int[main.data.size()];
		int[] yPoints = new int[main.data.size()];
		for (int i = 0; i < main.data.size(); i++) {
			xPoints[i] = (int) xpoints[i] + main.leftPanelWidth;
			yPoints[i] = (int) ypoints[i];
		}


		Graphics2D g2 = (Graphics2D) g;
		g2.setStroke(new BasicStroke(3));

		g.setColor(pathColor);
		g.drawPolyline(xPoints, yPoints, main.data.size());

		g2.setStroke(new BasicStroke(10));
		g.setColor(Color.WHITE);
		g.drawLine((int)((xPoint1+xPoint2)/2)+main.leftPanelWidth,(int)((yPoint1+yPoint2)/2),(int)((xPoint1+xPoint2)/2)+main.leftPanelWidth,(int)((yPoint1+yPoint2)/2));
		g2.setStroke(new BasicStroke());



		if (drawCompass) {
			double xPos = ((xPoint1+xPoint2)/2)+main.leftPanelWidth;
			double yPos = ((yPoint1+yPoint2)/2);

			g.drawOval((int) xpoints[0] + 50, (int) ypoints[0] - 50, 100, 100);
			g.drawLine((int)xpoints[0]+100, (int)ypoints[0],
					(int)(xpoints[0] - 50*Math.cos(startDir * Math.PI / 180))+100, (int)(ypoints[0] - 50*Math.sin(startDir * Math.PI / 180)));

			xPoints = new int[4];
			yPoints = new int[4];

			xPoints[0] = (int)(xPos + Math.cos((dirDraw) * Math.PI / 180 + Math.atan(8 / 3.5))*8.7321*pixelsToInch);
			yPoints[0] = (int)(yPos + Math.sin((dirDraw) * Math.PI / 180 + Math.atan(8 / 3.5))*8.7321*pixelsToInch);

			xPoints[1] = (int)(xPos + Math.cos((dirDraw) * Math.PI / 180 - Math.atan(8 / 3.5))*8.7321*pixelsToInch);
			yPoints[1] = (int)(yPos + Math.sin((dirDraw) * Math.PI / 180 - Math.atan(8 / 3.5))*8.7321*pixelsToInch);

			xPoints[2] = (int)(xPos - Math.cos((dirDraw) * Math.PI / 180 + Math.atan(8 / 14.5))*16.5605*pixelsToInch);
			yPoints[2] = (int)(yPos - Math.sin((dirDraw) * Math.PI / 180 + Math.atan(8 / 14.5))*16.5605*pixelsToInch);

			xPoints[3] = (int)(xPos - Math.cos((dirDraw) * Math.PI / 180 - Math.atan(8 / 14.5))*16.5605*pixelsToInch);
			yPoints[3] = (int)(yPos - Math.sin((dirDraw) * Math.PI / 180 - Math.atan(8 / 14.5))*16.5605*pixelsToInch);

			g.drawPolygon(xPoints, yPoints, 4);
		}
	}
}
