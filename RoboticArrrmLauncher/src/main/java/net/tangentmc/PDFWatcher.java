package net.tangentmc;

import com.sanjay900.ProcessingRunner;
import io.socket.client.IO;
import io.socket.client.Socket;
import net.tangentmc.svg.SVGParser;
import net.tangentmc.util.DrawPoint;
import net.tangentmc.util.Utils;
import net.tangentmc.web.WebSocketServer;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.nio.file.StandardWatchEventKinds.*;
/**
 * Created by sanjay on 16/09/2016.
 */
public class PDFWatcher {
    Socket client;
    public static void main(String[] args) {
        try {
            new PDFWatcher();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private WatchService watcher;
    public PDFWatcher() throws IOException {
        watcher = FileSystems.getDefault().newWatchService();
        Path dir = new File("c:/print/").toPath();
        if(!dir.toFile().exists()) {
            System.out.print("Printer not set up.");
            return;
        }
        dir.register(watcher, ENTRY_MODIFY);
        new Thread(this::loop).start();
        try {
            client = IO.socket("http://10.140.59.43:9092");
            client.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
    private long lastmill = 0;
    private void loop() {
        Path dir = new File("c:/print/").toPath();
        for (;;) {

            // wait for key to be signaled
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                return;
            }

            for (WatchEvent<?> event: key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();
                if (System.currentTimeMillis()-lastmill < 3000) return;
                lastmill = System.currentTimeMillis();
                if (kind == OVERFLOW) {
                    continue;
                }
                Path filename = (Path) event.context();
                System.out.println("Client recieved print job");
                Path child = dir.resolve(filename);
                try {
                    Thread.sleep(1000);
                    Process p = Runtime.getRuntime().exec(exec+" "+child+" out.svg");
                    System.out.println(exec+" "+child+" out.svg");
                    p.waitFor();
                    //Wait for file to save
                    Thread.sleep(1000);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
                JSONObject obj = new JSONObject();
                Shape[] shapes = new SVGParser().shapesFromXML("out.svg");
                UUID shapeId;
                for (Shape shape: shapes) {
                    int idx = 0;
                    shapeId = UUID.randomUUID();
                    for (DrawPoint pt: Utils.getAllPoints(shape,1)) {
                        try {
                            obj.put("penDown",pt.isPenDown());
                            obj.put("x",pt.getX());
                            obj.put("y",pt.getY());
                            obj.put("index",idx);
                            obj.put("currentShape",shapeId.toString());
                            client.emit("drawPoint",obj);
                            Thread.sleep(10);
                        } catch (JSONException | InterruptedException e) {
                            e.printStackTrace();
                        }
                        idx++;
                    }

                    try {
                        obj.put("penDown",false);
                        obj.put("x",0d);
                        obj.put("y",0d);
                        obj.put("index", WebSocketServer.END);
                        obj.put("currentShape",shapeId.toString());
                        client.emit("drawPoint",obj);
                        Thread.sleep(10);
                    } catch (InterruptedException | JSONException e) {
                        e.printStackTrace();
                    }
                }

            }
            boolean valid = key.reset();
            if (!valid) {
                break;
            }
        }
    }
    private String exec = "pdf2svg-windows-master"+File.separator+"dist-32bits"+File.separator+"pdf2svg.exe";
}
