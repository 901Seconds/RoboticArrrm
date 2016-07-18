package net.tangentmc;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
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

    public static Point.Double[] getAllPoints(Shape shape) {
        FlatteningPathIterator iter;
        ArrayList<Point.Double> points;
        iter=new FlatteningPathIterator(shape.getPathIterator(new AffineTransform()), 0.001);
        points=new ArrayList<>();
        double[] coords=new double[6];
        while (!iter.isDone()) {
            iter.currentSegment(coords);
            double x=coords[0];
            double y=coords[1];
            points.add(new Point.Double(x,y));
            iter.next();
        }
        System.out.print(points);
        return points.toArray(new Point.Double[0]);
    }
    @AllArgsConstructor
    @Getter
    public static class AngleTuple {
        double theta1,theta2;
    }

}
