package net.tangentmc;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

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

    public static ArrayList<AngleTuple[]> getAllAngles(RoboticArmModel m, ArrayList<Point.Double[]> pointcol) {
        ArrayList<AngleTuple[]> anglecol = new ArrayList<>();
        for (Point.Double[] points: pointcol) {
            AngleTuple[] angles = new AngleTuple[points.length];
            for (int i = 0; i < points.length; i++) {
                angles[i] = new AngleTuple(m.findTheta(1, 1, points[i].getX(), points[i].getY()), m.findTheta(2, 1, points[i].getX(), points[i].getY()));
            }
            anglecol.add(angles);
        }
        return anglecol;
    }

    public static ArrayList<Point.Double[]> getAllPoints(Shape shape) {
        ArrayList<Point.Double[]> pointcol = new ArrayList<>();
        ArrayList<Point.Double> points = new ArrayList<>();
            double[] coords = new double[6];
            for (InterpolatedPathIterator it = new InterpolatedPathIterator(shape.getPathIterator(new AffineTransform()),0.01); !it.isDone(); it.next()) {
                it.currentSegment(coords);
                //If a path is 10 pixels away, its likely a path that has been moved
                //So we seperate it out into its own path.
                if (points.size() > 0 && points.get(points.size()-1).distanceSq(coords[0],coords[1]) > 10 ){
                    pointcol.add(points.toArray(new Point2D.Double[0]));
                    points.clear();
                }
                coords[0] = coords[0]+200;
                coords[1] = coords[1]+150;
                points.add(new Point2D.Double(coords[0],coords[1]));
            }
            pointcol.add(points.toArray(new Point2D.Double[0]));
        return pointcol;
    }

    public static double interPolate(double proportion, double Co1, double Co2) {
        return Co1+proportion*(Co2-Co1);

    }
}
