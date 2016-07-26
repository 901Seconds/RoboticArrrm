import net.tangentmc.*;

import java.awt.*;
import java.util.ArrayList;


public class RobotTest {
    Shape[] points;

    public static void main(String[] args) {
        new RobotTest();
    }

    public RobotTest() {
        //loads svg file into an array of shapes
        points = new SVGer().shapesFromXML("file.svg");

        //creates new arm simulation
        RoboticArm arm = new RoboticArmSimulation();

        //produces a list of angle pairs from the array of shapes by giving them to the robotic arm model created by the simulation
        ArrayList<AngleTuple[]> angles = new ArrayList<>();
        for (int i = 0; i < points.length; i++) {
            angles.addAll(Utils.getAllAngles(arm.getModel(),Utils.getAllPoints(points[i])));
        }

        //goes through the list of angle pairs and sets theta1 ann theta2 to them.
        while (true) {
            for (int i = 0; i < angles.size(); i++) {
                //TODO: In here we need to transition from the last theta to this one before dropping the pen
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
