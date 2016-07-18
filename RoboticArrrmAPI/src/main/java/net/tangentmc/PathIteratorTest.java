package net.tangentmc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.*;
import java.util.ArrayList;

public class PathIteratorTest {
    public static void main(String[] args) {
        JFrame frame = new JFrame("FlatteningPathIterator test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Shape s=new Ellipse2D.Float(10,10,200,100);
        Area a=new Area(new Ellipse2D.Float(150,20,100,200));
        a.add(new Area(s));
        PaintPanel app = new PaintPanel(a);
        JScrollPane scroll = new JScrollPane(app);
        frame.getContentPane().add(scroll);

        frame.setSize(400, 300);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    static class PaintPanel extends JPanel {
        FlatteningPathIterator iter;
        Shape shape;
        ArrayList<Point> points;
        int index=0;
        public PaintPanel(Shape s) {
            this.shape=s;
            iter=new FlatteningPathIterator(s.getPathIterator(new AffineTransform()), 0.001);
            points=new ArrayList<Point>();
            float[] coords=new float[6];
            while (!iter.isDone()) {
                iter.currentSegment(coords);
                int x=(int)coords[0];
                int y=(int)coords[1];
                points.add(new Point(x,y));
                iter.next();
            }

            Timer timer=new Timer(10, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    index++;
                    if (index>=points.size()) {
                        index=0;
                    }
                    repaint();
                }
            });
            timer.start();
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(Color.white);
            ((Graphics2D)g).draw(shape);
            g.setColor(Color.blue);
            Point p=points.get(index);
            g.fillOval(p.x, p.y, 5,5);
        }
    }
}