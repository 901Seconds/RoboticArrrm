package net.tangentmc;

import static net.tangentmc.Utils.*;


//TODO: create a model that represents the robot
public class RoboticArmJNI implements RoboticArm {

    private final int LEFT_SERVO_PIN_NUMBER = 4;
    private final int RIGHT_SERVO_PIN_NUMBER = 17;

    double o1X, o1Y, o2X, o2Y;
    double d, l;

    RoboticArmModel theModel;
    static {
        System.load("/home/kingciar1/Projects/RoboticArrrm/RoboticArrrmJNI/target/classes/roboticarmlib.so");
    }
    public RoboticArmJNI(double shoulder1X, double shoulder1Y, double shoulder2X, double shoulder2Y, double appendageLength) {
        o1X=shoulder1X;
        o1Y=shoulder1Y;
        o2X=shoulder2X;
        o2Y=shoulder2Y;
        d=absLength(o1X,o2X,o1Y,o2Y);
        l=appendageLength;
        theModel = new RoboticArmModel(o1X,o1Y,o2X,o2Y,l);
        init();
    }

    @Override
    public void setAngle(double theta1, double theta2) {
        int leftPulse = (int)(500*theta1) + 500;
        int rightPulse = (int)(500*theta2) + 500;
        setServo(LEFT_SERVO_PIN_NUMBER, leftPulse);
        setServo(RIGHT_SERVO_PIN_NUMBER, rightPulse);
    }

    private native void setServo(int pin, int pulseWidth);

    private native void init();

    @Override
    public RoboticArmModel getModel() {
        return null;
    }

    @Override
    public void setPenMode(boolean down) {

    }
}
