package net.tangentmc.web;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOServer;
import net.tangentmc.Launcher;
import net.tangentmc.util.WebShape;

public class WebSocketServer {
    int test = 0;
    public WebSocketServer(Launcher launcher) {
        Configuration config = new Configuration();
        config.setPort(9092);
        SocketIOServer server = new SocketIOServer(config);
        server.addEventListener("drawShape",WebShape.class,(socketIOClient, drawShape, ackRequest) -> {
            test++;
            if (test % 10 == 0) {
                launcher.addShape(new Launcher.ShapeObject(drawShape));
            }
        });
        server.start();
    }
}
