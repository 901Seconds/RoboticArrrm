package net.tangentmc;

public interface RoboticArm {
    void setAngle(double theta1, double theta2);
    RoboticArmModel getModel();
}
