package net.tangentmc;

public interface RoboticArm {
    double[] findElbowPosition(double x, double y);
    void setAngles(double[] allPoints);
}
