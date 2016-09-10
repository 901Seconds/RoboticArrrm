package net.tangentmc;

import com.sanjay900.ProcessingRunner;
import processing.core.PApplet;

public class RoboticArmPlotter extends PApplet {
    boolean willClear = false;

    RoboticArmPlotter() {
        ProcessingRunner.run(this);
    }
    public void settings() {
        size(1280, 800);
    }

    public void setup() {
        noLoop();
        background(30, 35, 40);
    }
    private double[] tCPs;
    public void draw() {
        if (willClear) {
            background(30, 35, 40);
            willClear = false;
        }
        if (tCPs == null) return;
        erasePrevFrame();
        fill(200);
        ellipse(tCPs[0], tCPs[1], 1, 1);
        fill(255, 0, 0);
        ellipse(tCPs[2], tCPs[3], 1, 1);
        drawAngleGraph(theta1,theta2);
        drawAngleVis(theta1,theta2);
    }

    private void drawAngleVis(double theta1, double theta2) {
        noStroke();
        fill(200);
        text("left", 100, 40);
        text("right", 100, 70);
        fill(200);
        rect(width/2, 20, (float)theta1 * 150, 20);
        rect(width/2, 50, (float)theta2 * 150, 20);
    }


    private void ellipse(double X1, double Y1, double width, double height) {
        ellipse((float)X1,(float)Y1,(float)width,(float)height);
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

    private void erasePrevFrame() {
        noStroke();
        fill(30, 35, 40, 1);
        rect(0, 0, width, height);
        fill(30, 35, 40);
        rect(0, 0, width, 100);
    }
    private double theta1,theta2;
    void drawPoints(double[] tCPs, double theta1, double theta2) {
        this.tCPs = tCPs;
        this.theta1 = theta1;
        this.theta2 = theta2;
        loop();
    }
}



