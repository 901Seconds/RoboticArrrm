package net.tangentmc.svg;

import org.apache.batik.parser.AWTPathProducer;
import org.apache.batik.parser.ParseException;
import org.apache.batik.parser.PathParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class SVGParser {
    private static Shape parsePathShape(String svgPathShape) {
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


    public Shape[] shapesFromXML(String fileName) {
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
                    double width = getAttrib("width",path);
                    double height = getAttrib("height",path);
                    double x = getAttrib("x",path);
                    double y = getAttrib("y",path);
                    if (path.hasAttribute("rx")) {
                        double rx = getAttrib("rx",path);
                        double ry = getAttrib("ry",path);
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
                    double r = getAttrib("r",path);
                    double x = getAttrib("cx",path);
                    double y = getAttrib("cy",path);
                    shapes.add(new Ellipse2D.Double(x,y,r,r));

                }
            }
            pathList = doc.getElementsByTagName("ellipse");
            for (int i = 0; i < pathList.getLength(); i++) {
                org.w3c.dom.Node p = pathList.item(i);
                if (p.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                    Element path = (Element) p;
                    double rx = getAttrib("rx",path);
                    double ry = getAttrib("ry",path);
                    double x = getAttrib("cx",path);
                    double y = getAttrib("cy",path);
                    shapes.add(new Ellipse2D.Double(x,y,rx,ry));

                }
            }
            pathList = doc.getElementsByTagName("line");
            for (int i = 0; i < pathList.getLength(); i++) {
                org.w3c.dom.Node p = pathList.item(i);
                if (p.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                    Element path = (Element) p;
                    double x1 = getAttrib("x1",path);
                    double y1 = getAttrib("y1",path);
                    double x2 = getAttrib("x2",path);
                    double y2 = getAttrib("y2",path);
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
                    double x = getAttrib("x",path);
                    double y = getAttrib("y",path);
                    Font parentfont = parseStyle(path,null);
                    Font font;
                    if (path.hasChildNodes()) {
                        Element tPath = path;
                        for (int i1 = 0; i1 < tPath.getChildNodes().getLength(); i1++) {
                            path = (Element) tPath.getChildNodes().item(i1);
                            if (path.getTextContent().isEmpty()) continue;
                            font = parseStyle(path,parentfont);
                            shapes.add(getTextShape(path.getTextContent(), font, x, y));
                        }
                    }
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        return shapes.toArray(new Shape[0]);

    }
    private Font parseStyle(Element path, Font font) {
        double size = font==null?10:font.getSize();
        String fontFamily = font==null?null:font.getFamily();
        if (path.hasAttribute("style")) {
            String styleAttribs = path.getAttribute("style");
            for (String s : styleAttribs.split(";")) {
                if (s.contains("font-size")) {
                    size = Double.parseDouble(s.replace("font-size:", "").replace("px", ""));
                }
                if (s.contains("font-family")) {
                    fontFamily = s.replace("font-family:", "").replace("\'", "");
                }
            }
        }
        return new Font(fontFamily, Font.PLAIN, (int)size);
    }
    private double getAttrib(String attrib, Element path) {
        if (path.hasAttribute(attrib))
            return Double.parseDouble(path.getAttribute(attrib));
        return 0;
    }
    private Shape getTextShape(String str, Font font, double x, double y) {
        BufferedImage bufferImage = new BufferedImage(2,2, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = bufferImage.createGraphics();
        FontRenderContext frc = g2d.getFontRenderContext();
        TextLayout tl = new TextLayout(str, font, frc);
        return tl.getOutline(AffineTransform.getTranslateInstance(x,y));
    }
    private Point2D.Double getPoints(String point) {
        String[] split = point.split(",");
        return new Point2D.Double(Double.parseDouble(split[0]),Double.parseDouble(split[1]));
    }
}
