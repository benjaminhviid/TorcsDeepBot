package org.bj.deeplearning.dataobjects;

import org.bj.deeplearning.tools.ImageTool;
import org.bj.deeplearning.tools.PropertiesReader;
import org.bj.deeplearning.tools.Utils;

import static org.bj.deeplearning.tools.Utils.clamp;
import static org.bj.deeplearning.tools.Utils.map;


public class TrainingData {

	private int height, width, id;
	private byte[] pixelData;
	private double[] features;
	private double angle, speed, marking_L, marking_M, marking_R, dist_L, dist_R;
	static TrainingDataType type;

	// CONFIG 1: angle, speed, markingM, markingR
	// CONFIG 2: angle, speed, markingL, markingM, markingR, distL, distR

private int index = 0;


    public static int getFeatureCount() {
   		return TrainingDataHandler.getNumberOfGroundTruths();
	}

	public TrainingData(int id, TrainingDataType trainingDataType, RunType runType){
		int _id = id;
		if (_id == 0)
			_id = 1;

		String[] sample = TrainingDataHandler.getSample(_id, TrainingDataHandler.runType);
		TrainingData.type = trainingDataType;

        speed = Double.parseDouble(sample[index++]);
        speed = clamp(speed, 0, 200);
        speed = map(speed, 0.0, 200.0, 0.0, 1.0);
        
		angle = Double.parseDouble(sample[index++]);
		angle = clamp(angle, -Math.PI, Math.PI);
		angle = map(angle, -Math.PI, Math.PI, 0.0, 1.0);

		// when clamping trackpos we use -2, 2, as we give 2 x track width as track boundary in each side
		if (trainingDataType == TrainingDataType.MINIMAL || trainingDataType == TrainingDataType.EXTENSIVE){
			double trackPos = Double.parseDouble(sample[index++]);
			marking_M = map(clamp(trackPos, -2, 2), -2.0, 2.0, 0.0, 1.0);

			if (trainingDataType == TrainingDataType.EXTENSIVE) {
				marking_L = 0.9 - map(clamp(trackPos, -1.0, 1.0), -1.0, 1.0, 0.0, 1.0);
				marking_R = 0.9 - map(Utils.clamp(trackPos, -1, 1), -1.0, 1.0, 0.1, 0.9);
				marking_R = map(marking_R, 0.1, 0.9, 0.9, 0.1);
			}

		//
		}

		if (trainingDataType == TrainingDataType.SHALLOW) {
			marking_M = Double.parseDouble(sample[index++]);
			marking_M = clamp(marking_M, 0, 1.5);
			marking_M = map(marking_M, 0, 1.5, 0, 1);

			marking_R = Double.parseDouble(sample[index++]);
			marking_R = clamp(marking_R, 0, 1.5);
			marking_R = map(marking_R, 0, 1.5, 0, 1);
		}

		if (trainingDataType == TrainingDataType.EXTENSIVE) {
			dist_L = Double.parseDouble(sample[index++]);
			dist_R = Double.parseDouble(sample[index++]);

		}

		if (trainingDataType == TrainingDataType.SHALLOW) {
			height = (int) Double.parseDouble(sample[index++]);
			width = (int) Double.parseDouble(sample[index++]);
			this.id = (int) Double.parseDouble(sample[index++]);
		}
		else {
        	height = Integer.parseInt(PropertiesReader.getProjectProperties().getProperty("training.image.height"));
        	width =  Integer.parseInt(PropertiesReader.getProjectProperties().getProperty("training.image.width"));
        	this.id = Integer.parseInt(sample[index++]);

		}
		if(runType == RunType.TRAINING)
			pixelData = ImageTool.bufferedImageToByteArray(TrainingDataHandler.SCREENSHOTS_PATH + "screenshot" + id + ".jpg");
        else
			pixelData = ImageTool.bufferedImageToByteArray(TrainingDataHandler.TEST_SCREENSHOTS_PATH + "screenshot" + id + ".jpg");

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
		if (type == TrainingDataType.MINIMAL){
			double[] result = new double[2];
			result[0] = angle;
			result[1] = marking_M;
			//result[2] = marking_M;
			return result;
		}
		else if (type == TrainingDataType.SHALLOW ) {

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
