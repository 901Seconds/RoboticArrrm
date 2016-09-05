package net.tangentmc;

import ecs100.UI;
import ecs100.UIFileChooser;
import javafx.scene.transform.Affine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;

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
    AffineTransform transform = new AffineTransform();
    public static void main(String[] args) {
        new Launcher();
    }

    public Launcher() {
        UI.addButton("Pick SVG", this::load);
        UI.addButton("Simulate", this::simulate);
        UI.setMouseMotionListener(this::mouseMove);
        ((JComponent) UI.theUI.canvas).addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mouseClicked(e);
                left = e.getButton() == MouseEvent.BUTTON1;
            }
        });
    }

    RoboticArm armSimu;

    private void simulate() {
        if (armSimu == null)
            armSimu = new RoboticArmSimulation();
        ArrayList<AngleTuple[]> angles = new ArrayList<>();
        for (int i = 0; i < shapes.length; i++) {
           ArrayList<Point.Double[]> points = Utils.getAllPoints(transform.createTransformedShape(shapes[i]));
            angles.addAll(Utils.getAllAngles(armSimu.getModel(), points));
        }
        for (int i = 0; i < angles.size(); i++) {
            armSimu.setAngle(angles.get(i)[0].getTheta1(), angles.get(i)[0].getTheta2());
            armSimu.setPenMode(true);

            for (int i2 = 0; i2 < angles.get(i).length; i2++) {
                armSimu.setAngle(angles.get(i)[i2].getTheta1(), angles.get(i)[i2].getTheta2());
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            armSimu.setPenMode(false);
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
        UI.clearGraphics();
        UI.getGraphics().translate(xSpc,ySpc);
        UI.getGraphics().scale(scaleX,scaleY);
        transform = new AffineTransform(UI.getGraphics().getTransform());
        for (Shape s: shapes) {
            UI.getGraphics().draw(s);
        }
        UI.getGraphics().scale(1/scaleX,1/scaleY);
        UI.getGraphics().translate(-xSpc,-ySpc);
        UI.repaintAllGraphics();
    }
}
