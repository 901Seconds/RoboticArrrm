package net.tangentmc.svg;

import org.apache.batik.parser.AWTPathProducer;
import org.apache.batik.parser.ParseException;
import org.apache.batik.parser.PathParser;
import org.apache.xerces.dom.DeferredTextImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class SVGParser {
    private static final double MIN_X = 10;
    private static final double MAX_X = 230;
    private static final double MIN_Y = 30;
    private static final double MAX_Y = 180;
    private static Shape[] parsePathShape(String svgPathShape) {
        try {
            AWTPathProducer pathProducer = new AWTPathProducer();
            PathParser pathParser = new PathParser();
            pathParser.setPathHandler(pathProducer);
            pathParser.parse(svgPathShape);
            Shape shape = pathProducer.getShape();
            return new Shape[]{shape};

        } catch (ParseException ex) {
            // Fallback to default square shape if shape is incorrect
            return new Shape[]{new Rectangle2D.Float(0, 0, 1, 1)};
        }
    }

    public Shape[] shapesFromXML(String fileName) {
        ArrayList<Shape> shapes = new ArrayList<>();
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
                    if (path.getParentNode().getNodeName().contains("clip")) continue;
                    String d = path.getAttribute("d");
                    Collections.addAll(shapes, parsePathShape(d));

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
            pathList = doc.getElementsByTagName("image");
            for (int i = 0; i < pathList.getLength(); i++) {
                org.w3c.dom.Node p = pathList.item(i);
                if (p.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                    Element path = (Element) p;
                    String png = path.getAttribute("xlink:href");
                    String transStr = path.getAttribute("transform").replace("matrix(","");
                    transStr = transStr.substring(0, transStr.length()-1);
                    String[] transStrSplit = transStr.split(",");
                    float tx = Float.parseFloat(transStrSplit[4]), ty = Float.parseFloat(transStrSplit[5]);
                    float sx = Float.parseFloat(transStrSplit[0]), sy = Float.parseFloat(transStrSplit[3]);
                    Path2D path2d = new Path2D.Double();
                    BufferedImage img;
                    if (png.startsWith("data")) {
                        String base64Image = png.split(",")[1];
                        byte[] imageBytes = javax.xml.bind.DatatypeConverter.parseBase64Binary(base64Image);
                        ByteArrayInputStream bs = new ByteArrayInputStream(imageBytes);
                        img = ImageIO.read(bs);
                        bs.close();
                    } else {
                        img = ImageIO.read(new File(png));
                    }

                    BufferedImage newBufferedImage = new BufferedImage((int)(sx*img.getWidth()),(int)(sy*img.getHeight()), BufferedImage.TYPE_BYTE_GRAY);
                    Graphics2D graphics2D = newBufferedImage.createGraphics();
                    graphics2D.setTransform(AffineTransform.getScaleInstance(sx,sy));
                    graphics2D.drawImage(img, 0, 0, Color.WHITE, null);
                    img = newBufferedImage;
                    byte[] pixels = ((DataBufferByte)img.getRaster().getDataBuffer()).getData();
                    boolean cur = false, last = false;
                    int dir = 1;
                    path2d.moveTo(tx,ty);
                    for (int y = 0; y < img.getHeight(); y++) {
                        path2d.moveTo((dir==1?0:img.getWidth())+tx,y+ty);
                        //Alternate direction so that the pen doesnt cross the page
                        for (int x = dir==1?0:img.getWidth()-1; x >= 0 && x < img.getWidth();x+=dir) {
                            cur = pixels[(y * img.getWidth()) + x] >= 0;
                            if (cur != last) {
                                if (cur) {
                                    path2d.moveTo(x+tx,y+ty);
                                } else {
                                    //Remember, if your going from black to white, you want to draw the line to the prev
                                    //pixel, not current, so take dir
                                    path2d.lineTo(x-dir+tx,y+ty);
                                }
                            }
                            last = cur;
                        }
                        //If your at the end of the page, and the pen was down, we should finish that stroke
                        if (cur && dir == 1) {
                            path2d.lineTo(img.getWidth()+ tx, y + ty);
                        }
                        if (cur && dir == -1) {
                            path2d.lineTo(tx, y + ty);
                        }
                        dir = -dir;
                    }
                    shapes.add(path2d);
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
                        x = getAttrib("x",path,x);
                        y = getAttrib("y",path,y);
                        for (int i1 = 0; i1 < tPath.getChildNodes().getLength(); i1++)
                            if (tPath.getChildNodes().item(i1) instanceof DeferredTextImpl) {
                                font = parseStyle(tPath, parentfont);
                                shapes.add(getTextShape(path.getTextContent(), font, x, y));
                            } else {
                                path = (Element) tPath.getChildNodes().item(i1);
                                if (path.getTextContent().isEmpty()) continue;
                                font = parseStyle(path, parentfont);
                                shapes.add(getTextShape(path.getTextContent(), font, x, y));
                            }
                    }
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        double minX = Double.MAX_VALUE,maxX = 0,minY=Double.MAX_VALUE,maxY=0;
        for (Shape shape: shapes) {
            minX = Math.min(minX,shape.getBounds().getX());
            minY = Math.min(minY,shape.getBounds().getY());
            maxX = Math.max(maxX,shape.getBounds().getMaxX());
            maxY = Math.max(maxY,shape.getBounds().getMaxY());
        }
        double width = maxX-minX;
        double height = maxY-minY;
        double proportion = width/height;
        double newHeight = Math.min(height,MAX_Y-MIN_Y);
        double newWidth = proportion*newHeight;
        proportion = newHeight/newWidth;
        newWidth = Math.min(newWidth,MAX_X-MIN_X);
        newHeight = proportion*newWidth;
        AffineTransform transform = new AffineTransform();
        double tx=Math.max(minX,MIN_X),ty=Math.max(minY,MIN_Y);
        tx = Math.min(tx,MAX_X-newWidth);
        ty = Math.min(ty,MAX_Y-newHeight);
        transform.translate(tx,ty);
        transform.scale(newWidth/width,newHeight/height);
        for (int i = 0; i < shapes.size(); i++) {
            shapes.set(i,transform.createTransformedShape(shapes.get(i)));
        }
        return shapes.toArray(new Shape[0]);

    }
    private Font parseStyle(Element path, Font font) {
        float size = font==null?10:font.getSize();
        String family = font==null?null:font.getFamily();
        if (path.hasAttribute("style")) {
            String styleAttribs = path.getAttribute("style");
            for (String s : styleAttribs.split(";")) {
                if (s.contains("font-size")) {
                    size = Float.parseFloat(s.replace("font-size:", "").replace("px", ""));
                }
                if (s.contains("font-family")) {
                    family = s.replace("font-family","");
                }
            }
        } else if (path.hasAttribute("font-size")) {
            size = Float.parseFloat(path.getAttribute("font-size").replace("font-size:", "").replace("px", ""));
            family = path.getAttribute("font-family");
        }

        return new Font(family,Font.PLAIN, (int) size);
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
        return tl.getOutline(AffineTransform.getTranslateInstance(x,y));
    }


    private Point2D.Double getPoints(String point) {
        String[] split = point.split(",");
        return new Point2D.Double(Double.parseDouble(split[0]),Double.parseDouble(split[1]));
    }
}
