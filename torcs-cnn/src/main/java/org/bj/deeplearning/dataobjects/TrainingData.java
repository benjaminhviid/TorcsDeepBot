package org.bj.deeplearning.dataobjects;

import org.bj.deeplearning.tools.ImageTool;

public class TrainingData {

	private int height, width, id;
	private byte[] pixelData;
	private double[] features;
	private double angle, speed, dist_RL, dist_RR;

	private int angle_index         = 0;
	private int speed_index         = 1;
	private int dist_RL_index       = 2;
    private int dist_RR_index       = 3;
    private int height_index        = 4;
    private int width_index         = 5;
    private int id_index            = 6;


    public static int getFeatureCount() {
		return TrainingDataHandler.getNumberOfGroundTruths();
	}

	public TrainingData(int id){

        String[] sample = TrainingDataHandler.getSample(id+1); // plus one to avoid header

		angle = Double.parseDouble(sample[angle_index]);
		angle = clamp(angle, -1.0, 1.0);

        speed = Double.parseDouble(sample[speed_index]);
        speed = clamp(speed, 0, 80);
        speed = map(speed, 0, 80, 0, 1);

        dist_RL = Double.parseDouble(sample[dist_RL_index]);
		dist_RL = clamp(dist_RL, 0, 1.5);
		dist_RL = map(dist_RL, 0, 1.5, 0, 1);

		dist_RR = Double.parseDouble(sample[dist_RR_index]);
		dist_RR = clamp(dist_RR, 0, 1.5);
		dist_RR = map(dist_RR, 0, 1.5, 0, 1);

        height = (int)Double.parseDouble(sample[height_index]);
        width = (int)Double.parseDouble(sample[width_index]);
        id = (int)Double.parseDouble(sample[id_index]);
		pixelData = ImageTool.bufferedImageToByteArray(TrainingDataHandler.SCREENSHOTS_PATH + "screenshot" + id + ".jpg");

        features = calculateFeatures();

    }


	public TrainingData(double angle, double speed, double dist_RL, double dist_RR, int height, int width, int id){

		this.angle = angle;
		this.angle = clamp(angle, -1.0, 1.0);

		this.speed = speed;
		this.speed = clamp(speed, 0, 80);
		this.speed = map(speed, 0, 80, 0, 1);

		this.dist_RL = dist_RL;
		this.dist_RL = clamp(dist_RL, 0, 1.5);
		this.dist_RL = map(dist_RL, 0, 1.5, 0, 1);
		this.dist_RR = dist_RR;
		this.dist_RR = clamp(dist_RR, 0, 1.5);
		this.dist_RR = map(dist_RR, 0, 1.5, 0, 1);
       	this.height = height;
       	this.width = width;
        this.id = id;
		this.pixelData = ImageTool.bufferedImageToByteArray(TrainingDataHandler.SCREENSHOTS_PATH + "screenshot" + id + ".jpg");

		features = calculateFeatures();

	}

	public TrainingData(double angle, double speed, double dist_RL, double dist_RR, int height, int width, int id, byte[] pixelData) {

		this.angle = angle;
		this.speed = speed;
		this.dist_RL = dist_RL;
		this.dist_RR = dist_RR;
		this.height = height;
		this.width = width;
		this.id = id;
		this.pixelData = pixelData;

		features = calculateFeatures();

	}

		private double[] calculateFeatures() {

		double[] result = new double[4];

		result[0] = angle;
		result[1] = speed;
		result[2] = dist_RL;
		result[3] = dist_RR;

		return result;
	}

	public double[] getFeatures() {
		return features;
	}

	public int getHeight() {
		return height;
	}

	public int getWidth() {
		return width;
	}

	public byte[] getPixelData() {
		return pixelData;
	}

	public int getId() {
		return id;
	}

	public double getAngle() {
		return angle;
	}

	public double getSpeed() {
		return speed;
	}

	public double getDist_RL() {
		return dist_RL;
	}

	public double getDist_RR() {
		return dist_RR;
	}

	double map(double s, double a1, double a2, double b1, double b2)
	{
		return b1 + (s-a1)*(b2-b1)/(a2-a1);
	}

	double clamp (double value, double min, double max){
		return Math.max(min, Math.min(max, value));
	}
}
