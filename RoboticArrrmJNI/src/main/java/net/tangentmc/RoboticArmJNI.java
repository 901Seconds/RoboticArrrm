package net.tangentmc;

import ecs100.UI;
import ecs100.Trace;

import java.io.*;
import java.util.Arrays;
import java.util.Scanner;

import static net.tangentmc.Utils.absLength;


//TODO: create a model that represents the robot
public class RoboticArmJNI implements RoboticArm {
    private static final int ARM_1_MIN = 1500;
    private static final int ARM_1_MAX = 2000;
    private static final int ARM_2_MIN = 1000;
    private static final int ARM_2_MAX = 1400;
    BufferedReader in;
    BufferedWriter out;

    double arm1MinAngle, arm1MaxAngle,arm2MinAngle, arm2MaxAngle;

    double o1X, o1Y, o2X, o2Y;
    double d, l;
    RoboticArmModel theModel;
    Process process;
    public RoboticArmJNI(double shoulder1X, double shoulder1Y, double shoulder2X, double shoulder2Y, double appendageLength) {

        o1X=shoulder1X;
        o1Y=shoulder1Y;
        o2X=shoulder2X;
        o2Y=shoulder2Y;
        d=absLength(o1X,o2X,o1Y,o2Y);
        l=appendageLength;
        theModel = new RoboticArmModel(o1X,o1Y,o2X,o2Y,l);
    }
    public double readAngle(int servo) {
        if (process == null) return -1;
        try {
            out.write("m");
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Trace.print("Attempting read angle.");
        return -1;
    }
    int[] lastPoints = new int[]{1500,1500,1500};
    public void setServo(int servo, int pulse) {
        if (process == null) return;
        try {
            Thread.sleep(100);
            lastPoints[servo] = pulse;
            out.write("s\n");
            out.write(lastPoints[0]+"\n");
            out.write(lastPoints[1]+"\n");
            out.write(lastPoints[2]+"\n");
            out.flush();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
        Trace.print("Attempting set servo pulse.");

    }
    public void calibrate() {
        setServo(0, ARM_1_MIN);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        arm1MinAngle = readAngle(0);

        setServo(0, ARM_1_MAX);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        arm1MaxAngle = readAngle(0);
        setServo(1, ARM_2_MIN);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        arm2MinAngle = readAngle(1);
        setServo(1, ARM_2_MAX);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        arm2MaxAngle = readAngle(1);
        mArm1 = ((ARM_1_MAX - ARM_1_MIN) / (arm1MaxAngle - arm1MinAngle));
        mArm2 = ((ARM_2_MAX - ARM_2_MIN) / (arm2MaxAngle - arm2MinAngle));
        cArm1 = ARM_1_MIN - mArm1 * arm1MinAngle;
        cArm2 = ARM_2_MIN - mArm2 * arm2MinAngle;
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

        int pulse1 = (int) (mArm1*theta1+cArm1);
        int pulse2 = (int) (mArm2*theta2+cArm2);
        setServo(0, Math.max(Math.min(ARM_1_MAX,pulse1),ARM_1_MIN));
        setServo(1, Math.max(Math.min(ARM_2_MAX,pulse2),ARM_2_MIN));
        UI.println("Set angles to: "+theta1+","+theta2);
        UI.println("Measured: "+readAngle(0)+","+readAngle(1));
    }

    @Override
    public RoboticArmModel getModel() {
        return theModel;
    }

    @Override
    public void setPenMode(boolean down) {
        setServo(2,down?2000:1000);
    }

    public void init() throws Exception{
        Trace.setVisible(true);
        ProcessBuilder builder = new ProcessBuilder("sudo","/home/pi/Arm/arm2");
        process = builder.start();
        out = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
        in = new BufferedReader(new InputStreamReader(process.getInputStream()));
        calibrate();
        new Thread(()->{
            try {
                while (true) {
                    String next = in.readLine();
                    if (next == null) continue;
                    Trace.println(next);
                    if (next.startsWith("p1")) {
                        Trace.println("FUCK YES"+next);
                        //return;
                    }
                    if (next.startsWith("measured")) {
                        String[] args = next.replace("measured angles: ","").split(" ");
                        Trace.println(Arrays.toString(Arrays.stream(args).map(s2 -> s2.split("=")[1]).toArray()));
                        //return Double.parseDouble(args[servo].split("=")[1]);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
