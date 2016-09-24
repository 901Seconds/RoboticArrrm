package net.tangentmc;

import com.sanjay900.ProcessingRunner;
import ecs100.UI;
import io.socket.client.IO;
import io.socket.client.Socket;
import net.tangentmc.svg.SVGParser;
import net.tangentmc.util.DrawPoint;
import net.tangentmc.util.Utils;
import net.tangentmc.web.WebSocketServer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlcml.pdf2svg.PDF2SVGConverter;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

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
            client = IO.socket(/*"http://10.140.63.68:9092"*/"http://localhost:9092");
            client.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
    private long lastmill = 0;
    @SuppressWarnings("Duplicates")
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
                if (System.currentTimeMillis()-lastmill > 5000) {
                    lastmill = System.currentTimeMillis();
                    continue;
                }
                lastmill = System.currentTimeMillis();
                if (kind == OVERFLOW) {
                    continue;
                }
                Path filename = (Path) event.context();
                System.out.println("Client recieved print job");
                Path child = dir.resolve(filename);
                PDF2SVGConverter converter = new PDF2SVGConverter();
                try {
                    Thread.sleep(100);
                    converter.run(child.toString());
                } catch (Exception e) {
                    continue;
                }
                JSONObject obj;
                JSONArray points = new JSONArray();
                Shape[] shapes = new SVGParser().shapesFromXML("test.prn-page1.svg");
                UUID shapeId;
                int emitCount = 0;
                for (Shape shape: shapes) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    int idx = 0;
                    shapeId = UUID.randomUUID();
                    for (DrawPoint pt: Utils.getAllPoints(shape,Launcher.LINE_MIN_DIST)) {
                        try {
                            obj = new JSONObject();
                            obj.put("penDown",pt.isPenDown());
                            obj.put("x",pt.getX());
                            obj.put("y",pt.getY());
                            obj.put("index",idx);
                            obj.put("currentShape",shapeId.toString());
                            points.put(obj);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        emitCount++;
                        if (emitCount > 100) {
                            client.emit("drawPoints",points);
                            points = new JSONArray();
                            emitCount = 0;
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        idx++;
                    }

                    try {
                        obj = new JSONObject();
                        obj.put("penDown",false);
                        obj.put("x",0d);
                        obj.put("y",0d);
                        obj.put("index", -idx);
                        obj.put("currentShape",shapeId.toString());
                        points.put(obj);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    emitCount++;
                    if (emitCount > 100) {
                        client.emit("drawPoints",points);
                        points = new JSONArray();
                        emitCount = 0;
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                client.emit("drawPoints",points);

            }
            boolean valid = key.reset();
            if (!valid) {
                break;
            }
        }
    }
}
