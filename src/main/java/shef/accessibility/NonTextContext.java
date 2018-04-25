package shef.accessibility;

import com.google.api.services.drive.Drive;
import com.google.api.services.sheets.v4.model.*;
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
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NonTextContext implements IAccessibilityIssue {

  private static List<Element> errors = new ArrayList<>();
  private static List<Element> warnings = new ArrayList<>();
  private Boolean didPass = true;
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

      for (Element e : NonTextContext.warnings) {
        g2d.setColor(Color.ORANGE);
        g2d.setStroke(new BasicStroke(3));
        int coords[] = e.getBoundingCoords();
        g2d.drawRect(coords[0], coords[1], coords[2] - coords[0], coords[3] - coords[1]);
      }

      for (Element e : NonTextContext.errors) {
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
        Boolean makeFolders = new File(output + "/NonTextContext").mkdir();

        ImageIO.write(img, "png", new File(output + "/NonTextContext/" + captureWidth + ".png"));

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

      System.out.println("***** Non Text Context");
      if (element.getTag().equalsIgnoreCase("img")) {
        if (!(element.hasAttribute("aria-label") || element.hasAttribute("aria-labelledby"))) {
          NonTextContext.errors.add(element);
          this.width = width;
          didPass = false;
        } else {
          didPass = true;
        }
        if (!(element.hasAttribute("alt") || element.hasAttribute("longdesc"))) {
          NonTextContext.warnings.add(element);
        }
      }
      if (element.getTag().equalsIgnoreCase("button") || element.getTag().equalsIgnoreCase("input") || element.getTag().equalsIgnoreCase("audio") || element.getTag().equalsIgnoreCase("embed")) {
        if (element.getTag().equalsIgnoreCase("button")) {
          if (!element.hasAttribute("data-message")) {
            NonTextContext.errors.add(element);
            this.width = width;
            didPass = false;
          }
        }
        if (!element.hasAttribute("aria-label") && !element.hasAttribute("aria-labelledby")) {
          NonTextContext.errors.add(element);
          this.width = width;
          didPass = false;
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
    StringBuilder output =
        new StringBuilder(NonTextContext.errors.size() + " images do not have alt tags \n");
    for (Element element : NonTextContext.errors) {
      output.append(element.getXpath()).append(" \n");
    }

    return output.toString();
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
    Sheet sheet = new Sheet();
    SheetProperties sheetProperties = new SheetProperties();
    sheetProperties.setTitle("Non text Context");
    sheet.setProperties(sheetProperties);
    List<GridData> grid = new ArrayList<>();
    List<RowData> rowDataList = new ArrayList<>();

    CellData pageTitle = new CellData();
    pageTitle.setUserEnteredValue(new ExtendedValue().setStringValue("Images without alt tags"));

    List<CellData> titleRow = new ArrayList<>();
    titleRow.add(pageTitle);

    rowDataList.add((new RowData()).setValues(titleRow));
    rowDataList.add(null);

    int i = 1;
    for (Element element : NonTextContext.errors) {
      System.out.println(element.getXpath());
      CellData cellData = new CellData();
      cellData.setUserEnteredValue(new ExtendedValue().setStringValue(element.getXpath()));

      CellData cellData2 = new CellData();
      cellData2.setUserEnteredValue(new ExtendedValue().setStringValue(String.valueOf(i)));

      List<CellData> row = new ArrayList<>();
      row.add(cellData2);
      row.add(cellData);

      RowData rowData = new RowData();
      rowData.setValues(row);

      rowDataList.add(rowData);
      i++;
    }

    grid.add((new GridData()).setRowData(rowDataList));
    sheet.setData(grid);
    cloudReportGenerated = true;
    return sheet;
  }

  @Override
  public boolean cloudReportMade() {
    return cloudReportGenerated;
  }
}
