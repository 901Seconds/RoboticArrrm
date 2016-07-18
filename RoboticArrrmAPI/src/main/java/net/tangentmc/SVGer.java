package net.tangentmc;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by surface on 18/07/2016.
 */
public class SVGer {

//    public static SVGPoint[] pointsFromSVG(String fileName) {
//
//        SVGOMPathElement theElement;
//        //theElement.
//
//        Document doc = null;
//        try {
//            String parser = XMLResourceDescriptor.getXMLParserClassName();
//            SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
//            URI file = new File("file.svg").toURI();
//            System.out.println("uri creation happened");
//            String uri = file.toString();
//            doc = f.createDocument(uri);
//        } catch (IOException ex) {
//            ex.printStackTrace();
//            System.out.println("your'e farrrked");
//            return null;
//        }
//        NodeList h = (doc.getElementsByTagName("path"));
//        System.out.println(h.item(0));
//        SVGOMPathElement thePath = (SVGOMPathElement)h.item(0);
//        //System.out.println(thePath.getPathLength());
//        SVGPoint[] points = new SVGPoint[1000];
//        for(int i=0; i<1000; i++) {
//            System.out.println(thePath.getPointAtLength(i));
//            points[i] = thePath.getPointAtLength(((float)(i)));
//            System.out.println(points[i].getX() + ", " + points[1].getY());
//        }
//
//        return null;//points;
//    }

    @Deprecated
    public Point.Double[] pointsFromXML(String fileName) {
        Point.Double[] points = null;
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
                    Pattern pattern = Pattern.compile("[A-z]?(\\d*\\.\\d*,\\d*\\.\\d*)");
                    Matcher pointsets = pattern.matcher(d);
                    //this counts the z, so its perfect for adding a point to the end
                    points = new Point.Double[d.split("[A-z]?(\\d*\\.\\d*,\\d*\\.\\d*)").length];
                    int jDest = 0;
                    while(pointsets.find()) {
                        String[] split = pointsets.group(1).split(",");
                        points[jDest++] = new Point2D.Double(Double.parseDouble(split[0]),Double.parseDouble(split[1]));
                    }
                    points[jDest] = points[0];
                }
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();


        }
        return points;
    }

}
