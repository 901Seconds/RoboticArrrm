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

    //o is for shoulder, 1 means left, 2 meas right
    float o1X;
    float o1Y;
    float o2X;
    float o2Y;

    public static volatile double kP = 0;
    public static volatile double kI = 0;
    public static volatile double kD = 0;

    RoboticArmModel theArms;


    public void settings() {
        size(800, 800);
        xCoOrdCenter = width/2;
        yCoOrdCenter = height/2;
        o1X = xCoOrdCenter - d/2;
        o1Y = yCoOrdCenter;
        o2X = xCoOrdCenter + d/2;
        o2Y = yCoOrdCenter;
        theArms = new RoboticArmModel(o1X, o1Y, o2X, o2Y);
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
        drawAngleGraph(theta1, theta2);
        /*setTargets();

        double[] elbows = theArms.findElbowPosition(targetX, targetY);

        double theta1 = theArms.findTheta(elbows, 1, -1);
        double theta2 = theArms.findTheta(elbows, 2, 1);

        drawAngleVis(theta1, theta2);
//
        drawRanges();
        drawArms(elbows, -1, 1);
        gCursor();
        drawPIDDisplay();
        */
        drawArms(theta1, theta2);
    }

    private void drawArms(double theta1, double theta2) {
        fill(150, 150, 150, 150);
        double e1X = o1X + l * Math.cos(theta1);
        double e1Y = o1Y - l * Math.sin(theta1);
        double e2X = o2X + l * Math.cos(theta2);
        double e2Y = o2Y - l * Math.sin(theta2);
        //shoulders
        ellipse(o1X, o1Y, 10, 10);
        ellipse(o2X, o2Y, 10, 10);
        //elbows
        ellipse(e1X, e1Y, 10, 10);
        ellipse(e2X, e2Y, 10, 10);
        stroke(200, 200, 200, 150);
        line(o1X, o1Y, e1X, e1Y);
        line(o2X, o2Y, e2X, e2Y);

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


        fill(200);
        ellipse(tCPs[0], tCPs[1] + height / 2 - 100, 10, 10);
        fill(255, 0, 0);
        ellipse(tCPs[2], tCPs[3] + height / 2 - 100, 10, 10);
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
        text("left", 80, 40);
        stroke(0, 255, 0);
        fill(0, 200, 0);
        point(((float) (frameCount % 700) * width / 700), (float)(height - 40 * theta2));
        text("right", 80, 70);
        fill(30, 35, 40, 3);
        noStroke();
        rect(((float) ((frameCount - 20) % 700) * width / 700), (float)height - 150, 200, 150);
    }

    private void drawAngleVis(double theta1, double theta2) {
        noStroke();
        fill(200);
        rect(xCoOrdCenter, 20, (float)theta1 * 150, 20);
        rect(xCoOrdCenter, 50, (float)theta2 * 150, 20);
    }

    private void setTargets() {
//        int i = (frameCount / 50) % (coOrds.length);
//        i /= 2;
//        i *= 2;
////        println(i);
//        targetX = 50 + coOrds[i];
//        targetY = yCoOrdCenter - 900 + coOrds[i + 1];
////        targetX=mouseX;
////        targetY=mouseY;
////        targetX=xCoOrdCenter+160*sin((float)frameCount/16);
////        targetY=yCoOrdCenter+180*Math.cos((float)frameCount/19);
//        targetX = -370 + 2 * interPolate(((float) (frameCount % 100) / 100), coOrds[i % coOrds.length], coOrds[(i + 2) % coOrds.length]);
//        targetY = yCoOrdCenter - 400 + 2 * interPolate(((float) (frameCount % 100) / 100), coOrds[(i + 1) % coOrds.length], coOrds[(i + 3) % coOrds.length]);
////        targetX=mouseX;
////        targetY=mouseY;
//
////        targetX=interPolate(((float)frameCount%100)/100,o1X,o2X);
////        targetY=interPolate(((float)frameCount%100)/100,o1X,o2X);
    }

    private void erasePrevFrame() {
        noStroke();
        fill(30, 35, 40);
        rect(0, 0, width, height);
        rect(0, 0, width, height / 2);
        fill(30, 35, 40, 1);
        rect(0, height / 2, width, height / 2 - 200);
    }

