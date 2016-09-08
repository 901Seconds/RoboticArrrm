package net.tangentmc;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOServer;

/**
 * Created by sanjay on 9/09/2016.
 */
public class WebSocketServer {
    //Create a SocketIO Server that can listen for clients that want to interactivly draw.
    SocketIOServer server;
    public WebSocketServer(Launcher launcher) {
        Configuration config = new Configuration();
        config.setPort(9092);
        server = new SocketIOServer(config);
        //TODO: create some sort of client to test this.
        server.addEventListener("drawShape",DrawShape.class,(socketIOClient, drawShape, ackRequest) -> launcher.addShape(new Launcher.ShapeObject(drawShape)));
    }
}
