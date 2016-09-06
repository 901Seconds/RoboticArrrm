package net.tangentmc;

import ecs100.UI;
import ecs100.UIFileChooser;
import processing.core.PApplet;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by sanjay on 5/09/2016.
 */
public class Launcher {
    Shape[] shapes;
    double xSpc;
    double ySpc;
    double scaleX = 1;
    double scaleY = 1;
    boolean left = false;
    double sliderTime = 10;
    AffineTransform transform = new AffineTransform();
    public static void main(String[] args) {
        new Launcher();
    }

    public Launcher() {
        UI.addButton("Pick SVG", this::load);
        UI.addButton("Simulate", this::simulate);
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
    }

    private void clearSim() {
        armSimu.flagClear();
    }

    RoboticArmSimulation armSimu;
    AtomicBoolean running = new AtomicBoolean(false);
    private void simulate() {
        running.set(false);
        AngleTuple last;
        if (armSimu == null)
            armSimu = new RoboticArmSimulation();
        ArrayList<AngleTuple[]> anglesFromShapes = new ArrayList<>();
        for (Shape shape : shapes) {
            ArrayList<Point.Double[]> points = Utils.getAllPoints(transform.createTransformedShape(shape));
            anglesFromShapes.addAll(Utils.getAllAngles(armSimu.getModel(), points));
        }
        running.set(true);
        last = anglesFromShapes.get(0)[0];
        for (AngleTuple[] angles : anglesFromShapes) {
            armSimu.setPenMode(false);
            interpBetween(last,angles[0]);
            last = angles[0];
            armSimu.setPenMode(true);
            for (AngleTuple angle : angles) {
                if (!running.get()) return;
                interpBetween(last,angles[0]);
                last = angle;
            }
        }

    }

    private void interpBetween(AngleTuple last, AngleTuple angle) {
        float t = 0;
        while (t < 1) {
            float theta1 = PApplet.lerp((float)last.getTheta1(),(float)angle.getTheta1(),t);
            float theta2 = PApplet.lerp((float)last.getTheta2(),(float)angle.getTheta2(),t);
            armSimu.setAngle(theta1, theta2);
            t+=0.1;
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

    private void load() {
        String file = UIFileChooser.open("Pick an SVG file");
        shapes = new SVGer().shapesFromXML(file);

        double lx = Double.MAX_VALUE;
        double ly = Double.MAX_VALUE;
        double ux = 0;
        double uy = 0;
        for (Shape s: shapes) {
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

    private void draw() {
        UI.getGraphics().setTransform(new AffineTransform());
        UI.clearGraphics();
        UI.getGraphics().translate(xSpc,ySpc);
        UI.getGraphics().scale(scaleX,scaleY);
        transform = new AffineTransform(UI.getGraphics().getTransform());
        for (Shape s: shapes) {
            UI.getGraphics().draw(s);
        }
        UI.repaintAllGraphics();
    }
}
