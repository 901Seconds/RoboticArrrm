package net.tangentmc;

import ecs100.UI;
import org.scijava.nativelib.NativeLoader;

import java.io.IOException;

import static net.tangentmc.Utils.absLength;


//TODO: create a model that represents the robot
public class RoboticArmJNI implements RoboticArm {
    public static void main(String[] args) {
        UI.initialise();
        RoboticArmJNI arm = new RoboticArmJNI(100,100,100,100,100);
        UI.addSlider("Servo 1",800,1100,d->{arm.setServo(0,d);UI.println("Servo 1 pulse: "+d);});
        UI.addSlider("Servo 2",800,1100,d->{arm.setServo(1,d);UI.println("Servo 2 pulse: "+d);});
        UI.addButton("Read Theta 1",()->UI.println("Theta 1: "+arm.readAngle(0)));
        UI.addButton("Read Theta 2",()->UI.println("Theta 2: "+arm.readAngle(1)));
        UI.addButton("Pen Down",()->arm.setPenMode(true));
        UI.addButton("Pen Up",()->arm.setPenMode(false));
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
        init();
        calibrate();
    }
    public native void init();
    public native double readAngle(int servo);
    public native void setServo(int servo, double pulse);
    public void calibrate() {
        /*
        for (int mt = 0; mt < 2; mt++) {
            setServo(mt, 1000);
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            double last =readAngle(mt);
            UI.println("MOTOR: "+mt);
            for (int i = 100; i < 3000; i += 50) {
                setServo(mt, i);
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                UI.println(readAngle(mt));

            }
        }
        */
    }
    @Override
    public void setAngle(double theta1, double theta2) {
        int leftPulse = (int)(500*theta1) + 500;
        int rightPulse = (int)(500*theta2) + 500;
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
