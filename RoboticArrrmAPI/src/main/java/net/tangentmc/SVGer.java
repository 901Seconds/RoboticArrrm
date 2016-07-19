package net.tangentmc;

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
        Shape[] shapes = null;
        File opened = new File(fileName);
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(opened);

            NodeList pathList = doc.getElementsByTagName("path");
            shapes = new Shape[pathList.getLength()];
            for (int i = 0; i < pathList.getLength(); i++) {
                org.w3c.dom.Node p = pathList.item(i);
                if (p.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                    Element path = (Element) p;
                    String d = path.getAttribute("d");
                    shapes[i] = parsePathShape(d);
                }
            }

        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        return shapes;
    }

}
