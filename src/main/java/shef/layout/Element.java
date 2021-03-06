package shef.layout;

import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

/** Created by thomaswalsh on 13/05/2016. */
public class Element {
  String xpath;
  String tag;
  Integer lineNumber;
  Integer documentPosition;
  JSONObject obj;
  String colourString;
  Double[] backgroundColour;
  Double[] foregroundColour;
  Double[] actualBackgroundColour = null;
  Double[] actualForegroundColour = null;
  int x1, x2, y1, y2;
  int[] boundingCoords;
  int[] contentCoords;
  Element parent;
  ArrayList<Element> children = new ArrayList<>();
  HashMap<String, String> styles;
  Rectangle boundingRectangle;
  Rectangle contentRectangle;
  private Integer parentWidth;
  private Integer fontSize;
  private Integer documentHeight;
  private Integer documentLines;
  private Boolean inHead;
  private String text;
  private Boolean hasTabIndex;

  public Element(String x, int x1, int y1, int x2, int y2) {
    this.xpath = x;
    this.x1 = x1;
    this.x2 = x2;
    this.y1 = y1;
    this.y2 = y2;
    boundingCoords = new int[] {x1, y1, x2, y2};
    this.boundingRectangle = new Rectangle(x1, y1, x2 - x1, y2 - y1);
  }

  public Element(
      String x,
      Boolean inHead,
      Integer lineNumber,
      Integer codePosition,
      String backgroundColour,
      String foregroundColour,
      Boolean hasTabIndex,
      String text,
      Integer fontSize,
      Integer documentHeight,
      Integer documentLines,
      String y,
      JSONObject obj,
      int x1,
      int y1,
      int x2,
      int y2) {
    this.lineNumber = lineNumber;
    this.xpath = x;
    this.tag = y;
    this.obj = obj;
    this.x1 = x1;
    this.x2 = x2;
    this.text = text;
    this.hasTabIndex = hasTabIndex;
    this.y1 = y1;
    this.y2 = y2;
    this.inHead = inHead;
    this.fontSize = fontSize;
    //System.out.println("Font Size: " + fontSize);
    this.colourString = backgroundColour;
    this.backgroundColour = colourToArray(backgroundColour);
    this.foregroundColour = colourToArray(foregroundColour);
    this.documentLines = documentLines;
    this.documentHeight = documentHeight;
    boundingCoords = new int[] {x1, y1, x2, y2};
    this.boundingRectangle = new Rectangle(x1, y1, x2 - x1, y2 - y1);
  }

  public Boolean hasTabIndex() {
    return hasTabIndex;
  }

  public Boolean getInHead() {
    return inHead;
  }

  public String getColourString() {
    return colourString;
  }

  public String getText() { return text; }

  public Double[] getBackgroundColour() {
    return backgroundColour;
  }

  public void setBackgroundColour(String backgroundColour) {
    this.backgroundColour = colourToArray(backgroundColour);
  }

  //Developed by Matthew Walker
  public Double[] colourToArray(String colour) {
    Double[] colours = new Double[4];
    if (!inHead) {
      if (colour.startsWith("rgba")) {
        colour = colour.split("rgba")[1];
        colour = colour.replace("(", "");
        colour = colour.replace(")", "");
        String[] colourString = colour.split(", ");
        colours[0] = Double.parseDouble(colourString[0]) / 255;
        colours[1] = Double.parseDouble(colourString[1]) / 255;
        colours[2] = Double.parseDouble(colourString[2]) / 255;
        colours[3] = (Double.parseDouble(colourString[3]));
      } else if (colour.startsWith("rgb")) {
        colour = colour.split("rgb")[1];
        colour = colour.replace("(", "");
        colour = colour.replace(")", "");
        String[] colourString = colour.split(", ");

        colours[0] = Double.parseDouble(colourString[0]) / 255;
        colours[1] = Double.parseDouble(colourString[1]) / 255;
        colours[2] = Double.parseDouble(colourString[2]) / 255;
        colours[3] = 1.0;
      } else if (colour.equals("transparent")) {
        //System.out.println(this.getXpath());
        //System.out.println(this.tag);

        if (this.tag.equalsIgnoreCase("BODY")) {
          // System.out.println("got body");
          return new Double[] {1.0, 1.0, 1.0, 1.0};
        } else {
          //System.out.println(tag);
          //System.out.println("How");
          return new Double[] {1.0, 1.0, 1.0, 0.0};
        }
      }
    }
    return colours;
  }

