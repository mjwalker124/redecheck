package shef.accessibility;

import com.google.api.services.drive.Drive;
import com.google.api.services.sheets.v4.model.Sheet;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.WebDriver;
import shef.handlers.CloudReporting;
import shef.layout.Element;
import shef.layout.LayoutFactory;
import shef.main.RLGExtractor;
import shef.main.Utils;
import shef.rlg.ResponsiveLayoutGraph;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ParsingIDCheck implements IAccessibilityIssue {

  private static List<Element> errors = new ArrayList<>();
  private static HashMap<String, Element> ids = new HashMap<>();
  private Boolean didPass = false;
  private int numberOfTimesTested = 0;
  private int width;
  private boolean cloudReportGenerated = false;

  public static List<Element> getErrors() {
    return errors;
  }

  @Override
  public void captureScreenshotExample(
      int errorID, String url, WebDriver webDriver, String fullurl, String timeStamp) {
    System.out.println("attempt screen shot");
    try {
      int captureWidth = width;
      HashMap<Integer, LayoutFactory> lfs = new HashMap<>();

      BufferedImage img;
      img = RLGExtractor.getScreenshot(captureWidth, errorID, lfs, webDriver, url);

      Graphics2D g2d = img.createGraphics();

      for (Element e : ParsingIDCheck.errors) {
        g2d.setColor(Color.RED);
        g2d.setStroke(new BasicStroke(3));
        int coords[] = e.getBoundingCoords();
        g2d.drawRect(coords[0], coords[1], coords[2] - coords[0], coords[3] - coords[1]);
      }

      g2d.dispose();
      try {

        Drive driveService = CloudReporting.getDriveService();
        File output = Utils.getOutputFilePath(url, timeStamp, errorID, true);
        FileUtils.forceMkdir(output);
        Boolean makeFolders = new File(output + "/ParsingIDCheck").mkdir();

        ImageIO.write(img, "png", new File(output + "/ParsingIDCheck/" + captureWidth + ".png"));

      } catch (IOException e) {
        //                e.printStackTrace();
      }
    } catch (NullPointerException npe) {
      npe.printStackTrace();
      System.out.println("Could not find one of the offending elements in screenshot.");
    }
  }

  @Override
  public WebDriver checkIssue(Element element, HashMap<String, Element> otherElements, int width, WebDriver webDriver, ResponsiveLayoutGraph r, String fullUrl, ArrayList<Integer> breakpoints, HashMap<Integer, LayoutFactory> lFactories, int vmin, int vmax) {
    if (!element.getInHead()) {
      this.width = width;
      System.out.println("***** parsing id check" );
      if (!element.getAttr("id").equals("null")) {
        String elementId = element.getAttr("id");
        if (ids.get(elementId) != null) {
          if(!errors.contains(ids.get(elementId))) {
            errors.add(ids.get(elementId));
          }
          errors.add(element);
        } else {
          ids.put(elementId, element);
        }
      }
    }
    return webDriver;
  }

  @Override
  public boolean getDidPass() {
    return didPass;
  }

  @Override
  public String getErrorMessage() {
    if (didPass) {
      return "No Error with the title";
    } else {
      return "Missing title tag";
    }
  }

  @Override
  public String getFixInstructions() {
    return null;
  }

  @Override
  public String consoleOutput() {
    return null;
  }

  @Override
  public boolean isAffectedByLayouts() {
    return false;
  }

  @Override
  public int numberOfTimesTested() {
    return numberOfTimesTested;
  }

  @Override
  public void incNumberOfTimesTested() {
    numberOfTimesTested++;
  }

  @Override
  public Sheet generateCloudReport() {
    return null;
  }

  @Override
  public boolean cloudReportMade() {
    return cloudReportGenerated;
  }

}
