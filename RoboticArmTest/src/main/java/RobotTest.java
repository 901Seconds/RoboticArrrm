import net.tangentmc.RoboticArm;
import net.tangentmc.RoboticArmModel;
import net.tangentmc.SVGer;
import net.tangentmc.Utils;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by sanjay on 18/07/16.
 */
public class RobotTest {
    Shape[] points;
    public static void main(String[] args) {
        new RobotTest();
    }
    public RobotTest() {
        points = new SVGer().pointsFromXML("file.svg");
        RoboticArm arm = RoboticArmSimulation.createDraw();

        ArrayList<Utils.AngleTuple[]> angles = new ArrayList<>();
        for (int i = 0; i < points.length; i++) {
            angles.addAll(Utils.getAllAngles(arm.getModel(),Utils.getAllPoints(points[i])));
        }
        while (true) {
            for (int i = 0; i < angles.size(); i++) {
                arm.setAngle(angles.get(i)[0].getTheta1(),angles.get(i)[0].getTheta2());
                arm.setPenMode(true);
                for (int i2 = 0; i2 < angles.get(i).length; i2++) {
                    arm.setAngle(angles.get(i)[i2].getTheta1(),angles.get(i)[i2].getTheta2());
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                arm.setPenMode(false);
            }

        }
    }
}
