package shef.reporting.inconsistencies;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.WebDriver;
import shef.layout.Element;
import shef.layout.LayoutFactory;
import shef.main.RLGExtractor;
import shef.main.Utils;
import shef.rlg.Node;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/** Created by thomaswalsh on 10/06/2016. */
public class WrappingFailure extends ResponsiveLayoutFailure {
  Node wrapped;
  ArrayList<Node> row;
  String range;
  int min, max;

  public WrappingFailure(Node n, ArrayList<Node> r, int min, int max) {
    this.wrapped = n;
    this.row = r;
    this.range = min + " -> " + max;
    this.min = min;
    this.max = max;
  }

  public String toString() {
    String rowString = "[ ";
    for (Node nr : row) {
      rowString += nr.getXpath() + " ";
    }
    rowString += "]";
    return "WRAPPING ELEMENT ERROR FOR RANGE "
        + range
        + ":"
        + "\n\t"
        + wrapped.getXpath()
        + " wrapped from row \n\t"
        + rowString;
  }

    @Override
    public int getWindowWidth() {
        return (min + max) / 2;
    }

  @Override
  public BufferedImage captureScreenShot(
          int errorID, WebDriver webDriver, String fullUrl) {
    int captureWidth = (min + max) / 2;
    HashMap<Integer, LayoutFactory> lfs = new HashMap<>();

    BufferedImage img;
    //            if (imageMap.containsKey(captureWidth)) {
    //                img = imageMap.get(captureWidth);
    //            } else {
    img = RLGExtractor.getScreenshot(captureWidth, errorID, lfs, webDriver, fullUrl);
    //                imageMap.put(captureWidth, img);
    //            }
    LayoutFactory lf = lfs.get(captureWidth);
    Element e1 = lf.getElementMap().get(wrapped.getXpath());

    Graphics2D g2d = img.createGraphics();
    g2d.setColor(Color.RED);
    g2d.setStroke(new BasicStroke(3));
    int[] coords1 = e1.getBoundingCoords();
    g2d.drawRect(coords1[0], coords1[1], coords1[2] - coords1[0], coords1[3] - coords1[1]);

    g2d.setColor(Color.CYAN);
    g2d.setStroke(new BasicStroke(3));
    for (Node n : row) {
      if (!n.getXpath().equals(wrapped.getXpath())) {
        Element e2 = lf.getElementMap().get(n.getXpath());
        int[] coords2 = e2.getBoundingCoords();
        g2d.drawRect(coords2[0], coords2[1], coords2[2] - coords2[0], coords2[3] - coords2[1]);
      }
    }
    g2d.dispose();
    return img;
  }

  @Override
  public void captureScreenshotExample(
      int errorID, String url, WebDriver webDriver, String fullurl, String timeStamp) {
    try {
      int captureWidth = (min + max) / 2;
      HashMap<Integer, LayoutFactory> lfs = new HashMap<>();

      BufferedImage img;
      //            if (imageMap.containsKey(captureWidth)) {
      //                img = imageMap.get(captureWidth);
      //            } else {
      img = RLGExtractor.getScreenshot(captureWidth, errorID, lfs, webDriver, url);
      //                imageMap.put(captureWidth, img);
      //            }
      LayoutFactory lf = lfs.get(captureWidth);
      Element e1 = lf.getElementMap().get(wrapped.getXpath());

      Graphics2D g2d = img.createGraphics();
      g2d.setColor(Color.RED);
      g2d.setStroke(new BasicStroke(3));
      int[] coords1 = e1.getBoundingCoords();
      g2d.drawRect(coords1[0], coords1[1], coords1[2] - coords1[0], coords1[3] - coords1[1]);

      g2d.setColor(Color.CYAN);
      g2d.setStroke(new BasicStroke(3));
      for (Node n : row) {
        if (!n.getXpath().equals(wrapped.getXpath())) {
          Element e2 = lf.getElementMap().get(n.getXpath());
          int[] coords2 = e2.getBoundingCoords();
          g2d.drawRect(coords2[0], coords2[1], coords2[2] - coords2[0], coords2[3] - coords2[1]);
        }
      }
      g2d.dispose();
      try {
        File output = Utils.getOutputFilePath(url, timeStamp, errorID);
        FileUtils.forceMkdir(output);
        ImageIO.write(img, "png", new File(output + "/wrappingWidth" + captureWidth + ".png"));
      } catch (IOException e) {
        //                e.printStackTrace();
      }
    } catch (NullPointerException npe) {
      System.out.println("Could not find one of the offending elements in screenshot.");
    }
  }

  @Override
  public HashSet<Node> getNodes() {
    HashSet<Node> nodes = new HashSet<>();
    nodes.add(wrapped);
    nodes.addAll(row);
    return nodes;
  }

  @Override
  public int[] getBounds() {
    return new int[] {min, max};
  }
}
