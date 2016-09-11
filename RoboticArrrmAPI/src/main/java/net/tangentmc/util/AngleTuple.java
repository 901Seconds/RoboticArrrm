package net.tangentmc.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.awt.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class AngleTuple {
    double theta1,theta2;
    boolean penUp = false;
    public AngleTuple(double theta1, double theta2) {
        this.theta1 = theta1;
        this.theta2 = theta2;
    }
    public double dist(AngleTuple other) {
        if (other == null) return 0;
        return Point.distance(theta1,theta1,other.theta1,other.theta2);
    }
}