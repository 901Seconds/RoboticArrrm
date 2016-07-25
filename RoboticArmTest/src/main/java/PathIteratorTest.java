import net.tangentmc.InterpolatedPathIterator;
import net.tangentmc.SVGer;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;

public class PathIteratorTest {
    public static void main(String[] args) {
        JFrame frame = new JFrame("FlatteningPathIterator test");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        Shape[] shapes = new SVGer().shapesFromXML("file.svg");
        Shape s=shapes[0];
        PaintPanel app = new PaintPanel(s);
        JScrollPane scroll = new JScrollPane(app);
        frame.getContentPane().add(scroll);

        frame.setSize(400, 300);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    static class PaintPanel extends JPanel {
        PathIterator iter;
        Shape shape;
        ArrayList<Point> points;
        int index=0;
        public PaintPanel(Shape s) {
            setBackground(new Color(0,0,0));
            this.shape=s;
            iter=new InterpolatedPathIterator(s.getPathIterator(new AffineTransform()), 0.1);
            points= new ArrayList<>();
            float[] coords=new float[6];
            while (!iter.isDone()) {
                iter.currentSegment(coords);
                int x=(int)coords[0];
                int y=(int)coords[1];
                points.add(new Point(x,y));
                iter.next();
            }

            Timer timer=new Timer(50, e -> {
                index++;
                if (index>=points.size()) {
                    index=0;
                }
                repaint();
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