package net.tangentmc;


import static net.tangentmc.util.Utils.absLength;
import static net.tangentmc.util.Utils.findOp;

public class RoboticArmModel {

    //2*lengths ulnar and forearm
    double l;

    //o is for shoulder, 1 means left, 2 means right
    double o1X;
    double o1Y;
    double o2X;
    double o2Y;

    RoboticArmModel(double shoulder1X, double shoulder1Y, double shoulder2X, double shoulder2Y, double armLength) {
        o1X=shoulder1X;
        o1Y=shoulder1Y;
        o2X=shoulder2X;
        o2Y=shoulder2Y;
        l=armLength;
    }

    private double findTheta(double[] elbows, int shoulderNum, int leftRight) {
        double angle;
        double X1=0,Y1=0,X2=0,Y2=0;
        if(shoulderNum==1) {
            X1=o1X;
            Y1=o1Y;
            if(leftRight<=0) {
                X2=elbows[0];
                Y2=elbows[1];
            }
            if(leftRight>0) {
                X2=elbows[2];
                Y2=elbows[3];
            }
        }
        else if(shoulderNum==2) {
            X1=o2X;
            Y1=o2Y;
            if(leftRight<=0) {
                X2=elbows[4];
                Y2=elbows[5];
            }
            if(leftRight>0) {
                X2=elbows[6];
                Y2=elbows[7];
            }
        }
        else {
            System.out.println("shoulder number must be 1 or 2");
        }
        angle = Math.atan2(Y1-Y2,-X1+X2);
        return angle;
    }

    public double findTheta(int shoulderNum, int leftRight, double x, double y) {
        return findTheta(findElbowPosition(x,y),shoulderNum,leftRight);
    }
    double[] findTCPPos(double theta1, double theta2) {

        //e is for elbow, 1 is for left, 2 is for right
        double e1X, e1Y, e2X, e2Y;
        e1X=o1X+l* Math.cos(theta1);
        e1Y=o1Y-l* Math.sin(theta1);
        e2X=o2X+l* Math.cos(theta2);
        e2Y=o2Y-l* Math.sin(theta2);

        //elbow center x, elbow center y; the half way point between the elbows
        double eCX = (e1X+e2X)/2;
        double eCY = (e1Y+e2Y)/2;

        double tcpRad = findOp(l,absLength(e1X,e2X,e1Y,e2Y)/2);
        //inverse reciprocal of that line's angle
        double invRepElbowLineAngle = Math.atan2(e1X-e2X,-(e1Y-e2Y));

        double[] points = new double[4];

        points[0] = eCX + tcpRad* Math.cos(invRepElbowLineAngle);
        points[1] = eCY + tcpRad* Math.sin(invRepElbowLineAngle);
        points[2] = eCX - tcpRad* Math.cos(invRepElbowLineAngle);
        points[3] = eCY - tcpRad* Math.sin(invRepElbowLineAngle);

        return points;
    }


    private double[] findElbowPosition(double targetX, double targetY) {
        //co-ordinates of the center point of a line drawn from the shoulders to the target
        double o1XC = (targetX+o1X)/2;
        double o1YC = (targetY+o1Y)/2;
        double o2XC = (targetX+o2X)/2;
        double o2YC = (targetY+o2Y)/2;

        //length of the line between the shoulders and the target
        double abs1 = absLength(o1X,targetX,o1Y,targetY);
        double abs2 = absLength(o2X,targetX,o2Y,targetY);

        //radius of a circle centered halfway between the shoulders and the target so that the distance
        //between its intersection with the reach of the shoulders and the target is equal to l
        double o1R = findOp(l,abs1/2);
        double o2R = findOp(l,abs2/2);

        //finds the angle of the line from the shoulders to the target
        //and the inverse reciprocal
        double o1NormalAngle = Math.atan2(-(targetX-o1X),(targetY-o1Y));
        double o2NormalAngle = Math.atan2(-(targetX-o2X),(targetY-o2Y));

        //uses the length of the line as determined by the radius of the circle
        //and the slope of the line as determined by the inverse reciprocal of the angle
        //to determine the co-ordinates where the normal intersects the reach of the ulnar
        double o1NormalX1 = o1XC + o1R* Math.cos(o1NormalAngle);
        double o1NormalY1 = o1YC + o1R* Math.sin(o1NormalAngle);
        double o1NormalX2 = o1XC - o1R* Math.cos(o1NormalAngle);
        double o1NormalY2 = o1YC - o1R* Math.sin(o1NormalAngle);

        double o2NormalX1 = o2XC + o2R* Math.cos(o2NormalAngle);
        double o2NormalY1 = o2YC + o2R* Math.sin(o2NormalAngle);
        double o2NormalX2 = o2XC - o2R* Math.cos(o2NormalAngle);
        double o2NormalY2 = o2YC - o2R* Math.sin(o2NormalAngle);

        return new double[]{o1NormalX1,o1NormalY1,o1NormalX2,o1NormalY2,o2NormalX1,o2NormalY1,o2NormalX2,o2NormalY2};
    }

}
