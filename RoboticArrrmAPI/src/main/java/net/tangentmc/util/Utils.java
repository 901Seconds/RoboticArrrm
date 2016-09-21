package net.tangentmc.util;

import net.tangentmc.RoboticArmModel;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.PathIterator;
import java.util.ArrayList;


public class Utils {
    private static final int X_DISP = 200;
    private static final int Y_DISP = 75;
    public static double absLength(double X1, double X2, double Y1, double Y2) {
        return Math.sqrt(Math.pow(X1 - X2, 2) + Math.pow(Y1 - Y2, 2));
    }

    public static double findOp(double hyp, double adj) {
        return Math.sqrt(Math.pow(hyp, 2) - Math.pow(adj, 2));
    }

    public static Angle convertPoint(RoboticArmModel m, DrawPoint point) {
        point.x += X_DISP;
        point.y += Y_DISP;
        return new Angle(m.findTheta(1, -1, point.getX(), point.getY()), m.findTheta(2, 1, point.getX(), point.getY()),point.isPenDown());
    }
    public static ArrayList<DrawPoint> getAllPoints(Shape shape, double minDist) {
        ArrayList<DrawPoint> points = new ArrayList<>();
        double[] coords = new double[6];
        FlatteningPathIterator it = new FlatteningPathIterator(shape.getPathIterator(new AffineTransform()),minDist);
        for (; !it.isDone(); it.next()) {
            int type = it.currentSegment(coords);
            //MOVE_TO is pen up, since you want to move without drawing
            points.add(new DrawPoint(coords[0],coords[1],type!=PathIterator.SEG_MOVETO,1,null));
        }

        return points;
    }
    public static double lerp(double proportion, double Co1, double Co2) {
        return Co1+proportion*(Co2-Co1);
    }
}
