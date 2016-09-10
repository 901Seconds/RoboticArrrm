package net.tangentmc;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class WebServer {

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/", new MyHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    private static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String request = t.getRequestURI().getPath().substring(1);
            if (request.isEmpty()) request = "index.html";
            if (!new File(request).exists()) request +=".html";
            if (!new File(request).exists()) request = "404.html";
            File file = new File(request);
            t.sendResponseHeaders(200, file.length());
            OutputStream outputStream=t.getResponseBody();
            Files.copy(file.toPath(), outputStream);
            outputStream.close();
        }
    }

}