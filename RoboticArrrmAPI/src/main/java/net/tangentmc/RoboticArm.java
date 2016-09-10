package net.tangentmc;

interface RoboticArm {

    void setAngle(double theta1, double theta2);

    RoboticArmModel getModel();
    /**
     * Set if the pen is up (not on the page) or down
     * @param down true for down, false for up
     */
    void setPenMode(boolean down);
}
