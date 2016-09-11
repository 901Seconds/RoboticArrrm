package net.tangentmc;

import ecs100.UI;
import ecs100.UIFileChooser;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.tangentmc.svg.SVGParser;
import net.tangentmc.util.AngleTuple;
import net.tangentmc.util.WebShape;
import net.tangentmc.util.Utils;
import net.tangentmc.web.WebServer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

public class Launcher {
    private BlockingQueue<ShapeObject> shapes = new LinkedBlockingQueue<>();
    private double xSpc;
    private double ySpc;
    private double scaleX = 1;
    private double scaleY = 1;
    private boolean left = false;
    private AffineTransform transform = new AffineTransform();
    private ArrayList<RoboticArm> arms = new ArrayList<>();

    public static void main(String[] args) {
        new Launcher();
    }

    private Launcher() {
        RoboticArmJNI robot = new RoboticArmJNI(new RoboticArmModel(287, 374, 377, 374, 154));
        RoboticArmSimulation armSimu = new RoboticArmSimulation(robot.getModel());
        arms.add(robot);
        arms.add(armSimu);
        UI.addButton("Pick SVG", this::load);
        UI.addButton("Clear", () -> {
            shapes.clear();
            current = null;
            armSimu.flagClear();
            draw();
        });
        UI.addSlider("Servo 0",1000,2000,1500,d -> robot.setServo(0,(int)d));
        UI.addSlider("Servo 1",1000,2000,1500,d -> robot.setServo(1,(int)d));
        UI.addButton("Simulate / Plot", () -> shapes.add(current));
        UI.setMouseMotionListener(this::mouseMove);
        ((JComponent) UI.theUI.canvas).addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mouseClicked(e);
                left = e.getButton() == MouseEvent.BUTTON1;
            }
        });
        try {
            robot.init();
            robot.setPenMode(true);
            armSimu.setPenMode(true);
        } catch (Exception e) {
            e.printStackTrace();
            UI.printMessage(e.getLocalizedMessage());
        }
        new WebServer(this);
        new Thread(this::plotThread).start();
    }

    private void plotThread() {
        //noinspection InfiniteLoopStatement
        while (true) {
            ArrayList<ArrayList<AngleTuple[]>> angleTuples = new ArrayList<>();
            AngleTuple temp;
            try {
                currentDrawing = shapes.take();
                if (current == currentDrawing)
                    current.applyTransformation(transform);
                current = null;
                draw();
                for (int i = 0; i < currentDrawing.getShapes().length; i++) {
                    ArrayList<Point.Double[]> points = Utils.getAllPoints(currentDrawing.getShapes()[i]);
                    angleTuples.addAll(arms.stream().map(arm -> Utils.getAllAngles(arm.getModel(), points)).collect(Collectors.toList()));
                    int maxShapes = angleTuples.stream().mapToInt(ArrayList::size).max().orElseGet(() -> 0);
                    int maxTuples = angleTuples.stream().mapToInt(tuple -> tuple.stream().mapToInt(t -> t.length).max().orElseGet(() -> 0)).max().orElseGet(() -> 0);
                    for (int i1 = 0; i1 < maxShapes; i1++) {
                        for (RoboticArm arm : arms) {
                           // arm.setPenMode(false);
                            if (i1 > angleTuples.get(arms.indexOf(arm)).size()) {
                                continue;
                            }
                            temp = angleTuples.get(arms.indexOf(arm)).get(i1)[0];
                            arm.setAngle(temp.getTheta1(), temp.getTheta2());
                        }
                        for (RoboticArm arm : arms) {
                           // arm.setPenMode(true);
                        }
                        for (int i2 = 0; i2 < maxTuples; i2++) {
                            for (RoboticArm arm : arms) {
                                if (i1 > angleTuples.get(arms.indexOf(arm)).size() || i2 > angleTuples.get(arms.indexOf(arm)).get(i1).length) {
                                    continue;
                                }
                                temp = angleTuples.get(arms.indexOf(arm)).get(i1)[i2];
                                arm.setAngle(temp.getTheta1(), temp.getTheta2());
                            }
                        }
                    }
                    currentDrawing.getShapes()[i] = null;
                    draw();
                }
                currentDrawing = null;
                draw();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private double lastX = -1, lastY = -1, width, height;

    private void mouseMove(String s, double x, double y) {
        if (s.equals("pressed")) {
            lastX = x;
            lastY = y;
        }
        if (s.equals("released")) {
            lastX = -1;
            lastY = -1;
        }
        if (lastX != -1) {
            if (left) {
                xSpc += x - lastX;
                ySpc += y - lastY;
            } else {
                scaleX += (x - lastX) / width;
                scaleY += (y - lastY) / height;
            }
            if (shapes != null) {
                draw();
            }
            lastX = x;
            lastY = y;
        }

    }

    private ShapeObject current;
    private ShapeObject currentDrawing;

    public void addShape(ShapeObject shapeObject) {
        if (current != null) {
            current.applyTransformation(transform);
        }
        shapes.add(shapeObject);
    }

    private void load() {
        String file = UIFileChooser.open("Pick an SVG file");
        if (file == null) return;

        xSpc = ySpc = 0;
        scaleX = scaleY = 1;
        if (current != null) {
            current.applyTransformation(transform);
        }
        current = new ShapeObject(new SVGParser().shapesFromXML(file));
        double lx = Double.MAX_VALUE;
        double ly = Double.MAX_VALUE;
        double ux = 0;
        double uy = 0;
        for (Shape s : current.shapes) {
            if (s.getBounds().getX() < lx) {
                lx = s.getBounds().getX();
            }
            if (s.getBounds().getY() < ly) {
                ly = s.getBounds().getY();
            }
            if (s.getBounds().getMaxX() > ux) {
                ux = s.getBounds().getMaxX();
            }
            if (s.getBounds().getMaxY() > uy) {
                uy = s.getBounds().getMaxY();
            }
        }
        lx = 10;
        ly = 10;
        width = ux - lx;
        height = uy - ly;
        draw();
    }

    private void draw() {
        UI.clearGraphics();
        shapes.forEach(this::drawShape);
        transform = new AffineTransform();
        transform.translate(xSpc, ySpc);
        transform.scale(scaleX, scaleY);
        if (current != null) {
            current.applyTransformation(transform);
            drawShape(current);
            try {
                current.applyTransformation(transform.createInverse());
            } catch (NoninvertibleTransformException ignored) {
            }
        }
        if (currentDrawing != null) {
            drawShape(currentDrawing);
        }
        UI.repaintAllGraphics();
    }

    private void drawShape(ShapeObject shape) {
        for (Shape s2 : shape.getShapes()) {
            if (s2 == null) continue;
            UI.getGraphics().draw(s2);
        }
    }


    @AllArgsConstructor
    @Getter
    @Setter
    public static class ShapeObject {
        //Scale web objects down
        private static int scale = 11;
        Shape[] shapes;

        public ShapeObject(WebShape shape) {
            Path2D path = new Path2D.Double();
            path.moveTo(shape.xpoints[0] / scale, shape.ypoints[0] / scale);
            for (int i = 1; i < shape.xpoints.length; i+=10) {
                path.lineTo(shape.xpoints[i] / scale, shape.ypoints[i] / scale);
            }
            shapes = new Shape[]{path};
        }

        void applyTransformation(AffineTransform transform) {
            for (int i = 0; i < shapes.length; i++) {
                shapes[i] = transform.createTransformedShape(shapes[i]);
            }
        }
    }
}
