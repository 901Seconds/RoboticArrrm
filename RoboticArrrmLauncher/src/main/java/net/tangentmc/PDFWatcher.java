package net.tangentmc;

import io.socket.client.IO;
import io.socket.client.Socket;
import net.tangentmc.svg.SVGParser;
import net.tangentmc.util.DrawPoint;
import net.tangentmc.util.Utils;
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
            client = IO.socket("http://localhost:9092");
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
                if (System.currentTimeMillis()-lastmill < 1000) return;
                lastmill = System.currentTimeMillis();
                if (kind == OVERFLOW) {
                    continue;
                }

                Path filename = (Path) event.context();
                System.out.println("Client recieved print job");
                Path child = dir.resolve(filename);
                try {
                    Process p = Runtime.getRuntime().exec(exec+" "+child+" out.svg");
                    p.waitFor();
                    while (p.isAlive()) {
                        Thread.sleep(1);
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
                JSONObject obj = new JSONObject();
                Shape[] shapes = new SVGParser().shapesFromXML("out.svg");
                for (Shape shape: shapes) {
                    for (DrawPoint pt: Utils.getAllPoints(shape,1)) {
                        try {
                            obj.put("penDown",pt.isPenDown());
                            obj.put("x",pt.getX());
                            obj.put("y",pt.getY());
                            client.emit("drawPoint",obj);
                            Thread.sleep(1);
                        } catch (JSONException | InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
            boolean valid = key.reset();
            if (!valid) {
                break;
            }
        }
    }
    private String exec = "pdf2svg-windows-master"+File.separator+"dist-"+System.getProperty("sun.arch.data.model")+"bits"+File.separator+"pdf2svg.exe";
}
