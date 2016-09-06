package net.tangentmc;

import com.sanjay900.ProcessingRunner;
import processing.core.PApplet;

import static net.tangentmc.Utils.*;

public class RoboticArmSimulation extends PApplet implements RoboticArm {

    public RoboticArmSimulation() {
        ProcessingRunner.run(this);
    }
    //2*lengths ulnar and forearm
    final float l = 180;
    //2*distance between shoulders
    final float d = 236;
    //centerpoint between shoulders
    float xCoOrdCenter;
    float yCoOrdCenter;

    float q1X;
    float q1Y;
    float q2X;
    float q2Y;
    public static volatile double kP = 0;
    public static volatile double kI = 0;
    public static volatile double kD = 0;

    RoboticArmModel theArms;
    RoboticArmPlotter plotter = new RoboticArmPlotter();
    public void settings() {
        size(800, 800);
        xCoOrdCenter = width/2;
        yCoOrdCenter = height/2;
        q1X = xCoOrdCenter - d / 2;
        q1Y = yCoOrdCenter;
        q2X = xCoOrdCenter + d / 2;
        q2Y = yCoOrdCenter;
        theArms = new RoboticArmModel(q1X, q1Y, q2X, q2Y);
    }

    public void setup() {
        noLoop();

        textSize(20);
        background(30, 35, 40);
    }
    double theta1, theta2;
    public void draw() {
        //Shift the bot towards the center of the screen
        //translate(200,200);
        erasePrevFrame();
        drawOtherArms(theta1, theta2);
    }

    private void drawOtherArms(double theta1, double theta2) {
        fill(150, 150, 150, 150);
        double e1X = q1X + l * Math.cos(theta1);
        double e1Y = q1Y - l * Math.sin(theta1);
        double e2X = q2X + l * Math.cos(theta2);
        double e2Y = q2Y - l * Math.sin(theta2);
        //shoulders
        ellipse(q1X, q1Y, 10, 10);
        ellipse(q2X, q2Y, 10, 10);
        //elbows
        ellipse(e1X, e1Y, 10, 10);
        ellipse(e2X, e2Y, 10, 10);
        stroke(200, 200, 200, 150);
        line(q1X, q1Y, e1X, e1Y);
        line(q2X, q2Y, e2X, e2Y);

        double[] tCPs = theArms.findTCPPos(theta1, theta2);
        line(tCPs[0], tCPs[1], tCPs[2], tCPs[3]);
        line(e1X, e1Y, tCPs[0], tCPs[1]);
        line(e1X, e1Y, tCPs[2], tCPs[3]);
        line(e2X, e2Y, tCPs[0], tCPs[1]);
        line(e2X, e2Y, tCPs[2], tCPs[3]);

        noFill();
        ellipse((e1X + e2X) / 2, (e1Y + e2Y) / 2, absLength(e1X, e2X, e1Y, e2Y), absLength(e1X, e2X, e1Y, e2Y));
        ellipse((e1X + e2X) / 2, (e1Y + e2Y) / 2, absLength(tCPs[0], tCPs[2], tCPs[1], tCPs[3]), absLength(tCPs[0], tCPs[2], tCPs[1], tCPs[3]));
        ellipse(e1X, e1Y, 2 * l, 2 * l);
        ellipse(e2X, e2Y, 2 * l, 2 * l);

        noStroke();
        fill(200);
        ellipse(tCPs[0], tCPs[1], 10, 10);
        fill(255, 0, 0);
        ellipse(tCPs[2], tCPs[3], 10, 10);

        plotter.drawPoints(tCPs,theta1,theta2);
    }

    private void ellipse(double X1, double Y1, double width, double height) {
        ellipse((float)X1,(float)Y1,(float)width,(float)height);
    }

    private void line(double X1, double Y1, double X2, double Y2) {
        line((float)X1,(float)Y1,(float)X2,(float)Y2);
    }

    private void erasePrevFrame() {
        noStroke();
        fill(30, 35, 40);
        rect(0, 0, width, height);
    }
    @Override
    public void setAngle(double theta1, double theta2) {
        this.theta1 = theta1;
        this.theta2 = theta2;
        loop();
    }

    @Override
    public RoboticArmModel getModel() {
        return theArms;
    }
    public boolean penDown = false;
    //@Override
    public void setPenMode(boolean down) {
        penDown = down;
    }

    public void flagClear() {
        plotter.willClear = true;
    }
}


