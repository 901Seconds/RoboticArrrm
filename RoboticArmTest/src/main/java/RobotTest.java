import net.tangentmc.RoboticArm;
import net.tangentmc.RoboticArmModel;
import net.tangentmc.SVGer;
import net.tangentmc.Utils;

/**
 * Created by sanjay on 18/07/16.
 */
public class RobotTest {
    double[] points;
    public RobotTest() {
        points = new SVGer().pointsFromXML("file.svg");
        RoboticArmSimulation.createDraw();
        RoboticArmSimulation arm = RoboticArmSimulation.instance;
        arm.setAngles(Utils.getAllAngles(m,Utils.setTargets(points)));
    }
}
