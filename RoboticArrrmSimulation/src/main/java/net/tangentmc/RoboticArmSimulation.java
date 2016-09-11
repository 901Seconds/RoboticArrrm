package net.tangentmc;

import com.sanjay900.ProcessingRunner;
import ecs100.UI;
import processing.core.PApplet;

import static net.tangentmc.util.Utils.*;

public class RoboticArmSimulation extends PApplet implements RoboticArm {

    RoboticArmSimulation(RoboticArmModel model) {
        theArms = model;
        ProcessingRunner.run(this);
    }
    //2*lengths ulnar and forearm
    private float l;
    private float q1X;
    private float q1Y;
    private float q2X;
    private float q2Y;
    private RoboticArmModel theArms;
    private RoboticArmPlotter plotter = new RoboticArmPlotter();
    public void settings() {
        size(800, 800);
        q1X = (float) theArms.o1X;
        q1Y = (float) theArms.o1Y;
        q2X = (float) theArms.o2X;
        q2Y = (float) theArms.o2Y;
        l = (float) theArms.l;
    }

    public void setup() {
        noLoop();
        textSize(20);
        background(30, 35, 40);
    }
    private double theta1, theta2;
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

        plotter.drawPoints(tCPs,theta1,theta2,penDown);
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
        UI.sleep(10);
    }

    @Override
    public RoboticArmModel getModel() {
        return theArms;
    }
    private boolean penDown = false;

    public void setPenMode(boolean down) {
        penDown = down;
    }

    void flagClear() {
        plotter.willClear = true;
    }
}



