import net.tangentmc.RoboticArm;
import net.tangentmc.RoboticArmModel;
import net.tangentmc.SVGer;

/**
 * Created by sanjay on 18/07/16.
 */
public class RobotTest {
    double[] points;
    public RobotTest() {
        points = new SVGer().pointsFromXML("file.svg");
        RoboticArm arm = new RoboticArmModel();
        int frameCount;
        while (true) {


        }
    }
}
