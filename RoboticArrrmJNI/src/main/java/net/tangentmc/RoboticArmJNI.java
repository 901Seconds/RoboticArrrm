package net.tangentmc;

import ecs100.UI;
import org.scijava.nativelib.NativeLoader;

import java.io.IOException;

import static net.tangentmc.Utils.absLength;


//TODO: create a model that represents the robot
public class RoboticArmJNI implements RoboticArm {
    private static final int ARM_1_MIN = 1500;
    private static final int ARM_1_MAX = 2000;
    private static final int ARM_2_MIN = 900;
    private static final int ARM_2_MAX = 1400;
    double arm1MinAngle, arm1MaxAngle,arm2MinAngle, arm2MaxAngle;
    public static void main(String[] args) {
        UI.initialise();
        RoboticArmJNI arm = new RoboticArmJNI(100,100,100,100,100);
        UI.addSlider("Servo 1",ARM_1_MIN,ARM_1_MAX,d->{arm.setServo(0,d);UI.println("Servo 1 pulse: "+d);});
        UI.addSlider("Servo 2",ARM_2_MIN,ARM_2_MAX,d->{arm.setServo(1,d);UI.println("Servo 2 pulse: "+d);});
        UI.addSlider("Servo 1 Angle",0,360,d->arm.setAngle(d,arm.lastTheta2));
        UI.addSlider("Servo 2 Angle",0,360,d->arm.setAngle(arm.lastTheta1,d));
        UI.addButton("Read Theta 1",()->UI.println("Theta 1: "+arm.readAngle(0)));
        UI.addButton("Read Theta 2",()->UI.println("Theta 2: "+arm.readAngle(1)));
        UI.addButton("Pen Down",()->arm.setPenMode(true));
        UI.addButton("Pen Up",()->arm.setPenMode(false));
        arm.init();
        arm.calibrate();
    }
    static {
        try {
            NativeLoader.loadLibrary("RoboticArrrmJNI-1.0-SNAPSHOT");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    double o1X, o1Y, o2X, o2Y;
    double d, l;
    RoboticArmModel theModel;
    public RoboticArmJNI(double shoulder1X, double shoulder1Y, double shoulder2X, double shoulder2Y, double appendageLength) {
        o1X=shoulder1X;
        o1Y=shoulder1Y;
        o2X=shoulder2X;
        o2Y=shoulder2Y;
        d=absLength(o1X,o2X,o1Y,o2Y);
        l=appendageLength;
        theModel = new RoboticArmModel(o1X,o1Y,o2X,o2Y,l);
    }
    public native void init();
    public native double readAngle(int servo);
    public native void setServo(int servo, double pulse);
    public void calibrate() {
        setServo(0,ARM_1_MIN);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        arm1MinAngle = readAngle(0);
        setServo(0,ARM_1_MAX);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        arm1MaxAngle = readAngle(0);
        setServo(1,ARM_2_MIN);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        arm2MinAngle = readAngle(1);
        setServo(1,ARM_2_MAX);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        arm2MaxAngle = readAngle(1);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    double lastTheta1 = 0;
    double lastTheta2 = 0;
    @Override
    public void setAngle(double theta1, double theta2) {
        lastTheta1 = theta1;
        lastTheta2 = theta2;
        double tOut1 = (ARM_1_MAX-ARM_1_MIN)*((theta1-arm1MinAngle)/(arm1MaxAngle-arm1MinAngle));
        double tOut2 = (ARM_2_MAX-ARM_2_MIN)*((theta2-arm2MinAngle)/(arm2MaxAngle-arm2MinAngle));
        setServo(0,tOut1);
        setServo(1,tOut2);
        System.out.println("Set angles to: "+theta1+","+theta2);
    }

    @Override
    public RoboticArmModel getModel() {
        return null;
    }

    @Override
    public void setPenMode(boolean down) {
        setServo(2,down?2000:1000);
    }
}
