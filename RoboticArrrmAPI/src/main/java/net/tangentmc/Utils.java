package net.tangentmc;

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

    public static double[] getAllAngles(RoboticArmModel m, double[] points) {
        double[] angles = new double[points.length];
        for (int i = 0; i < points.length; i+=2) {
            angles[i]=m.findTheta(1,1,points[i],points[i+1]);
            angles[i+1]=m.findTheta(2,-1,points[i],points[i+1]);
        }
        return angles;
    }

    public static double[] getAllPoints(double[] discreteCoOrds) {
        double[] points = new double[discreteCoOrds.length*100];
        for (int frameCount=0; frameCount<points.length-1; frameCount+=1) {
            int i = (frameCount / 50) % (discreteCoOrds.length);
            i/=2;
            i*=2;
            points[frameCount] = interPolate(((float) (frameCount % 100) / 100), discreteCoOrds[i % discreteCoOrds.length], discreteCoOrds[(i + 2) % discreteCoOrds.length]);
            points[frameCount+1] = interPolate(((float) (frameCount % 100) / 100), discreteCoOrds[(i + 1) % discreteCoOrds.length], discreteCoOrds[(i + 3) % discreteCoOrds.length]);
        }
        System.out.println(Arrays.toString(points));
        return points;
    }

    private static double interPolate(double proportion, double Co1, double Co2) {
        return Co1 + proportion * (Co2 - Co1);

    }

}
