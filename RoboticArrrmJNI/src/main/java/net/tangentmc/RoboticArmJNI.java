package net.tangentmc;

import ecs100.Trace;
import ecs100.UI;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Scanner;

public class RoboticArmJNI implements RoboticArm {
    private static final int ARM_1_MIN = 1500;
    private static final int ARM_1_MAX = 2000;
    private static final int ARM_2_MIN = 1000;
    private static final int ARM_2_MAX = 1500;
    private double mArm1,mArm2,cArm1,cArm2;
    private RoboticArmModel theModel;

    //IO from the running arm2 process
    private InputStream in;
    private PrintStream out;
    private Process process;

    public RoboticArmJNI(RoboticArmModel model) {
        theModel = model;
    }
    private double readAngle(int servo) {
        if (process == null) return -1;
        out.println(MEASURE_ANGLE_COMMAND);
        out.flush();
        try {
            while (in.available() > 0) {
                Scanner s = new Scanner(in);
                while (s.hasNextLine()) {
                    String next = s.nextLine();
                    if (next.startsWith("measured")) {
                        //The angles are separated by space
                        String[] args = next.replace("measured angles: ","").split(" ");
                        //The doubles are stored as theta=double so we want whats after the equals sign
                        Trace.println(Arrays.toString(Arrays.stream(args).map(s2 -> s2.split("=")[1]).toArray()));
                        return Double.parseDouble(args[servo].split("=")[1]);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }
    void setServo(int servo, int pulse) {
        if (process == null) return;
        out.println(SET_MOTOR_COMMAND);
        out.println(servo);
        out.println(pulse);
        out.flush();
        try {
            while (in.available() > 0) {
                Scanner s = new Scanner(in);
                while (s.hasNextLine()) {
                    String next = s.nextLine();
                    if (next.startsWith(MOTOR_PREFIX)) {
                        Trace.println(next);
                        return;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    private void calibrate() {
        Trace.println("Initialization:");
        //Wait for arm2 to start
        UI.sleep(5000);
        //Read a few angles as the first few inputs get eaten by arm2
        readAngle(0);
        readAngle(0);

        Trace.println("Starting Calibration:");
        Trace.println("Arm 1 Min:");
        setServo(0, ARM_1_MIN);
        UI.sleep(3000);
        double arm1MinAngle = readAngle(0);

        Trace.println("Arm 1 Max:");
        setServo(0, ARM_1_MAX);
        UI.sleep(3000);
        double arm1MaxAngle = readAngle(0);


        Trace.println("Arm 2 Min:");
        setServo(1, ARM_2_MIN);
        UI.sleep(3000);
        double arm2MinAngle = readAngle(1);

        Trace.println("Arm 2 Max:");
        setServo(1, ARM_2_MAX);
        UI.sleep(3000);
        double arm2MaxAngle = readAngle(1);

        Trace.println("Calculating Constants:");
        mArm1 = ((ARM_1_MAX - ARM_1_MIN) / (arm1MaxAngle - arm1MinAngle));
        mArm2 = ((ARM_2_MAX - ARM_2_MIN) / (arm2MaxAngle - arm2MinAngle));
        cArm1 = ARM_1_MIN - (mArm1 * arm1MinAngle);
        cArm2 = ARM_2_MIN - (mArm2 * arm2MinAngle);

    }
    @Override
    public void setAngle(double theta1, double theta2) {
        theta1 = -Math.toDegrees(theta1);
        theta2 = -Math.toDegrees(theta2);
        int pulse1 = (int) (mArm1*theta1+cArm1);
        int pulse2 = (int) (mArm2*theta2+cArm2);
        setServo(0, Math.max(Math.min(ARM_1_MAX,pulse1),ARM_1_MIN));
        UI.sleep(20);
        setServo(1, Math.max(Math.min(ARM_2_MAX,pulse2),ARM_2_MIN));
        UI.sleep(20);
        Trace.println("Servo 1:"+pulse1);
        Trace.println("Servo 2:"+pulse2);
    }

    @Override
    public RoboticArmModel getModel() {
        return theModel;
    }

    @Override
    public void setPenMode(boolean down) {
        setServo(2,down?2000:1000);
    }

    void init() throws Exception {
        //Script is required here, as there is a bug with java/c where the input stream
        //is not correctly flushed otherwise, and we recieve no input.
        //If a computer has the script command, and the script isnt found,
        //Or we forget sudo or the arm crashes, throw an exception
        ProcessBuilder builder = new ProcessBuilder("script","-c","/home/pi/Arm/arm2");
        process = builder.start();
        Thread.sleep(1000);
        if (!process.isAlive()) {
            process = null;
            throw new IOException("The arm failed to start!");
        }
        Trace.setVisible(true);
        out = new PrintStream(process.getOutputStream());
        in = new BufferedInputStream(process.getInputStream());
        calibrate();
    }
    private static final String SET_MOTOR_COMMAND = "w";
    private static final String MEASURE_ANGLE_COMMAND = "m";
    private static final String MOTOR_PREFIX = "Servo okay";
}
