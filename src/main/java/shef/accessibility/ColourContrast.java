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
import java.util.Map;

public class ColourContrast implements IAccessibilityIssue {

  private static HashMap<Integer, List<Element>> errors = new HashMap<>();
  public int[] coords;
  private String xpath;
  private Boolean didPass = true;
  private int numberOfTimesTested = 0;
  private int x1, x2, y1, y2;
  private int width;
  private boolean cloudReportGenerated = false;

  public static HashMap<Integer, List<Element>> getErrors() {
    return errors;
  }

  @Override
  public void captureScreenshotExample(
      int errorID, String url, WebDriver webDriver, String fullurl, String timeStamp) {
    System.out.println("attempt screen shot");
    for (Map.Entry<Integer, List<Element>> elementEntry : errors.entrySet()) {
      try {
        int captureWidth = elementEntry.getKey();
        HashMap<Integer, LayoutFactory> lfs = new HashMap<>();

        BufferedImage img;
        img = RLGExtractor.getScreenshot(captureWidth, errorID, lfs, webDriver, url);

        Graphics2D g2d = img.createGraphics();
        for (Element e : elementEntry.getValue()) {
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
          Boolean makeFolders = new File(output + "/ColourContrastIssues").mkdir();
          ImageIO.write(
              img, "png", new File(output + "/ColourContrastIssues/" + captureWidth + ".png"));

        } catch (IOException e) {
          //                e.printStackTrace();
        }
      } catch (NullPointerException npe) {
        npe.printStackTrace();
        System.out.println("Could not find one of the offending elements in screenshot.");
      }
    }
  }

  private void addError(Element element, int width) {
    if (ColourContrast.errors.get(width) == null) {
        List<Element> elements = new ArrayList<>();
        elements.add(element);
        ColourContrast.errors.put(width, elements);
    } else {
        ColourContrast.errors.get(width).add(element);
    }
  }

  private boolean shouldCheckError(Element element, int width) {
      for(Map.Entry<Integer, List<Element>> elementEntry : ColourContrast.errors.entrySet()) {
          if (elementEntry.getKey() > width - 150 && elementEntry.getKey() > width + 150 && elementEntry.getValue().contains(element)) {
              return false;
          }
      }
      return true;
  }

  @Override
  public WebDriver checkIssue(Element element, HashMap<String, Element> otherElements, int width, WebDriver webDriver, ResponsiveLayoutGraph r, String fullUrl, ArrayList<Integer> breakpoints, HashMap<Integer, LayoutFactory> lFactories, int vmin, int vmax) {
    this.width = width;
    if (!element.getInHead() && shouldCheckError(element, width)) {
      Double backgroundLuminance = luminanceCalculator(element.getActualBackgroundColour());
      Double foregroundLuminance = luminanceCalculator(element.getActualForegroundColour());

      if ((element.getFontSize() < 24
              && luminanceContrast(backgroundLuminance, foregroundLuminance) < 4.5)
          || (element.getFontSize() >= 24
              && luminanceContrast(backgroundLuminance, foregroundLuminance) < 3)
          && element.getFontSize() < 14) {

        addError(element, width);
        System.out.println("***** colour test");

        System.out.println("Node: " + element.getTag());
        System.out.println("Colour String: " + element.getColourString());
        System.out.println(
            "Foreground: " + luminanceCalculator(element.getActualForegroundColour()));
        System.out.println(outputColour(element.getActualForegroundColour()));
        System.out.println(
            "Background: " + luminanceCalculator(element.getActualBackgroundColour()));
        System.out.println(outputColour(element.getActualBackgroundColour()));
        System.out.println(
            "Contrast: " + luminanceContrast(backgroundLuminance, foregroundLuminance));
        System.out.println("---------");
      }
    }
    return webDriver;
  }

  private Double luminanceCalculator(Double[] colour) {
    Double[] colourCalcs = new Double[3];
    for (int i = 0; i < 3; i++) {
      if (colour[i] <= 0.03928) {
        colourCalcs[i] = colour[i] / 12.92;
      } else {
        colourCalcs[i] = Math.pow(((colour[i] + 0.055) / 1.055), 2.4);
      }
    }

    return (0.2126 * colourCalcs[0]) + (0.7152 * colourCalcs[1]) + (0.0722 * colourCalcs[2]);
  }

  private Double luminanceContrast(Double colour1, Double colour2) {
    Double c1;
    Double c2;

    if (colour1 > colour2) {
      c1 = colour1;
      c2 = colour2;
    } else {
      c1 = colour2;
      c2 = colour1;
    }

    return (c1 + 0.05) / (c2 + 0.05);
  }

  private String outputColour(Double[] colour) {
    return "["
        + (colour[0] * 255)
        + ", "
        + (colour[1] * 255)
        + ", "
        + (colour[2] * 255)
        + ", "
        + colour[3]
        + "]";
  }

  @Override
  public boolean getDidPass() {
    return didPass;
  }

  @Override
  public String getErrorMessage() {
    StringBuilder output =
        new StringBuilder(
            ColourContrast.errors.size()
                + " foreground and background colours do not have enough contrast \n");


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
    return true;
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
    sheetProperties.setTitle("Colour Contrast");
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
    for (Map.Entry<Integer, List<Element>> elementEntry : errors.entrySet()) {
      for (Element element : elementEntry.getValue()) {
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
