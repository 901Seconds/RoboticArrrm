package net.tangentmc;

import io.socket.client.IO;
import io.socket.client.Socket;
import net.tangentmc.svg.SVGParser;
import net.tangentmc.util.DrawPoint;
import net.tangentmc.util.Utils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlcml.pdf2svg.PDF2SVGConverter;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.UUID;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

public class PDFWatcher {
    private Socket client;
    public static void main(String[] args) {
        try {
            new PDFWatcher();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private WatchService watcher;
    PDFWatcher() throws IOException {
        watcher = FileSystems.getDefault().newWatchService();
        Path dir = new File("c:/print/").toPath();
        if(!dir.toFile().exists()) {
            System.out.print("Printer not set up.");
            return;
        }
        dir.register(watcher, ENTRY_MODIFY);
        new Thread(this::loop).start();
        try {
            client = IO.socket("http://10.140.133.175:9092"/*"http://localhost:9092"*/);
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
                System.out.println("Client received print job");
                Path child = dir.resolve(filename);
                PDF2SVGConverter converter = new PDF2SVGConverter();
                converter.run(child.toString());
                JSONObject obj;
                JSONArray points = new JSONArray();
                Shape[] shapes = new SVGParser().shapesFromXML("test.prn-page1.svg");
                UUID shapeId;
                int emitCount = 0;
                for (Shape shape: shapes) {
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
                        if (emitCount > 500) {
                            client.emit("drawPoints",points);
                            points = new JSONArray();
                            emitCount = 0;
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
                    client.emit("drawPoints",points);
                    points = new JSONArray();
                    emitCount = 0;
                }
                //File cleanup
                File[] files = new File(".").listFiles();
                if (files != null) {
                    Arrays.stream(files).filter(file -> file.getName().contains("test.prn")).forEach(File::delete);
                }
            }
            boolean valid = key.reset();
            if (!valid) {
                break;
            }
        }
    }
}
