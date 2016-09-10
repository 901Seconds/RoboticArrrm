package net.tangentmc.web;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import net.tangentmc.Launcher;

public class WebServer {
    public WebServer(Launcher launcher) {
        try {
            new WebSocketServer(launcher);
            HttpServer server = HttpServer.create(new InetSocketAddress(80), 0);
            server.createContext("/", new FileSystemHandler());
            server.setExecutor(null); // creates a default executor
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static class FileSystemHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String request = t.getRequestURI().getPath().substring(1);
            if (request.isEmpty()) request = "index.html";
            if (!new File(request).exists()) request +=".html";
            if (!new File(request).exists()) request = "404.html";
            File file = new File(request);
            t.sendResponseHeaders(200, file.length());
            OutputStream outputStream = t.getResponseBody();
            Files.copy(file.toPath(), outputStream);
            outputStream.close();
        }
    }

}