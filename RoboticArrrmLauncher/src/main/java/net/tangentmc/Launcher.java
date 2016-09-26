package net.tangentmc;

import ecs100.UI;
import ecs100.UIFileChooser;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.tangentmc.svg.SVGParser;
import net.tangentmc.util.Angle;
import net.tangentmc.util.DrawPoint;
import net.tangentmc.util.Utils;
import net.tangentmc.web.WebServer;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class Launcher {
    //Min distance between two points
    static final double LINE_MIN_DIST = 1;
    private BlockingQueue<DrawPoint> pointsToDraw = new LinkedBlockingQueue<>();
    private boolean left = false;
    private ArrayList<RoboticArm> arms = new ArrayList<>();
    private ShapeObject current;
    private boolean robotAlive = true;
    public static void main(String[] args) {
        new Launcher();
    }

    private Launcher() {
        disableLogger();
        UI.setImmediateRepaint(false);
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
        UI.addButton("Pen Up", () -> robot.setPenMode(false));
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
        } catch (Exception e) {
            e.printStackTrace();
            UI.printMessage(e.getLocalizedMessage());
            robotAlive = false;
        }
        new WebServer(this);
        new Thread(this::plotThread).start();
        try {
            new PDFWatcher();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private void disableLogger() {
        ArrayList<Logger> loggers = Collections.<Logger>list(LogManager.getCurrentLoggers());
        loggers.add(LogManager.getRootLogger());
        for ( Logger logger : loggers ) {
            logger.setLevel(Level.INFO);
        }
    }

    private DrawPoint last;
    private void plotThread() {
        //noinspection InfiniteLoopStatement
        while (true) {
            try {
                DrawPoint cpt = pointsToDraw.take();
                DrawPoint tmpPoint;
                if(last == null) {
                    last = cpt.cpy();
                    //Make sure to trigger pen down events.
                    last.setPenDown(!cpt.isPenDown());
                }
                double dist = last.dist(cpt);
                if (last.isPenDown() != cpt.isPenDown()) {
                    arms.forEach(arm -> {
                        arm.setPenMode(cpt.isPenDown());
                        UI.sleep(robotAlive?150:10);
                    });
                }
                draw();
                if (dist > LINE_MIN_DIST && cpt.isPenDown()) {
                    for (double t = 0; t < 1; t+=LINE_MIN_DIST/dist) {
                        tmpPoint = new DrawPoint(Utils.lerp(t,last.getX(),cpt.getX()),
                                Utils.lerp(t,last.getY(),cpt.getY()),
                                cpt.isPenDown(),0,null);
                        setAngles(tmpPoint);
                    }
                } else {
                    setAngles(cpt);
                }
                last = cpt.cpy();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    private void setAngles(DrawPoint point) {
        for (RoboticArm arm : arms) {
            Angle tuple = Utils.convertPoint(arm.getModel(),point);
            arm.setAngle(tuple.getTheta1(), tuple.getTheta2());
        }
        UI.sleep(robotAlive?200:50);
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
            lx=Math.min(lx,s.getBounds().getX());
            ly=Math.min(ly,s.getBounds().getY());
            ux=Math.max(ux,s.getBounds().getMaxX());
            uy=Math.max(uy,s.getBounds().getMaxY());
        }
        lx = 10;
        ly = 10;
        width = ux - lx;
        height = uy - ly;
        draw();
    }
    private AtomicBoolean stopDraw = new AtomicBoolean(false);
    private void draw() {
        stopDraw.set(true);
        SwingUtilities.invokeLater(()-> {
            stopDraw.set(false);
            if (!pointsToDraw.isEmpty()) {
                Path2D path = new Path2D.Float();
                path.moveTo(pointsToDraw.iterator().next().getX(), pointsToDraw.iterator().next().getY());
                for (DrawPoint point : pointsToDraw) {
                    if (stopDraw.get()) return;
                    if (point.isPenDown()) path.lineTo(point.getX(), point.getY());
                    else path.moveTo(point.getX(), point.getY());
                }

                UI.clearGraphics();
                UI.getGraphics().draw(path);
            }
            if (current != null) {
                UI.clearGraphics();
                transform = new AffineTransform();
                transform.translate(xSpc, ySpc);
                transform.scale(scaleX, scaleY);
                for (Shape s2 : current.getShapes()) {
                    if (stopDraw.get()) return;
                    if (s2 == null) continue;
                    UI.getGraphics().draw(transform.createTransformedShape(s2));
                }
            }
            UI.repaintAllGraphics();
        });
    }

    public void addPoint(DrawPoint drawPoint) {
        pointsToDraw.add(drawPoint);
        draw();
    }

    private void addShape(ShapeObject shape) {
        for (Shape shapes: shape.getShapes()) {
            if (shape == current) shapes = transform.createTransformedShape(shapes);
            pointsToDraw.addAll(Utils.getAllPoints(shapes,LINE_MIN_DIST));
        }
        current = null;
    }

    public void addPoints(ArrayList<DrawPoint> drawPoints) {
        Collections.sort(drawPoints, (o1, o2) -> o1.getIndex()-o2.getIndex());
        DrawPoint pt = drawPoints.get(0).cpy();
        pt.setPenDown(false);
        addPoint(pt);
        drawPoints.forEach(pointsToDraw::add);
        draw();
    }

    @AllArgsConstructor
    @Getter
    @Setter
    private static class ShapeObject {
        Shape[] shapes;
    }
}
