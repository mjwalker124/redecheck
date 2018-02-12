package shef.layout;

import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

/**
 * Created by thomaswalsh on 13/05/2016.
 */
public class Element {
    String xpath;
    String tag;
    JSONObject obj;
    int x1, x2, y1, y2;

    int[] boundingCoords;


    public int[] getContentCoords() {
        return contentCoords;
    }

    int[] contentCoords;
    Element parent;
    ArrayList<Element> children;
    HashMap<String, String> styles;
    Rectangle boundingRectangle;
    Rectangle contentRectangle;

    public HashMap<String, String> getStyles() {
        return styles;
    }

    public String getXpath() {
        return xpath;
    }
    public String getTag() { return tag; }
    public JSONObject getObj() {return obj;}
    public int[] getBoundingCoordinates() {
        return new int[] {x1, y1, x2, y2};
    }

    public Rectangle getRectangle() {
        return boundingRectangle;
    }

    public Element(String x, int x1, int y1, int x2, int y2) {
        this.xpath = x;
        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;
        boundingCoords = new int[] {x1,y1,x2,y2};
        this.boundingRectangle = new Rectangle(x1, y1, x2-x1, y2 - y1);
    }

    public Element(String x, String y, JSONObject obj, int x1, int y1, int x2, int y2) {
        this.xpath = x;
        this.tag = y;
        this.obj = obj;
        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;
        boundingCoords = new int[] {x1,y1,x2,y2};
        this.boundingRectangle = new Rectangle(x1, y1, x2-x1, y2 - y1);
    }

    public boolean hasAttribute(String attr) {
        try {
            System.out.println(obj);
            String data = obj.getString(attr);

            return (data != null && !Objects.equals(data, "") && !Objects.equals(data, "null"));
        } catch (Exception e) {
            System.out.println("Error while checking for "+attr+" attribute");
            e.printStackTrace();
        }
        return false;
    }

    public String getAttr(String attr) {
        try {
            return obj.getString(attr);
        } catch (JSONException e) {
            System.out.println("Error while getting "+attr+" attribute");
            e.printStackTrace();
        }

        return "error";
    }

    public void setParent(Element p) {
        parent = p;
    }

    public Element getParent() {
        return parent;
    }

    public void addChild(Element c) {
        this.children.add(c);
    }

    public String toString() {
        return xpath + " [" + x1 + "," + y1 + "," + x2 + "," + y2 + "]";
    }

    public void setY1(int y1) {
        this.y1 = y1;
    }

    public void setY2(int y2) {
        this.y2 = y2;
    }

    public int[] getBoundingCoords() {
        return boundingCoords;
    }



    public Rectangle getContentRectangle() {
        return this.contentRectangle;
    }


    public void setStyles(HashMap<String,String> styles) {
        this.styles = styles;
    }

    public int getY1() {
        return y1;
    }

    public int getY2() {
        return y2;
    }
}
