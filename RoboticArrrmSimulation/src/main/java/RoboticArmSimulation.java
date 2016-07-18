import net.tangentmc.RoboticArmModel;
import processing.core.PApplet;
import net.tangentmc.RoboticArm;

import java.awt.*;
import java.lang.invoke.MethodHandles;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import static net.tangentmc.Utils.*;

public class RoboticArmSimulation extends PApplet implements RoboticArm {


    public static RoboticArmSimulation instance;
    public RoboticArmSimulation() {
        instance=this;
    }

    public static void createDraw() {
        main(MethodHandles.lookup().lookupClass().getName());
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

    float targetX, targetY;
    //not used
    //float h1X, h1Y, h2X, h2Y;

    double[] coOrds;
    public static volatile double kP = 0;
    public static volatile double kI = 0;
    public static volatile double kD = 0;

    RoboticArmModel theOtherArms;


    public void settings() {
        size(1280, 800);
        xCoOrdCenter = 500;
        yCoOrdCenter = 400;
        q1X = xCoOrdCenter - d / 2;
        q1Y = yCoOrdCenter - 100;
        q2X = xCoOrdCenter + d / 2;
        q2Y = yCoOrdCenter - 100;
        theOtherArms = new RoboticArmModel(q1X, q1Y, q2X, q2Y);
    }

    public void setup() {
        //XMLer.pointsFromSVG("file.svg");
        noLoop();

        textSize(20);
        background(30, 35, 40);
    }

//    private void launchAdjustment() {
//        Thread adjustment = new Thread(new PIDAdjuster());
//        adjustment.start();
//        //String[] args = null;
//        //PIDAdjuster.main(args);
//    }
    double theta1, theta2;
    public void draw() {
        erasePrevFrame();
        /*setTargets();

        double[] elbows = theArms.findElbowPosition(targetX, targetY);

        double theta1 = theArms.findTheta(elbows, 1, -1);
        double theta2 = theArms.findTheta(elbows, 2, 1);

        drawAngleVis(theta1, theta2);
//        drawAngleGraph(theta1, theta2);
        drawRanges();
        drawArms(elbows, -1, 1);
        gCursor();
        drawPIDDisplay();
        */
        drawOtherArms(theta1, theta2);
        drawAngleVis(theta1, theta2);
       drawAngleGraph(theta1, theta2);
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

        double[] tCPs = theOtherArms.findTCPPos(theta1, theta2);
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


        fill(200);
        ellipse(tCPs[0], tCPs[1] + height / 2 +150, 10, 10);
        fill(255, 0, 0);
        ellipse(tCPs[2], tCPs[3] + height / 2 +150, 10, 10);
    }

    private void ellipse(double X1, double Y1, double width, double height) {
        ellipse((float)X1,(float)Y1,(float)width,(float)height);
    }

    private void line(double X1, double Y1, double X2, double Y2) {
        line((float)X1,(float)Y1,(float)X2,(float)Y2);
    }

    private void drawPIDDisplay() {
        fill(200, 200, 200, 100);
        noStroke();
        rect(50, 500, 10, -20 * (float) kP);
        rect(100, 500, 10, -20 * (float) kI);
        rect(150, 500, 10, -20 * (float) kD);
    }

    private void drawAngleGraph(double theta1, double theta2) {
        stroke(255, 0, 0);
        fill(200, 0, 0);
        point(((float) (frameCount % 700) * width / 700), (float)(height - 40 * theta1));
        stroke(0, 255, 0);
        fill(0, 200, 0);
        point(((float) (frameCount % 700) * width / 700), (float)(height - 40 * theta2));
        fill(30, 35, 40, 3);
        noStroke();
        rect((((frameCount - 20) % 700) * width / 700), height - 150, 200, 150);
    }

    private void drawAngleVis(double theta1, double theta2) {
        noStroke();
        text("left", xCoOrdCenter-50, 40);
        text("right", xCoOrdCenter-50, 70);
        fill(200);
        rect(xCoOrdCenter, 20, (float)theta1 * 150, 20);
        rect(xCoOrdCenter, 50, (float)theta2 * 150, 20);
    }

    private void erasePrevFrame() {
        noStroke();
        fill(30, 35, 40);
        rect(0, 0, width / 2, height - 200);
        rect(0, 0, width, height / 2);
        fill(30, 35, 40, 1);
        rect(0, height / 2, width, height / 2 - 200);
    }

    //displays the mouse position
    void gCursor() {
        noStroke();
        fill(200);
        ellipse(targetX, targetY, 10, 10);
    }
    @Override
    public void setAngle(double theta1, double theta2) {
        this.theta1 = theta1;
        this.theta2 = theta2;
        loop();
    }

    @Override
    public RoboticArmModel getModel() {
        return theOtherArms;
    }
}



