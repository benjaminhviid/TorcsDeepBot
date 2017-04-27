package org.bj.deeplearning.dataobjects;

import org.bj.deeplearning.tools.ImageTool;
import org.bj.deeplearning.tools.Utils;

import static org.bj.deeplearning.tools.Utils.clamp;
import static org.bj.deeplearning.tools.Utils.map;

enum TrainingDataType {MINIMAL, SHALLOW, EXTENSIVE;}

public class TrainingData {

	private int height, width, id;
	private byte[] pixelData;
	private double[] features;
	private double angle, speed, marking_L, marking_M, marking_R, dist_L, dist_R;
	TrainingDataType type;

	// CONFIG 1: angle, speed, markingM, markingR
	// CONFIG 2: angle, speed, markingL, markingM, markingR, distL, distR

private int index = 0;


    public static int getFeatureCount() {
		return TrainingDataHandler.getNumberOfGroundTruths();
	}

	public TrainingData(int id, TrainingDataType type){

        String[] sample = TrainingDataHandler.getSample(id); // plus one to avoid header
		this.type = type;
		angle = Double.parseDouble(sample[index++]);
		angle = clamp(angle, -Math.PI, Math.PI);
		angle = map(speed, -Math.PI, Math.PI, 0.1, 0.9);

        speed = Double.parseDouble(sample[index++]);
        speed = clamp(speed, 0, 200);
        speed = map(speed, 0, 200, 0.1, 0.9);


		if (type == TrainingDataType.MINIMAL || type == TrainingDataType.EXTENSIVE){
			double trackPos = Double.parseDouble(sample[index++]);

			if (type == TrainingDataType.EXTENSIVE) {
				marking_L = 0.9 - map(clamp(trackPos, -1, 1), -1.0, 1.0, 0.1, 0.9);
			}

			marking_M = map(clamp(trackPos, -1, 1), -1.0, 1.0, 0.1, 0.9);
			marking_R = 0.9 - map(Utils.clamp(trackPos, -1, 1), -1.0, 1.0, 0.1, 0.9);
		    marking_R = map(marking_R, 0.1, 0.9, 0.9, 0.1);
		}

		if (type == TrainingDataType.SHALLOW) {
			marking_M = Double.parseDouble(sample[index++]);
			marking_M = clamp(marking_M, 0, 1.5);
			marking_M = map(marking_M, 0, 1.5, 0, 1);

			marking_R = Double.parseDouble(sample[index++]);
			marking_R = clamp(marking_R, 0, 1.5);
			marking_R = map(marking_R, 0, 1.5, 0, 1);
		}

		if (type == TrainingDataType.EXTENSIVE) {
			dist_L = Double.parseDouble(sample[index++]);
			dist_R = Double.parseDouble(sample[index++]);

		}

		if (type == TrainingDataType.SHALLOW) {
			height = (int) Double.parseDouble(sample[index++]);
			width = (int) Double.parseDouble(sample[index++]);
			id = (int) Double.parseDouble(sample[index++]);
		}
		else {
        	height = 210; // TODO: get from properties file instead
        	width = 280;
        	this.id = id;

		}
		pixelData = ImageTool.bufferedImageToByteArray(TrainingDataHandler.SCREENSHOTS_PATH + "screenshot" + id + ".jpg");

        features = calculateFeatures();
    }

	// used in FileSystem
	@Deprecated
	public TrainingData(double angle, double speed, double marking_M, double marking_R, int height, int width, int id, byte[] pixelData) {

		this.angle = angle;
		this.speed = speed;
		this.marking_M = marking_M;
		this.marking_R = marking_R;
		this.height = height;
		this.width = width;
		this.id = id;
		this.pixelData = pixelData;
		features = calculateFeatures();
	}

	private double[] calculateFeatures() {

		if (type == TrainingDataType.SHALLOW || type == TrainingDataType.MINIMAL) {

			double[] result = new double[4];
			result[0] = angle;
			result[1] = speed;
			result[2] = marking_M;
			result[3] = marking_R;
			return result;
		}
		else {
			double[] result = new double[7];
			result[0] = angle;
			result[1] = speed;
			result[2] = marking_L;
			result[3] = marking_M;
			result[4] = marking_R;
			result[5] = dist_L;
			result[6] = dist_R;
			return result;

		}

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

	public double getMarking_L() {
		return marking_L;
	}

	public double getMarking_M() {
		return marking_M;
	}

	public double getMarking_R() {
		return marking_R;
	}

	public double getDist_L() {
		return dist_L;
	}

	public double getDist_R() {
		return dist_R;
	}


	public TrainingDataType getType(){ return type; }

}
