package net.tangentmc;

import net.tangentmc.RoboticArm;

/**
 * Created by sanjay on 18/07/16.
 */
public class RoboticArmJNI implements RoboticArm {
    public native void motors_pulse(int u1,int u2,int dt);
    public native int find_angles(int side);
    public double[] findElbowPosition(double x, double y) {
        return new double[0];
    }

    public void setAngles(int theta1, int theta2) {

    }
}
