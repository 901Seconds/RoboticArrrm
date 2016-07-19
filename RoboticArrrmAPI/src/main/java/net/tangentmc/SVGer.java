package net.tangentmc;

import org.apache.batik.parser.AWTPathProducer;
import org.apache.batik.parser.ParseException;
import org.apache.batik.parser.PathParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.awt.font.GlyphVector;
import java.awt.geom.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by surface on 18/07/2016.
 */
public class SVGer {
    public static Shape parsePathShape(String svgPathShape) {
        try {
            AWTPathProducer pathProducer = new AWTPathProducer();
            PathParser pathParser = new PathParser();
            pathParser.setPathHandler(pathProducer);
            pathParser.parse(svgPathShape);
            return pathProducer.getShape();
        } catch (ParseException ex) {
            // Fallback to default square shape if shape is incorrect
            return new Rectangle2D.Float(0, 0, 1, 1);
        }
    }


    public Shape[] pointsFromXML(String fileName) {
        ArrayList<Shape> shapes = new ArrayList<>();
        File opened = new File(fileName);
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(opened);

            NodeList pathList = doc.getElementsByTagName("path");
            for (int i = 0; i < pathList.getLength(); i++) {
                org.w3c.dom.Node p = pathList.item(i);
                if (p.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                    Element path = (Element) p;
                    String d = path.getAttribute("d");
                    shapes.add(parsePathShape(d));
                }
            }
            pathList = doc.getElementsByTagName("rect");
            for (int i = 0; i < pathList.getLength(); i++) {
                org.w3c.dom.Node p = pathList.item(i);
                if (p.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                    Element path = (Element) p;
                    double width = Double.parseDouble(path.getAttribute("width"));
                    double height = Double.parseDouble(path.getAttribute("height"));
                    double x=0,y=0;
                    if (path.hasAttribute("x"))
                        x = Double.parseDouble(path.getAttribute("x"));
                    if (path.hasAttribute("y"))
                        y = Double.parseDouble(path.getAttribute("y"));

                    if (path.hasAttribute("rx")) {
                        double rx = Double.parseDouble(path.getAttribute("rx"));
                        double ry = Double.parseDouble(path.getAttribute("ry"));
                        shapes.add(new RoundRectangle2D.Double(x,y,width,height,rx,ry));
                    } else {
                        shapes.add(new Rectangle2D.Double(x,y,width,height));
                    }
                }
            }
            pathList = doc.getElementsByTagName("circle");
            for (int i = 0; i < pathList.getLength(); i++) {
                org.w3c.dom.Node p = pathList.item(i);
                if (p.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                    Element path = (Element) p;
                    double r = Double.parseDouble(path.getAttribute("r"));
                    double x=0,y=0;
                    if (path.hasAttribute("cx"))
                        x = Double.parseDouble(path.getAttribute("cx"));
                    if (path.hasAttribute("cy"))
                        y = Double.parseDouble(path.getAttribute("cy"));
                        shapes.add(new Ellipse2D.Double(x,y,r,r));

                }
            }
            pathList = doc.getElementsByTagName("ellipse");
            for (int i = 0; i < pathList.getLength(); i++) {
                org.w3c.dom.Node p = pathList.item(i);
                if (p.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                    Element path = (Element) p;
                    double rx = Double.parseDouble(path.getAttribute("rx"));
                    double ry = Double.parseDouble(path.getAttribute("ry"));
                    double x=0,y=0;
                    if (path.hasAttribute("cx"))
                        x = Double.parseDouble(path.getAttribute("cx"));
                    if (path.hasAttribute("cy"))
                        y = Double.parseDouble(path.getAttribute("cy"));
                    shapes.add(new Ellipse2D.Double(x,y,rx,ry));

                }
            }
            pathList = doc.getElementsByTagName("line");
            for (int i = 0; i < pathList.getLength(); i++) {
                org.w3c.dom.Node p = pathList.item(i);
                if (p.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                    Element path = (Element) p;
                    double x1 = Double.parseDouble(path.getAttribute("x1"));
                    double y1 = Double.parseDouble(path.getAttribute("y1"));
                    double x2 = Double.parseDouble(path.getAttribute("x2"));
                    double y2 = Double.parseDouble(path.getAttribute("y2"));
                    shapes.add(new Line2D.Double(x1,y1,x2,y2));

                }
            }
            pathList = doc.getElementsByTagName("polygon");
            for (int i = 0; i < pathList.getLength(); i++) {
                org.w3c.dom.Node p = pathList.item(i);
                if (p.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                    Element path = (Element) p;
                    String[] points = path.getAttribute("points").split(" ");
                    Path2D path2d = new Path2D.Double();
                    path2d.moveTo(getPoints(points[0]).getX(),getPoints(points[0]).getY());
                    for (int i1 = 1; i1 < points.length; i1++) {
                        path2d.lineTo(getPoints(points[i]).getX(),getPoints(points[i]).getY());
                    }
                    path2d.closePath();
                    shapes.add(path2d);
                }
            }
            pathList = doc.getElementsByTagName("polyline");
            for (int i = 0; i < pathList.getLength(); i++) {
                org.w3c.dom.Node p = pathList.item(i);
                if (p.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                    Element path = (Element) p;
                    String[] points = path.getAttribute("points").split(" ");
                    Path2D path2d = new Path2D.Double();
                    path2d.moveTo(getPoints(points[0]).getX(),getPoints(points[0]).getY());
                    for (int i1 = 1; i1 < points.length; i1++) {
                        path2d.lineTo(getPoints(points[i]).getX(),getPoints(points[i]).getY());
                    }
                    shapes.add(path2d);
                }
            }
            pathList = doc.getElementsByTagName("text");
            for (int i = 0; i < pathList.getLength(); i++) {
                org.w3c.dom.Node p = pathList.item(i);
                if (p.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                    Element path = (Element) p;
                    double x=0,y=0;
                    if (path.hasAttribute("x"))
                        x = Double.parseDouble(path.getAttribute("x"));
                    if (path.hasAttribute("y"))
                        y = Double.parseDouble(path.getAttribute("y"));
                    Font f = new JLabel().getFont();
                    GlyphVector v = f.createGlyphVector(new Canvas().getFontMetrics(f).getFontRenderContext(), path.getTextContent());
                    shapes.add(v.getOutline((float)x,(float)y));
                }
            }


        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        return shapes.toArray(new Shape[0]);
    }

    private Point2D.Double getPoints(String point) {
        String[] split = point.split(",");
        return new Point2D.Double(Double.parseDouble(split[0]),Double.parseDouble(split[1]));
    }

}