//    private float interPolate(float proportion, float Co1, float Co2) {
//        return Co1 + proportion * (Co2 - Co1);
//
//    }
//
//
//    private void drawArms(double[] elbowPos, int leftConfig, int rightConfig) {
//        stroke(0xff, 0xff, 0xff, 0x22);
//        line(elbowPos[0], elbowPos[1], elbowPos[2], elbowPos[3]);
//        line(elbowPos[4], elbowPos[5], elbowPos[6], elbowPos[7]);
//        ellipse((elbowPos[0] + elbowPos[2]) / 2, (elbowPos[1] + elbowPos[3]) / 2, absLength(elbowPos[0], elbowPos[2], elbowPos[1], elbowPos[3]), absLength(elbowPos[0], elbowPos[2], elbowPos[1], elbowPos[3]));
//        ellipse((elbowPos[4] + elbowPos[6]) / 2, (elbowPos[5] + elbowPos[7]) / 2, absLength(elbowPos[4], elbowPos[6], elbowPos[5], elbowPos[7]), absLength(elbowPos[4], elbowPos[6], elbowPos[5], elbowPos[7]));
//
//        if (leftConfig <= 0) {
//            stroke(0xff, 0x00, 0x00, 0x88);
//            line(targetX, targetY, elbowPos[0], elbowPos[1]);
//            stroke(0x00, 0x00, 0xff, 0x88);
//            line(o1X, o1Y, elbowPos[0], elbowPos[1]);
//        }
//        if (rightConfig <= 0) {
//            stroke(0xff, 0x00, 0x00, 0x88);
//            line(targetX, targetY, elbowPos[4], elbowPos[5]);
//            stroke(0x00, 0x00, 0xff, 0x88);
//            line(o2X, o2Y, elbowPos[4], elbowPos[5]);
//        }
//
//        if (leftConfig >= 0) {
//            stroke(0xff, 0x00, 0x00, 0xdd);
//            line(targetX, targetY, elbowPos[2], elbowPos[3]);
//            stroke(0x00, 0x00, 0xff, 0xdd);
//            line(o1X, o1Y, elbowPos[2], elbowPos[3]);
//        }
//        if (rightConfig >= 0) {
//            stroke(0xff, 0x00, 0x00, 0xdd);
//            line(targetX, targetY, elbowPos[6], elbowPos[7]);
//            stroke(0x00, 0x00, 0xff, 0xdd);
//            line(o2X, o2Y, elbowPos[6], elbowPos[7]);
//        }
//
//
//    }

    //draws the reach of the ulnars pivoting from shoulders and the total working
    //area as the intersections of two ellipses centered at the shoulders
    void drawRanges() {
        noStroke();
        fill(50, 50, 60);
        ellipse(o1X, o1Y, 2 * l, 2 * l);
        ellipse(o2X, o2Y, 2 * l, 2 * l);
        stroke(200);
        noFill();
        ellipse(o1X, o1Y, 4 * l, 4 * l);
        ellipse(o2X, o2Y, 4 * l, 4 * l);
        stroke(0xff, 0xff, 0xff, 0x22);
        ellipse(o1X, o1Y, 2 * l, 2 * l);
        ellipse(o2X, o2Y, 2 * l, 2 * l);
    }

//    //displays the mouse position
//    void gCursor() {
//        noStroke();
//        fill(200);
//        ellipse(targetX, targetY, 10, 10);
//    }

//    public double[] findElbowPosition(double x, double y) {
//        return instance.theArms.findElbowPosition(x,y);
//    }

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
}



