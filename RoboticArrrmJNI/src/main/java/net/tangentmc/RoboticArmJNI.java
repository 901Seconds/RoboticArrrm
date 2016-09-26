package net.tangentmc;

import ecs100.Trace;
import ecs100.UI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Arrays;

class RoboticArmJNI implements RoboticArm {
    //These are calibration points, not real min/max.
    //That's handled by c.
    private static final int ARM_1_MIN = 1300;
    private static final int ARM_1_MAX = 1800;
    private static final int ARM_2_MIN = 1400;
    private static final int ARM_2_MAX = 1800;
    private double mArm1,mArm2,cArm1,cArm2;
    private RoboticArmModel theModel;

    //IO from the running arm2 process
    private BufferedReader in;
    private PrintStream out;
    private Process process;

    RoboticArmJNI(RoboticArmModel model) {
        theModel = model;
    }

    void setServo(int servo, int pulse) {
        if (process == null) return;
        out.println(SET_MOTOR_COMMAND);
        out.println(servo);
        out.println(pulse);
        out.flush();
    }
    private void calibrate() {
        Trace.println("Initialization:");
        //Wait for arm2 to start
        UI.sleep(4000);
        setPenMode(false);
        //Read a few angles as the first few inputs may get eaten by arm2
        readAngle(0);
        readAngle(0);
        setServo(0, ARM_1_MIN);
        setServo(1, ARM_2_MAX);
        UI.sleep(2000);
        Trace.println("Starting Calibration:");
        Trace.println("Arm 1 Min:");
        setServo(0, ARM_1_MIN);
        UI.sleep(3000);
        double arm1MinAngle = readAngle(0);
        UI.sleep(5000);

        Trace.println("Arm 1 Max:");
        setServo(0, ARM_1_MAX);
        UI.sleep(3000);
        double arm1MaxAngle = readAngle(0);
        UI.sleep(5000);

        Trace.println("Arm 2 Min:");
        setServo(1, ARM_2_MIN);
        UI.sleep(3000);
        double arm2MinAngle = readAngle(1);
        UI.sleep(5000);

        Trace.println("Arm 2 Max:");
        setServo(1, ARM_2_MAX);
        UI.sleep(3000);
        double arm2MaxAngle = readAngle(1);
        UI.sleep(3000);

        Trace.println("Calculating Constants:");
        mArm1 = ((ARM_1_MAX - ARM_1_MIN) / (arm1MaxAngle - arm1MinAngle));
        mArm2 = ((ARM_2_MAX - ARM_2_MIN) / (arm2MaxAngle - arm2MinAngle));
        cArm1 = ARM_1_MIN - (mArm1 * arm1MinAngle);
        cArm2 = ARM_2_MIN - (mArm2 * arm2MinAngle);
        Trace.println("mArm1: "+mArm1);
        Trace.println("mArm2: "+mArm2);
        Trace.println("cArm1: "+cArm1);
        Trace.println("cArm2: "+cArm2);
    }
    @Override
    public void setAngle(double theta1, double theta2) {
        theta1 = -Math.toDegrees(theta1);
        theta2 = -Math.toDegrees(theta2);

        int pulse1 = (int) (mArm1*theta1+cArm1);
        int pulse2 = (int) (mArm2*theta2+cArm2);
        setServo(0, pulse1);
        setServo(1, pulse2);
    }

    @Override
    public RoboticArmModel getModel() {
        return theModel;
    }

    @Override
    public void setPenMode(boolean down) {
        setServo(2,down?1600:1000);
    }

    void init() throws Exception {
        //Script is required here, as there is a bug with java/c where the input stream
        //is not correctly flushed otherwise, and we receive no input.
        //If a computer has the script command, and the script inst found,
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
        in  = new BufferedReader(new InputStreamReader((process.getInputStream())));
        calibrate();
    }

    private double readAngle(int servo) {
        if (process == null) return -1;
        out.println(MEASURE_ANGLE_COMMAND);
        out.flush();
        try {
            String next;
            while ((next = in.readLine())!= null) {
                if (next.startsWith("measured")) {
                    //The angles are separated by space
                    String[] args = next.replace("measured angles: ","").split(" ");
                    //The doubles are stored as theta=double so we want whats after the equals sign
                    Trace.println(Arrays.toString(Arrays.stream(args).map(s2 -> s2.split("=")[1]).toArray()));
                    return Double.parseDouble(args[servo].split("=")[1]);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private static final String SET_MOTOR_COMMAND = "w";
    private static final String MEASURE_ANGLE_COMMAND = "m";
    //TODO: it might be worth making a new arm2 executable that displays no text and is more accurate at reporting angles.
    //TODO: Should make it so that both motors are controlled at once with the pen being seperate.
    //TODO: I kinda want to swap out in the arm code using setmotor from arthurs libs and directly using gpioServo(4|17,pwm);
    //TODO: IF this still sucks, we could try ServoBlaster, tho thats a last resort.
}
