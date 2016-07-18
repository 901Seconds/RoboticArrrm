import net.tangentmc.RoboticArm;
import net.tangentmc.RoboticArmModel;
import net.tangentmc.SVGer;
import net.tangentmc.Utils;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Arrays;

/**
 * Created by sanjay on 18/07/16.
 */
public class RobotTest {
    Point.Double[][] points;
    public static void main(String[] args) {
        new RobotTest();
    }
    public RobotTest() {
        points = new SVGer().pointsFromXML("file.svg");
        RoboticArmSimulation.createDraw();
        RoboticArm arm = RoboticArmSimulation.instance;
        Utils.AngleTuple[][] angles = new Utils.AngleTuple[points.length][];
        for (int i = 0; i < points.length; i++) {
            angles[i] = Utils.getAllAngles(arm.getModel(),Utils.getAllPoints(points[i]));
        }
        while (true) {
            for (int i = 0; i < angles.length; i++) {
                for (int i2 = 0; i2 < angles[i].length; i2++) {
                    arm.setAngle(angles[i][i2].getTheta1(),angles[i][i2].getTheta2());
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }
}