  //Developed By Matthew Walker
  private Double[] getActualBackground() {
      //Only do complicated stuff if the opacity isn't 100%
    if (backgroundColour[3] < 100) {
      Element parentElement = this.getParent();
      try {
          //The parent will only be null at the top element so at that point the background for that element is just set
          // to white, otherwise merge the actual background of the parent with the child.
        if (parentElement != null) {
          return mergeTwoColours(backgroundColour, this.getParent().getActualBackground());
        } else {
          return mergeTwoColours(backgroundColour, new Double[] {1.0, 1.0, 1.0, 1.0});
        }
      } catch(StackOverflowError ex) {
        return new Double[] {1.0, 1.0, 1.0, 1.0};
      }
    } else {
      return backgroundColour;
    }
  }

  public Integer getFontSize() {
    return fontSize;
  }

    //Developed By Matthew Walker
  private Double[] getActualForeground() {
    //If there is some opacity then merge the foreground with the actual background colour
    if (foregroundColour[3] < 100) {
      return mergeTwoColours(foregroundColour, getActualBackgroundColour());
    } else {
      return foregroundColour;
    }
  }

  //Developed By Matthew Walker using algorithm outlined https://stackoverflow.com/a/48343059
  private Double[] mergeTwoColours(Double[] colour1, Double[] colour2) {
    Double[] result = new Double[] {0.0, 0.0, 0.0, 0.0};
    Double colour1A = colour1[3];
    Double colour2A = colour2[3];

    result[3] = ((((1 - colour1A) * colour2A) + colour1A));
    for (int i = 0; i < 3; i++) {
      result[i] = ((((1 - colour1A) * colour2A * colour2[i]) + colour1A * colour1[i]) / result[3]);
    }

    return result;
  }

  public Double[] getForegroundColour() {
    return foregroundColour;
  }

  public void setForegroundColour(String foregroundColour) {
    this.foregroundColour = colourToArray(foregroundColour);
  }

    //Developed By Matthew Walker
  public Double[] getActualBackgroundColour() {
    if (actualBackgroundColour == null) {
      actualBackgroundColour = getActualBackground();
    }
    return actualBackgroundColour;
  }

    //Developed By Matthew Walker
  public Double[] getActualForegroundColour() {
    if (actualForegroundColour == null) {
      actualForegroundColour = getActualForeground();
    }
    return actualForegroundColour;
  }

  public Integer getParentWidth() {
    return parentWidth;
  }

  public void setParentWidth(int width) {
    parentWidth = width;
  }

  public Integer getLineNumber() {
    return lineNumber;
  }

  public int[] getContentCoords() {
    return contentCoords;
  }

  public HashMap<String, String> getStyles() {
    return styles;
  }

  public void setStyles(HashMap<String, String> styles) {
    this.styles = styles;
  }

  public String getXpath() {
    return xpath;
  }

  public String getTag() {
    return tag;
  }

  public JSONObject getObj() {
    return obj;
  }

  public int[] getBoundingCoordinates() {
    return new int[] {x1, y1, x2, y2};
  }

  public Rectangle getRectangle() {
    return boundingRectangle;
  }

  public boolean hasAttribute(String attr) {
    try {
      //System.out.println(obj);
      String data = obj.getString(attr);

      return (data != null && !Objects.equals(data, "") && !Objects.equals(data, "null"));
    } catch (Exception e) {
      //System.out.println("Error while checking for " + attr + " attribute");
      e.printStackTrace();
    }
    return false;
  }

  public String getAttr(String attr) {
    try {
      return obj.getString(attr);
    } catch (JSONException e) {
      //System.out.println("Error while getting " + attr + " attribute");
      e.printStackTrace();
    }

    return "error";
  }

  public Element getParent() {
    return parent;
  }

  public void setParent(Element p) {
    parent = p;
  }

  public void addChild(Element c) {
    this.children.add(c);
  }

  public ArrayList<Element> getChildren() {
    return this.children;
  }

  public String toString() {
    return xpath + " [" + x1 + "," + y1 + "," + x2 + "," + y2 + "]";
  }

  public int[] getBoundingCoords() {
    return boundingCoords;
  }

  public Rectangle getContentRectangle() {
    return this.contentRectangle;
  }

  public int getY1() {
    return y1;
  }

  public void setY1(int y1) {
    this.y1 = y1;
  }

  public int getY2() {
    return y2;
  }

  public void setY2(int y2) {
    this.y2 = y2;
  }

  public Integer getDocumentHeight() {
    return documentHeight;
  }

  public Integer getDocumentLines() {
    return documentLines;
  }
}
