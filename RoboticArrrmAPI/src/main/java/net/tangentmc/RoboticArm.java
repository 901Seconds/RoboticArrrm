package net.tangentmc;

public interface RoboticArm {
    double[] findElbowPosition(double x, double y);
    void setAngles(int theta1, int theta2);
}
