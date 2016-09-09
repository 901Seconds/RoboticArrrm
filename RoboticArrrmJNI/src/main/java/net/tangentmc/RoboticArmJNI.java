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
   /*public static void main(String[] args) {
        UI.initialise();

        RoboticArmJNI arm = new RoboticArmJNI(287,374,377,374,154);
        UI.addSlider("Servo 1",ARM_1_MIN,ARM_1_MAX,d->{arm.setServo(0,d);UI.println("Servo 1 pulse: "+d);});
        UI.addSlider("Servo 2",ARM_2_MIN,ARM_2_MAX,d->{arm.setServo(1,d);UI.println("Servo 2 pulse: "+d);});
        UI.addSlider("Servo 1 Angle",-180,0,d->arm.setAngle(d,arm.lastTheta2));
        UI.addSlider("Servo 2 Angle",-180,0,d->arm.setAngle(arm.lastTheta1,d));
        UI.addButton("Read Theta 1",()->UI.println("Theta 1: "+arm.readAngle(0)));
        UI.addButton("Read Theta 2",()->UI.println("Theta 2: "+arm.readAngle(1)));
        UI.addButton("Pen Down",()->arm.setPenMode(true));
        UI.addButton("Pen Up",()->arm.setPenMode(false));
        arm.init();
        arm.calibrate();
    }*/
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
        init();
        calibrate();
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
        mArm1 = ((ARM_1_MAX-ARM_1_MIN)/(arm1MaxAngle-arm1MinAngle));
        mArm2 = ((ARM_2_MAX-ARM_2_MIN)/(arm2MaxAngle-arm2MinAngle));
        cArm1 = ARM_1_MIN - mArm1*arm1MinAngle;
        cArm2 = ARM_2_MIN - mArm2*arm2MinAngle;
    }
    double lastTheta1 = -90;
    double lastTheta2 = -90;
    double mArm1,mArm2,cArm1,cArm2;
    @Override
    public void setAngle(double theta1, double theta2) {
        lastTheta1 = theta1;
        lastTheta2 = theta2;
        theta1 = -Math.toDegrees(theta1);
        theta2 = -Math.toDegrees(theta2);

        double pulse1 = mArm1*theta1+cArm1;
        double pulse2 = mArm2*theta2+cArm2;
        setServo(0,Math.max(Math.min(ARM_1_MAX,pulse1),ARM_1_MIN));
        setServo(1,Math.max(Math.min(ARM_2_MAX,pulse2),ARM_2_MIN));
        UI.println("Set angles to: "+theta1+","+theta2);
        UI.println("Measured: "+readAngle(0)+","+readAngle(1));

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
