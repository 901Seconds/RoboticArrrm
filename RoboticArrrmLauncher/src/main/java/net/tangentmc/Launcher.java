package net.tangentmc;

import ecs100.UI;
import ecs100.UIFileChooser;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.tangentmc.svg.SVGParser;
import net.tangentmc.util.AngleTuple;
import net.tangentmc.util.DrawPoint;
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

public class Launcher {
    private static final double WEB_SCALE_FACTOR = 0.5;

    private BlockingQueue<DrawPoint> pointsToDraw = new LinkedBlockingQueue<>();
    private boolean left = false;
    private ArrayList<RoboticArm> arms = new ArrayList<>();
    private ShapeObject current;
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
            pointsToDraw.clear();
            current = null;
            armSimu.flagClear();
            draw();
        });
        UI.addSlider("Servo 0",1000,2000,1500,d -> robot.setServo(0,(int)d));
        UI.addSlider("Servo 1",1000,2000,1500,d -> robot.setServo(1,(int)d));
        UI.addButton("Simulate / Plot", () -> addShape(current));
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
            try {
                DrawPoint pt = pointsToDraw.take();
                draw();
                for (RoboticArm arm: arms) {
                    AngleTuple tuple = Utils.convertPoint(arm.getModel(),pt);
                    arm.setPenMode(tuple.isPenDown());
                    arm.setAngle(tuple.getTheta1(),tuple.getTheta2());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    //variables used for mouse manipulation
    private double lastX = -1, lastY = -1, scaleX = 1, scaleY = 1, width, height, xSpc, ySpc;
    private AffineTransform transform = new AffineTransform();
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
            if (current != null) {
                draw();
            }
            lastX = x;
            lastY = y;
        }

    }

    private void load() {
        String file = UIFileChooser.open("Pick an SVG file");
        if (file == null) return;

        xSpc = ySpc = 0;
        scaleX = scaleY = 1;
        if (current != null) {
            addShape(current);
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
        if (!pointsToDraw.isEmpty()) {
            Path2D path = new Path2D.Float();
            path.moveTo(pointsToDraw.peek().getX(), pointsToDraw.peek().getY());
            for (DrawPoint point : pointsToDraw) {
                if (point.isPenDown()) path.lineTo(point.getX(), point.getY());
                else path.moveTo(point.getX(), point.getY());
            }
            UI.getGraphics().draw(path);
        }
        transform = new AffineTransform();
        transform.translate(xSpc, ySpc);
        transform.scale(scaleX, scaleY);
        if (current != null) {
            for (Shape s2 : current.getShapes()) {
                if (s2 == null) continue;
                UI.getGraphics().draw(transform.createTransformedShape(s2));
            }
        }
        UI.repaintAllGraphics();
    }

    public void addPoint(DrawPoint drawPoint) {
        drawPoint.scale(WEB_SCALE_FACTOR);
        pointsToDraw.add(drawPoint);
    }

    private void addShape(ShapeObject shape) {
        for (Shape shapes: shape.getShapes()) {
            if (shape == current) shapes = transform.createTransformedShape(shapes);
            pointsToDraw.addAll(Utils.getAllPoints(shapes));
        }
        current = null;
    }

    @AllArgsConstructor
    @Getter
    @Setter
    private static class ShapeObject {
        Shape[] shapes;
    }
}
