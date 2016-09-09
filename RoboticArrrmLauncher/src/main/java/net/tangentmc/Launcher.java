package net.tangentmc;

import ecs100.UI;
import ecs100.UIFileChooser;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import processing.core.PApplet;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by sanjay on 5/09/2016.
 */
public class Launcher {
    ArrayList<ShapeObject> shapes = new ArrayList<>();
    double xSpc;
    double ySpc;
    double scaleX = 1;
    double scaleY = 1;
    boolean left = false;
    double sliderTime = 10;
    AffineTransform transform = new AffineTransform();
    boolean drawTorobot;
    RoboticArmJNI robot;
    public static void main(String[] args) {
        new Launcher();
    }
    public Launcher() {
        robot = new RoboticArmJNI(287,374,377,374,154);
        armSimu = new RoboticArmSimulation(robot.getModel());
        UI.addButton("Pick SVG", this::load);
        UI.addButton("Clear", ()->{shapes.clear();current=null;draw();});
        UI.addButton("Simulate", ()->{drawTorobot=false;simulate();});
        UI.addButton("Robot", ()->{drawTorobot=true;simulate();});
        UI.setMouseMotionListener(this::mouseMove);
        UI.addSlider("Wait",0,10,s->sliderTime=s);
        UI.addButton("Clear Simulation",this::clearSim);
        ((JComponent) UI.theUI.canvas).addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mouseClicked(e);
                left = e.getButton() == MouseEvent.BUTTON1;
            }
        });
        new WebSocketServer(this);
    }

    private void clearSim() {
        armSimu.flagClear();
    }

    RoboticArmSimulation armSimu;
    AtomicBoolean running = new AtomicBoolean(false);
    private void simulate() {
        if (running.get()) return;
        running.set(true);
        AngleTuple last;
        ArrayList<AngleTuple[]> anglesFromShapesSimu;
        ArrayList<AngleTuple[]> anglesFromShapesBot = null;
        Iterator<ShapeObject> shapeObjectIterator = shapes.iterator();
        ShapeObject shapeObject;
        while (shapeObjectIterator.hasNext()) {
            shapeObject = shapeObjectIterator.next();
            current.applyTransformation(transform);
            for (int i = 0; i < shapeObject.getShapes().length; i++) {
                ArrayList<Point.Double[]> points = Utils.getAllPoints(shapeObject.getShapes()[i]);
                anglesFromShapesSimu = Utils.getAllAngles(armSimu.getModel(), points);
                if (robot != null && drawTorobot)
                    anglesFromShapesBot = Utils.getAllAngles(robot.getModel(), points);
                draw();
                last = anglesFromShapesSimu.get(0)[0];
                for (int i1 = 0; i1 < anglesFromShapesSimu.size(); i1++) {
                    armSimu.setPenMode(false);
                    interpBetween(last, anglesFromShapesSimu.get(i1)[0]);
                    last = anglesFromShapesSimu.get(i1)[0];
                    armSimu.setPenMode(true);
                    for (int i2 = 0; i2 < anglesFromShapesSimu.get(i1).length; i2++) {
                        if (anglesFromShapesBot != null)
                            robot.setAngle(anglesFromShapesBot.get(i1)[i2].theta1, anglesFromShapesBot.get(i1)[i2].theta2);
                        interpBetween(last, anglesFromShapesSimu.get(i1)[i2]);
                        last = anglesFromShapesSimu.get(i1)[i2];
                    }
                }
                shapeObject.getShapes()[i] = null;
            }
            shapeObjectIterator.remove();
            draw();
        }
        running.set(false);
    }

    private void interpBetween(AngleTuple last, AngleTuple angle) {
        float t = 0;
        while (t < 1) {
            float theta1 = PApplet.lerp((float)last.getTheta1(),(float)angle.getTheta1(),t);
            float theta2 = PApplet.lerp((float)last.getTheta2(),(float)angle.getTheta2(),t);
            armSimu.setAngle(theta1, theta2);
            t+=0.5;
            try {
                Thread.sleep((long) sliderTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    double lastX = -1,lastY = -1;
    double width,height;
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
                xSpc += x-lastX;
                ySpc += y-lastY;
            } else {
                scaleX += (x-lastX)/width;
                scaleY += (y-lastY)/height;
            }
            if (shapes != null) {
                draw();
            }
            lastX = x;
            lastY = y;
        }

    }
    ShapeObject current;
    public void addShape(ShapeObject shapeObject) {
        if (current != null) {
            current.applyTransformation(transform);
        }
        xSpc = ySpc = 0;
        scaleX = scaleY = 1;
        shapes.add(current=shapeObject);

        double lx = Double.MAX_VALUE;
        double ly = Double.MAX_VALUE;
        double ux = 0;
        double uy = 0;
        for (Shape s: current.shapes) {
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
        width = ux-lx;
        height = uy-ly;
        draw();
    }
    private void load() {
        if (running.get()) {
            JOptionPane.showMessageDialog(null, "You can't add SVG files to the canvas while drawing.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String file = UIFileChooser.open("Pick an SVG file");
        addShape(new ShapeObject(new SVGer().shapesFromXML(file)));
    }

    private void draw() {
        UI.getGraphics().setTransform(new AffineTransform());
        UI.clearGraphics();
        for (ShapeObject s: shapes) {
            if (s == current) continue;
            for (Shape s2 : s.getShapes()) {
                if (s2 == null) continue;
                UI.getGraphics().draw(s2);
            }
        }
        UI.getGraphics().translate(xSpc,ySpc);
        UI.getGraphics().scale(scaleX,scaleY);
        transform = new AffineTransform(UI.getGraphics().getTransform());
        if (current != null) {
            for (Shape s2 : current.getShapes()) {
                if (s2 == null) continue;
                UI.getGraphics().draw(s2);
            }
        }
        UI.repaintAllGraphics();
    }

    @AllArgsConstructor
    @Getter
    @Setter
    static class ShapeObject {
        Shape[] shapes;
        ShapeObject(DrawShape shape) {
            Path2D path = new Path2D.Double();
            path.moveTo(shape.xpoints[0],shape.ypoints[0]);
            for (int i = 1; i < shape.xpoints.length; i++) {
                path.lineTo(shape.xpoints[i],shape.ypoints[i]);
            }
            shapes = new Shape[]{path};
        }

        public void applyTransformation(AffineTransform transform) {
            for (int i = 0; i < shapes.length; i++) {
                shapes[i] = transform.createTransformedShape(shapes[i]);
            }
        }
    }
}
