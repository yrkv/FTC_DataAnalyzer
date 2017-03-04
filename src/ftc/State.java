package ftc;

import java.util.StringTokenizer;

public class State {
	public double time;
	public double voltage;
	public int[] motorPositions;
	public double[] motorPowers;
	public int[] colorSensorValues;
	public boolean navXConnected;
	public double navXHeading;


	public State(String l1, String l2, String l3, String l4) {
		StringTokenizer st = new StringTokenizer(l1);
		time = Double.parseDouble(st.nextToken());
		voltage = Double.parseDouble(st.nextToken());

		String[] motorData = l2.split(" ");
		motorPositions = new int[motorData.length / 2];
		motorPowers = new double[motorData.length / 2];
		for (int i = 0; i < motorData.length / 2; i++) {
			motorPositions[i] = Integer.parseInt(motorData[i*2]);
			motorPowers[i] = Double.parseDouble(motorData[i*2 + 1]);
		}

		String[] colorSensorData = l3.split(" ");
		colorSensorValues = new int[colorSensorData.length];
		for (int i = 0; i < colorSensorData.length; i++)
			colorSensorValues[i] = Integer.parseInt(colorSensorData[i]);

		st = new StringTokenizer(l4);
		navXConnected = Boolean.parseBoolean(st.nextToken());
		navXHeading = Double.parseDouble(st.nextToken());
	}
}
