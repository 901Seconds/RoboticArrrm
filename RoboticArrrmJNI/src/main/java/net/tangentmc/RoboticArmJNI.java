package net.tangentmc;

/**
 * Created by sanjay on 18/07/16.
 */
//TODO: create a model that represents the robot
public class RoboticArmJNI implements RoboticArm {
    public native void motors_pulse(int u1,int u2,int dt);
    public native int find_angles(int side);
    public native void set_pen_mode(int mode);
    public double[] findElbowPosition(double x, double y) {
        return new double[0];
    }

    public void setAngles(int theta1, int theta2) {

    }

    @Override
    public void setAngle(double theta1, double theta2) {

    }

    @Override
    public RoboticArmModel getModel() {
        return null;
    }

    @Override
    public void setPenMode(boolean down) {
        set_pen_mode(down?1:0);
    }
}
