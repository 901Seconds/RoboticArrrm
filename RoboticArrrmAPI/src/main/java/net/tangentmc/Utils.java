package net.tangentmc;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Arrays;

/**
 * Created by surface on 18/07/2016.
 */
public class Utils {
    public static double absLength(double X1, double X2, double Y1, double Y2) {
        return Math.sqrt(Math.pow(X1 - X2, 2) + Math.pow(Y1 - Y2, 2));
    }

    public static double findOp(double hyp, double adj) {
        return Math.sqrt(Math.pow(hyp, 2) - Math.pow(adj, 2));
    }

    public static AngleTuple[] getAllAngles(RoboticArmModel m, Point.Double[] points) {
        AngleTuple[] angles = new AngleTuple[points.length];
        for (int i = 0; i < points.length; i++) {
            angles[i]=new AngleTuple(m.findTheta(1,-1,points[i].getX(),points[i].getY()),m.findTheta(2,1,points[i].getX(),points[i].getY()));
        }
        return angles;
    }

    public static Point.Double[] getAllPoints(Point2D.Double[] discreteCoords) {
        Point.Double[] points = new Point.Double[(discreteCoords.length-1)*100];
        for (int i2 = 0; i2 <discreteCoords.length-1; i2++) {
            for (int i =0; i < 100; i++) {
                points[(i2*100)+i] = new Point2D.Double(interPolate(i/100d, discreteCoords[i2].getX(), discreteCoords[i2 + 1].getX()),
                        interPolate(i/100d, discreteCoords[i2].getY(), discreteCoords[i2 + 1].getY()));
            }
        }
        return points;
    }

    private static double interPolate(double proportion, double Co1, double Co2) {
        return Co1 + proportion * (Co2 - Co1);

    }
    @AllArgsConstructor
    @Getter
    public static class AngleTuple {
        double theta1,theta2;
    }

}
