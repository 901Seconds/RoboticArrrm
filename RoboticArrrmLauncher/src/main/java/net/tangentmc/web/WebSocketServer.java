package net.tangentmc.web;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketConfig;
import com.corundumstudio.socketio.SocketIOServer;
import net.tangentmc.Launcher;
import net.tangentmc.util.DrawPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class WebSocketServer {

    public WebSocketServer(Launcher launcher) {
        HashMap<UUID,HashMap<UUID,ArrayList<DrawPoint>>> userPoints = new HashMap<>();
        Configuration config = new Configuration();
        config.setPort(9092);
        SocketConfig socketConfig = new SocketConfig();
        socketConfig.setReuseAddress(true);
        config.setSocketConfig(socketConfig);
        SocketIOServer server = new SocketIOServer(config);
        server.addEventListener("drawPoint",DrawPoint.class,(socketIOClient, drawPoint, ackRequest) -> {
            if (drawPoint.getIndex() == END) {
                launcher.addPoints(userPoints.get(socketIOClient.getSessionId()).get(UUID.fromString(drawPoint.getCurrentShape())));
                return;
            }
            userPoints.putIfAbsent(socketIOClient.getSessionId(),new HashMap<>());
            userPoints.get(socketIOClient.getSessionId()).putIfAbsent(UUID.fromString(drawPoint.getCurrentShape()),new ArrayList<>());
            userPoints.get(socketIOClient.getSessionId()).get(UUID.fromString(drawPoint.getCurrentShape())).add(drawPoint);


        });
        server.start();
    }
    public static final int END = -1;
}
