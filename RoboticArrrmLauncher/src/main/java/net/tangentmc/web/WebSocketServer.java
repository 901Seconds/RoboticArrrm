package net.tangentmc.web;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketConfig;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import net.tangentmc.Launcher;
import net.tangentmc.util.DrawPoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

public class WebSocketServer {

    HashMap<UUID,HashMap<UUID,ArrayList<DrawPoint>>> userPoints = new HashMap<>();
    HashMap<UUID,HashMap<UUID,Integer>> sizes = new HashMap<>();
    public WebSocketServer(Launcher launcher) {
        Configuration config = new Configuration();
        config.setPort(9092);
        SocketConfig socketConfig = new SocketConfig();
        socketConfig.setReuseAddress(true);
        config.setSocketConfig(socketConfig);
        SocketIOServer server = new SocketIOServer(config);
        server.addEventListener("drawPoint",DrawPoint.class,(socketIOClient, drawPoint, ackRequest) ->{
            launcher.addPoint(drawPoint);
        });
        server.addEventListener("drawPoints",DrawPoint[].class,(socketIOClient, drawPoints, ackRequest) -> {
            Arrays.stream(drawPoints).forEach(drawPoint -> drawPoint(drawPoint,launcher,socketIOClient));
        });
        server.start();
    }
    private void drawPoint(DrawPoint drawPoint, Launcher launcher, SocketIOClient socketIOClient) {
        if (drawPoint.getIndex() < 0) {
            sizes.putIfAbsent(socketIOClient.getSessionId(),new HashMap<>());
            sizes.get(socketIOClient.getSessionId()).put(UUID.fromString(drawPoint.getCurrentShape()),-drawPoint.getIndex());
            checkPoints(launcher);
            return;
        }
        userPoints.putIfAbsent(socketIOClient.getSessionId(),new HashMap<>());
        userPoints.get(socketIOClient.getSessionId()).putIfAbsent(UUID.fromString(drawPoint.getCurrentShape()),new ArrayList<>());
        userPoints.get(socketIOClient.getSessionId()).get(UUID.fromString(drawPoint.getCurrentShape())).add(drawPoint);
        checkPoints(launcher);

    }

    private void checkPoints(Launcher launcher) {
        for (UUID uuid:sizes.keySet()) {
            sizes.get(uuid).keySet().stream().filter(shape -> userPoints.get(uuid).get(shape).size() >= sizes.get(uuid).get(shape) - 1).forEach(shape -> {
                launcher.addPoints(userPoints.get(uuid).get(shape));
                sizes.get(uuid).remove(shape);
                userPoints.get(uuid).remove(shape);
            });
        }
    }
    public static final int END = -1;
}
