package net.tangentmc;

import ecs100.UI;
import io.socket.client.IO;
import io.socket.client.Socket;
import org.json.JSONException;
import org.json.JSONObject;
import org.scijava.nativelib.NativeLoader;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static net.tangentmc.Utils.absLength;


//TODO: create a model that represents the robot
public class RoboticArmJNI implements RoboticArm {
    private static final int ARM_1_MIN = 1500;
    private static final int ARM_1_MAX = 2000;
    private static final int ARM_2_MIN = 1000;
    private static final int ARM_2_MAX = 1400;
    double arm1MinAngle, arm1MaxAngle,arm2MinAngle, arm2MaxAngle;
    Socket socket;
    BlockingQueue<JSONObject> movementQueue = new LinkedBlockingQueue<>();

    public static void main(String[] args) {
        if (args.length == 0) args = new String[]{"10.140.108.96:"};
        try {
            RoboticArmJNI robot = new RoboticArmJNI(287,374,377,374,154, args[0]);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    static {
        try {
            NativeLoader.loadLibrary("RoboticArrrmJNI-1.0-SNAPSHOT");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    double o1X, o1Y, o2X, o2Y;
    double d, l;
    RoboticArmModel theModel;
    public RoboticArmJNI(double shoulder1X, double shoulder1Y, double shoulder2X, double shoulder2Y, double appendageLength, String ip) throws InterruptedException {
        try {
            socket = IO.socket("http://"+ip+":9092");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        socket.on("setAngle",args->{
            JSONObject obj = (JSONObject)args[0];
            movementQueue.add(obj);

        }).on("setPenMode",args->{
            JSONObject obj = (JSONObject)args[0];
            movementQueue.add(obj);
        }).on(Socket.EVENT_CONNECT, args-> {
            System.out.print("CONNETED.");
        });
        new Thread(socket::connect).start();
        o1X=shoulder1X;
        o1Y=shoulder1Y;
        o2X=shoulder2X;
        o2Y=shoulder2Y;
        d=absLength(o1X,o2X,o1Y,o2Y);
        l=appendageLength;
        theModel = new RoboticArmModel(o1X,o1Y,o2X,o2Y,l);
        init();
        calibrate();
        while (true) {
            JSONObject obj = movementQueue.take();
            try {
                if (obj.has("theta1")) {
                    setAngle(obj.getDouble("theta1"), obj.getDouble("theta2"));
                } else if (obj.has("penMode")){
                    setPenMode(obj.getBoolean("penMode"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    public native void init();
    public native double readAngle(int servo);
    public native void setServo(int servo, double pulse);
    public void calibrate() {
            setServo(0, ARM_1_MIN);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            arm1MinAngle = readAngle(0);
            setServo(0, ARM_1_MAX);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            arm1MaxAngle = readAngle(0);
            setServo(1, ARM_2_MIN);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            arm2MinAngle = readAngle(1);
            setServo(1, ARM_2_MAX);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            arm2MaxAngle = readAngle(1);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mArm1 = ((ARM_1_MAX - ARM_1_MIN) / (arm1MaxAngle - arm1MinAngle));
            mArm2 = ((ARM_2_MAX - ARM_2_MIN) / (arm2MaxAngle - arm2MinAngle));
            cArm1 = ARM_1_MIN - mArm1 * arm1MinAngle;
            cArm2 = ARM_2_MIN - mArm2 * arm2MinAngle;
    }
    double lastTheta1 = -90;
    double lastTheta2 = -90;
    double mArm1,mArm2,cArm1,cArm2;
    @Override
    public void setAngle(double theta1, double theta2) {
        lastTheta1 = theta1;
        lastTheta2 = theta2;
        theta1 = -Math.toDegrees(theta1);
        theta2 = -Math.toDegrees(theta2);

        double pulse1 = mArm1*theta1+cArm1;
        double pulse2 = mArm2*theta2+cArm2;
        setServo(0,Math.max(Math.min(ARM_1_MAX,pulse1),ARM_1_MIN));
        setServo(1,Math.max(Math.min(ARM_2_MAX,pulse2),ARM_2_MIN));
        UI.println("Set angles to: "+theta1+","+theta2);
        UI.println("Measured: "+readAngle(0)+","+readAngle(1));

    }

    @Override
    public RoboticArmModel getModel() {
        return null;
    }

    @Override
    public void setPenMode(boolean down) {
        setServo(2,down?2000:1000);
    }
}
