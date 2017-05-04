package itu.bj.torcs;

import static org.bj.deeplearning.tools.Utils.clamp;
import static org.bj.deeplearning.tools.Utils.map;

/**
 * Created by benjaminhviid on 5/2/17.
 */
public class CNNSensorModel {

    private static CNNSensorModel instance = null;
    private CNNSensorModel() {
        // Exists only to defeat instantiation.
    }

    public static CNNSensorModel getInstance() {
        if(instance == null) {
            instance = new CNNSensorModel();
        }
        return instance;
    }

    private double speed = 0.0;
    private double angleToTrackAxis = 0.0;
    private double trackPosition = 0.0;

    public void setValues(double _speed, double _angleToTrackAxis, double _trackPos){
        this.speed = revertNormalizedSpeed(_speed);
        this.angleToTrackAxis = revertNormalizedAngleToTrackAxis(_angleToTrackAxis);
        this.trackPosition = revertNormalizedTrackPosition(_trackPos);
    }

    public double getAngleToTrackAxis(){
        return angleToTrackAxis;
    }

    public double getSpeed(){
        return speed;
    }

    public double getTrackPosition(){
        return trackPosition;
    }

    private double revertNormalizedSpeed(double speed){
        return map(speed, 0.1, 0.9, 0.0, 200.0);

    }

    private double revertNormalizedAngleToTrackAxis(double angleToTrackAxis){
        return map(angleToTrackAxis,0.1, 0.9,  -Math.PI, Math.PI);

    }

    private double revertNormalizedTrackPosition(double trackPosition){
        return map(trackPosition, 0.1, 0.9, -1.0, 1.0);

    }



}
