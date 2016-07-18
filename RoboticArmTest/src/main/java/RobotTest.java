import net.tangentmc.RoboticArm;
import net.tangentmc.RoboticArmModel;
import net.tangentmc.SVGer;
import net.tangentmc.Utils;

import java.awt.*;
import java.util.Arrays;

/**
 * Created by sanjay on 18/07/16.
 */
public class RobotTest {
    Point.Double[] points;
    public static void main(String[] args) {
        new RobotTest();
    }
    public RobotTest() {
        points = new SVGer().pointsFromXML("file.svg");
        RoboticArmSimulation.createDraw();
        RoboticArm arm = RoboticArmSimulation.instance;
        Utils.AngleTuple[] angles = Utils.getAllAngles(arm.getModel(),Utils.getAllPoints(points));
        while (true) {
            for (int i = 0; i < angles.length; i++) {
                arm.setAngle(angles[i].getTheta1(),angles[i].getTheta2());
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
