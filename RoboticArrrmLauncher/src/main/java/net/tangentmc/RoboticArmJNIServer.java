package net.tangentmc;

import org.json.JSONException;
import org.json.JSONObject;

import static net.tangentmc.Utils.absLength;

/**
 * Created by sanjay on 10/09/2016.
 */
public class RoboticArmJNIServer implements RoboticArm {

    double o1X, o1Y, o2X, o2Y;
    double d, l;
    RoboticArmModel theModel;
    WebSocketServer server;
    public RoboticArmJNIServer(WebSocketServer server, double shoulder1X, double shoulder1Y, double shoulder2X, double shoulder2Y, double appendageLength) {
        o1X=shoulder1X;
        o1Y=shoulder1Y;
        o2X=shoulder2X;
        o2Y=shoulder2Y;
        d=absLength(o1X,o2X,o1Y,o2Y);
        l=appendageLength;
        theModel = new RoboticArmModel(o1X,o1Y,o2X,o2Y,l);
        this.server = server;
    }
    @Override
    public void setAngle(double theta1, double theta2) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("theta1",theta1);
            obj.put("theta2",theta2);
            server.server.getBroadcastOperations().sendEvent("setAngle",obj);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public RoboticArmModel getModel() {
        return theModel;
    }

    @Override
    public void setPenMode(boolean down) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("penMode",down);
            server.server.getBroadcastOperations().sendEvent("setPenMode",obj);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
