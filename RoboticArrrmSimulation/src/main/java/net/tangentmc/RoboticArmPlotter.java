package net.tangentmc;

import com.sanjay900.ProcessingRunner;
import ecs100.UI;
import processing.core.PApplet;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class RoboticArmPlotter extends PApplet {
    boolean willClear = false;
    boolean penDown;
    private Queue<Object[]> drawables = new LinkedBlockingQueue<>();
    RoboticArmPlotter() {
        ProcessingRunner.run(this);
    }
    public void settings() {
        size(1280, 800);
    }

    public void setup() {
        background(255, 255, 255);
    }
    public void draw() {
        if (drawables.isEmpty()) return;
        Object[] draw = drawables.poll();
        double[] lasttCPs = (double[]) draw[0];
        double[] tCPs = (double[]) draw[1];
        double theta1 = (double) draw[2];
        double theta2 = (double) draw[3];
        boolean penDown = (boolean) draw[4];
        if (willClear) {
            background(255, 255, 255);
            willClear = false;
        }
        if (tCPs == null || lasttCPs == null) return;
        if (penDown) {
            stroke(0, 0, 0);
            line(lasttCPs[0], lasttCPs[1], tCPs[0], tCPs[1]);
            stroke(0, 0, 0);
            line(lasttCPs[2], lasttCPs[3], tCPs[2], tCPs[3]);
        } else {
            //pen up
            //stroke(255, 255, 255);
            //line(lasttCPs[0], lasttCPs[1], tCPs[0], tCPs[1]);
        }
        drawAngleGraph(theta1,theta2);
        drawAngleVis(theta1,theta2);
    }

    private void line(double X1, double Y1, double X2, double Y2) {
        line((float)X1,(float)Y1,(float)X2,(float)Y2);
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

    private void drawAngleGraph(double theta1, double theta2) {
        stroke(255, 0, 0);
        fill(200, 0, 0);
        point(((float) (frameCount % 700) * width / 700), (float)(height - 40 * theta1));
        stroke(0, 255, 0);
        fill(0, 200, 0);
        point(((float) (frameCount % 700) * width / 700), (float)(height - 40 * theta2));
        fill(255, 255, 255, 3);
        noStroke();
        rect((((frameCount - 20) % 700) * width / 700), height - 150, 200, 150);
    }

    private void erasePrevFrame() {
        noStroke();
        fill(255,255,255, 1);
        rect(0, 0, width, height);
        fill(255,255,255);
        rect(0, 0, width, 100);
    }
    private double[] lasttCPs = null;
    void drawPoints(double[] tCPs, double theta1, double theta2) {
        drawables.add(new Object[]{lasttCPs,tCPs,theta1,theta2,penDown});
        lasttCPs = tCPs;
    }
}



