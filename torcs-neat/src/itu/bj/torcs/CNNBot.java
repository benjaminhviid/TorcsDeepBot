package itu.bj.torcs;

/**
 * Created by benjaminhviid on 5/2/17.
 */
public class CNNBot extends Controller {


    /* Gear Changing Constants*/
    final int[]  gearUp={5000,6000,6000,6500,7000,0};
    final int[]  gearDown={0,2500,3000,3000,3500,3500};

    /* Stuck constants*/
    final int  stuckTime = 25;
    final float  stuckAngle = (float) 0.523598775; //PI/6

    /* Accel and Brake Constants*/
    final float maxSpeedDist=70;
    final float maxSpeed=150;
    final float sin5 = (float) 0.08716;
    final float cos5 = (float) 0.99619;

    /* Steering constants*/
    final float steerLock=(float) 0.366519;
    final float steerSensitivityOffset=(float) 80.0;
    final float wheelSensitivityCoeff=1;

    /* ABS Filter Constants */
    final float wheelRadius[]={(float) 0.3306,(float) 0.3306,(float) 0.3276,(float) 0.3276};
    final float absSlip=(float) 2.0;
    final float absRange=(float) 3.0;
    final float absMinSpeed=(float) 3.0;

    /* Clutching Constants */
    final float clutchMax=(float) 0.5;
    final float clutchDelta=(float) 0.05;
    final float clutchRange=(float) 0.82;
    final float	clutchDeltaTime=(float) 0.02;
    final float clutchDeltaRaced=10;
    final float clutchDec=(float) 0.01;
    final float clutchMaxModifier=(float) 1.3;
    final float clutchMaxTime=(float) 1.5;

    private int stuck=0;

    // current clutch
    private float clutch=0;

    private double targetSpeed = 0;

    private int getGear(SensorModel sensors){
        int gear = sensors.getGear();
        double rpm  = sensors.getRPM();

        // if gear is 0 (N) or -1 (R) just return 1
        if (gear < 1)
            return 1;
        // check if the RPM value of car is greater than the one suggested
        // to shift up the gear from the current one
        if (gear <6 && rpm >= gearUp[gear-1])
            return gear + 1;
        else
            // check if the RPM value of car is lower than the one suggested
            // to shift down the gear from the current one
            if (gear > 1 && rpm <= gearDown[gear-1])
                return gear - 1;
            else // otherwhise keep current gear
                return gear;
    }

    private float getSteer(CNNSensorModel sensors){
        // steering angle is compute by correcting the actual car angle w.r.t. to track
        // axis [sensors.getAngle()] and to adjust car position w.r.t to middle of track [sensors.getTrackPos()*0.5]
        float targetAngle=(float) (sensors.getAngleToTrackAxis()-sensors.getTrackPosition());
        // at high speed reduce the steering command to avoid loosing the control
        if (sensors.getSpeed() > steerSensitivityOffset)
            return (float) (targetAngle/(steerLock*(sensors.getSpeed()-steerSensitivityOffset)*wheelSensitivityCoeff));
        else
            return (targetAngle)/steerLock;

    }


    @Override
    public Action control(SensorModel sensors) {

        Action action = new Action ();
        if (sensors.getSpeed() < CNNSensorModel.getInstance().getSpeed ()) {
            action.accelerate = 1;
            action.brake = 0;
        }
        else{
            action.brake = 0.8;
            action.accelerate = 0;
        }

        action.steering = getSteer(CNNSensorModel.getInstance());

        action.gear = getGear(sensors);
        return action;
    }

    public void reset() {
        System.out.println("Restarting the race!");

    }

      public void shutdown() {
        System.out.println("Bye bye!");
    }
}
