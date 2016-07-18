package net.tangentmc;

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

    public double[] getAllAngles(RoboticArmModel m, double[] points) {
        double[] angles = new double[points.length];
        for (int i = 0; i < points.length; i+=2) {
            angles[i]=m.findTheta(1,1,points[i],points[i+1]);
            angles[i+1]=m.findTheta(2,-1,points[i],points[i+1]);
        }
        return angles;
    }
}
