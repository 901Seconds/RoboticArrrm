import net.tangentmc.RoboticArmModel;
import processing.core.PApplet;
import net.tangentmc.RoboticArm;

import java.awt.*;
import java.lang.invoke.MethodHandles;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import static net.tangentmc.Utils.*;

public class RoboticArmPlotter extends PApplet {


    private static RoboticArmPlotter instance;
    public RoboticArmPlotter() {
        instance=this;
    }

    public static RoboticArmPlotter createDraw() {
        main(MethodHandles.lookup().lookupClass().getName());
        return instance;
    }




    public void settings() {
        size(1280, 800);
    }

    public void setup() {
        noLoop();
        background(30, 35, 40);
    }
    double[] tCPs;
    public void draw() {
        if (tCPs == null) return;
        erasePrevFrame();
        fill(200);
        ellipse(tCPs[0], tCPs[1] + height / 2 , 10, 10);
        fill(255, 0, 0);
        ellipse(tCPs[2], tCPs[3] + height / 2 , 10, 10);
    }


    private void ellipse(double X1, double Y1, double width, double height) {
        ellipse((float)X1,(float)Y1,(float)width,(float)height);
    }

    private void line(double X1, double Y1, double X2, double Y2) {
        line((float)X1,(float)Y1,(float)X2,(float)Y2);
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
    }
    public void drawPoints(double[] tCPs) {
        this.tCPs = tCPs;
        loop();
    }
}



