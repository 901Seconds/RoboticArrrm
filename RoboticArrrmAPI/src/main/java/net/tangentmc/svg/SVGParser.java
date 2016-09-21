package net.tangentmc.svg;

import ecs100.UI;
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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import static java.awt.geom.PathIterator.*;

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
        HashMap<String,ArrayList<Shape>> symbols = new HashMap<>();
        File opened = new File(fileName);
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(opened);
            //Parse and store symbols
            NodeList pathList = doc.getElementsByTagName("path");
            for (int i = 0; i < pathList.getLength(); i++) {
                org.w3c.dom.Node p = pathList.item(i);
                if (p.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                    Element path = (Element) p;
                    if (checkStroke(path)) continue;
                    String d = path.getAttribute("d");
                    addToArr(path,parsePathShape(d),symbols,shapes);
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
                        addToArr(path,new RoundRectangle2D.Double(x,y,width,height,rx,ry),symbols,shapes);
                    } else {
                        addToArr(path,new Rectangle2D.Double(x,y,width,height),symbols,shapes);
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
                    addToArr(path,new Ellipse2D.Double(x,y,r,r),symbols,shapes);

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
                    addToArr(path,new Ellipse2D.Double(x,y,rx,ry),symbols,shapes);

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
                    addToArr(path,new Line2D.Double(x1,y1,x2,y2),symbols,shapes);

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
                    addToArr(path,path2d,symbols,shapes);
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
                    addToArr(path,path2d,symbols,shapes);
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
                        x = getAttrib("x",path,x);
                        y = getAttrib("y",path,y);
                        for (int i1 = 0; i1 < tPath.getChildNodes().getLength(); i1++) {
                            path = (Element) tPath.getChildNodes().item(i1);
                            if (path.getTextContent().isEmpty()) continue;
                            font = parseStyle(path,parentfont);
                            addToArr(path,getTextShape(path.getTextContent(), font, x, y),symbols,shapes);
                        }
                    }
                }
            }
            pathList = doc.getElementsByTagName("use");
            for (int i = 0; i < pathList.getLength(); i++) {
                org.w3c.dom.Node p = pathList.item(i);
                if (p.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                    Element path = (Element) p;
                    String symbolid=path.getAttribute("xlink:href").substring(1);
                    //We don't parse images, so some symbols wont exist.
                    if (!symbols.containsKey(symbolid)) continue;
                    AffineTransform transform = AffineTransform.getTranslateInstance(getAttrib("x", path),getAttrib("y", path));
                    for (int i1 = 0; i1 < symbols.get(symbolid).size(); i1++) {
                        shapes.add(transform.createTransformedShape(symbols.get(symbolid).get(i1)));
                    }
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        return shapes.toArray(new Shape[0]);

    }

    private boolean checkStroke(Element path) {
        return path.hasAttribute("style") && path.getAttribute("style").contains("fill-rule:nonzero");
    }
    private static Font plotFriendly;
    static {
        try {
            plotFriendly = Font.createFont(Font.TRUETYPE_FONT, new File("fonts/1CamBam_Stick_7.ttf"));
            GraphicsEnvironment ge =
                    GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(plotFriendly);
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
        }
    }
    private Font parseStyle(Element path, Font font) {
        float size = font==null?10:font.getSize();
        if (path.hasAttribute("style")) {
            String styleAttribs = path.getAttribute("style");
            for (String s : styleAttribs.split(";")) {
                if (s.contains("font-size")) {
                    size = Float.parseFloat(s.replace("font-size:", "").replace("px", ""));
                }
            }
        }

        return plotFriendly.deriveFont(size);
    }
    private void addToArr(Element p, Shape shape, HashMap<String,ArrayList<Shape>> symbols, ArrayList<Shape> shapes) {
        if (p.getParentNode().getNodeName().equals("symbol")) {
            String glyphid = ((Element) p.getParentNode()).getAttribute("id");
            symbols.putIfAbsent(glyphid,new ArrayList<>());
            symbols.get(glyphid).add(stripClose(shape.getPathIterator(null)));
        } else {
            shapes.add(shape);
        }
    }
    private double getAttrib(String attrib, Element path, double def) {
        if (path.hasAttribute(attrib))
            return Double.parseDouble(path.getAttribute(attrib));
        return def;
    }
    private double getAttrib(String attrib, Element path) {
        return getAttrib(attrib,path,0);
    }
    private Shape getTextShape(String str, Font font, double x, double y) {
        BufferedImage bufferImage = new BufferedImage(2,2, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = bufferImage.createGraphics();
        FontRenderContext frc = g2d.getFontRenderContext();
        TextLayout tl = new TextLayout(str, font, frc);
        return stripClose(tl.getOutline(AffineTransform.getTranslateInstance(x,y)).getPathIterator(null));
    }

    private Shape stripClose(PathIterator outline) {
        Path2D textPath = new Path2D.Double();
        //Single line fonts are forced to join paths with SEG_CLOSE. However, this actually results
        //in extra lines we don't want drawn, so we can safely skip them.
        double[] points = new double[6];
        for (;!outline.isDone();outline.next()) {
            int type = outline.currentSegment(points);
            switch (type) {
                case SEG_CLOSE:
                    continue;
                case SEG_CUBICTO:
                    textPath.curveTo(points[0],points[1],points[2],points[3],points[4],points[5]);
                    continue;
                case SEG_LINETO:
                    textPath.lineTo(points[0],points[1]);
                    continue;
                case SEG_MOVETO:
                    textPath.moveTo(points[0],points[1]);
                    continue;
                case SEG_QUADTO:
                    textPath.quadTo(points[0],points[1],points[2],points[3]);
            }
        }
        return textPath;
    }

    private Point2D.Double getPoints(String point) {
        String[] split = point.split(",");
        return new Point2D.Double(Double.parseDouble(split[0]),Double.parseDouble(split[1]));
    }
}
