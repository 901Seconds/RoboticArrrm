package net.tangentmc.web;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketConfig;
import com.corundumstudio.socketio.SocketIOServer;
import net.tangentmc.Launcher;
import net.tangentmc.util.DrawPoint;

public class WebSocketServer {
    public WebSocketServer(Launcher launcher) {
        Configuration config = new Configuration();
        config.setPort(9092);
        SocketConfig socketConfig = new SocketConfig();
        socketConfig.setReuseAddress(true);
        config.setSocketConfig(socketConfig);
        SocketIOServer server = new SocketIOServer(config);
        server.addEventListener("drawPoint",DrawPoint.class,(socketIOClient, drawPoint, ackRequest) -> launcher.addPoint(drawPoint));
        server.start();
    }
}
