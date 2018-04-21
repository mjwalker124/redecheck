package shef.accessibility;

import com.google.api.services.sheets.v4.model.*;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.WebDriver;
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

public class MeaningfulSequence implements IAccessibilityIssue {

  private static HashMap<Integer, HashMap<Element, Element>> errors = new HashMap<>();
  private static HashMap<String, List<Integer>> xPathWidthStore = new HashMap<>();
  public int[] coords;
  private String xpath;
  private Boolean didPass = true;
  private int numberOfTimesTested = 0;
  private int x1, x2, y1, y2;
  private int max, min;
  private boolean cloudReportGenerated = false;


  public static HashMap<Integer, HashMap<Element, Element>> getErrors() {
    return errors;
  }

  @Override
  public void captureScreenshotExample(
      int errorID, String url, WebDriver webDriver, String fullurl, String timeStamp) {
    for (Map.Entry<Integer, HashMap<Element, Element>> error :
        MeaningfulSequence.errors.entrySet()) {
      System.out.println("attempt screen shot");
      try {
        int captureWidth = error.getKey();
        HashMap<Integer, LayoutFactory> lfs = new HashMap<>();

        for (Map.Entry<Element, Element> internalError : error.getValue().entrySet()) {
          BufferedImage img;
          Element e1 = internalError.getKey();
          Element e2 = internalError.getValue();
          img = RLGExtractor.getScreenshot(captureWidth, errorID, lfs, webDriver, url);

          Graphics2D g2d = img.createGraphics();

          g2d.setColor(Color.BLUE);
          g2d.setStroke(new BasicStroke(3));
          int coords[] = e1.getBoundingCoords();
          g2d.drawRect(coords[0], coords[1], coords[2] - coords[0], coords[3] - coords[1]);
          g2d.setColor(Color.RED);
          int coords2[] = e2.getBoundingCoords();
          g2d.drawRect(coords2[0], coords2[1], coords2[2] - coords2[0], coords2[3] - coords2[1]);
          g2d.dispose();

          try {

            // Drive driveService = CloudReporting.getDriveService();
            File output = Utils.getOutputFilePath(url, timeStamp, errorID, true);
            FileUtils.forceMkdir(output);
            Boolean makeFolders = new File(output + "/MeaningfulSequence").mkdir();
            ImageIO.write(
                img,
                "png",
                new File(
                    output
                        + "/MeaningfulSequence/"
                        + captureWidth
                        + ".png"));
            /*
            com.google.api.services.drive.model.File imageData = new com.google.api.services.drive.model.File();
            imageData.setName("ImageAltTagMissing.png");
            FileUtils.forceMkdir(output);
            java.io.File filePath = new java.io.File(output + "/ImageAltTagMissing" + captureWidth + ".png");
            FileContent mediaContent = new FileContent("image/png", filePath);
            com.google.api.services.drive.model.File imageFileUpload = driveService.files().create(imageData, mediaContent)
                    .setFields("id")
                    .execute();
            System.out.println("Content ID: " + imageFileUpload.getWebContentLink());
            System.out.println("View ID: " + imageFileUpload.getWebViewLink());
            */
          } catch (IOException e) {
            e.printStackTrace();
          }
        }

      } catch (NullPointerException npe) {
        npe.printStackTrace();
        System.out.println("Could not find one of the offending elements in screenshot.");
      }
    }
  }

  private void addError(Integer width, Element e1, Element e2) {
    List<Integer> storedWidths = xPathWidthStore.get(e1.getXpath() + e2.getXpath());
    Boolean addElements = false;
    if (storedWidths.size() == 0) {
      addElements = true;
    } else {
      for (Integer storedWidth : storedWidths) {
        if (!(width > storedWidth - 150 && width < storedWidth + 150)) {
          addElements = true;
        }
      }
    }

    if (addElements) {
      storedWidths.add(width);
      xPathWidthStore.put(e1.getXpath() + e2.getXpath(), storedWidths);
      HashMap<Element, Element> hashmap = errors.get(width);
      if (hashmap == null) {
        hashmap = new HashMap<>();
      }
      hashmap.put(e1, e2);
      errors.put(width, hashmap);
    }
  }

  private boolean shouldCheckError(Element element1, Element element2, int width) {
    for(Map.Entry<Integer, HashMap<Element, Element>> elementEntry : MeaningfulSequence.errors.entrySet()) {
      if (elementEntry.getKey() > width - 150 && elementEntry.getKey() > width + 150 ) {
        for(Map.Entry<Element, Element> elements : elementEntry.getValue().entrySet()) {
          if (element1.equals(elements.getKey()) && element2.equals(elements.getValue())) {
            return false;
          }
        }

      }
    }
    return true;
  }

  @Override
  public WebDriver checkIssue(Element element, HashMap<String, Element> otherElements, int width, WebDriver webDriver, ResponsiveLayoutGraph r, String fullUrl, ArrayList<Integer> breakpoints, HashMap<Integer, LayoutFactory> lFactories, int vmin, int vmax) {
    if (!element.getInHead()) {

      System.out.println("***** visual layout matches source");

      // Loop all elements compare position on page to position in code
      Element holder;
      for (Map.Entry<String, Element> elementEntry : otherElements.entrySet()) {
        if (!elementEntry.equals(element)) {
          holder = elementEntry.getValue();

          if (shouldCheckError(element, holder, width) && !elementMatchesCodePositionToElement(element, holder) ) {
            element.setParentWidth(width);
            addError(width, element, holder);
          }
        }
      }
    }
    return webDriver;
  }

  private boolean elementMatchesCodePositionToElement(Element e1, Element e2) {
    if (e1.getLineNumber() > e2.getLineNumber()) {

      int yDiff = (e1.getY1() - e2.getY2());
      int lineDiff = Math.abs(e1.getLineNumber() - e2.getLineNumber());

      // System.out.println(e1.getY1() + " - " + e2.getY1() + " : " + lineDiff);
      // System.out.println(e1.getDocumentHeight() + " - " + e1.getDocumentLines());
      return yDiff <= (lineDiff * (e1.getDocumentHeight() / e1.getDocumentLines()) * 20);
    }
    return true;
  }

  @Override
  public boolean getDidPass() {
    return didPass;
  }

  @Override
  public String getErrorMessage() {
    StringBuilder output =
        new StringBuilder(
            MeaningfulSequence.errors.size() + " elements in bad positions \n");
    for (Map.Entry<Integer, HashMap<Element, Element>> error : MeaningfulSequence.errors.entrySet()) {
      Integer width = error.getKey();
      for (Map.Entry<Element, Element> errors : error.getValue().entrySet()) {
        Element e1 = errors.getKey();
        Element e2 = errors.getValue();
        output
            .append(e1.getXpath() + " - " + e2.getXpath() + " : " + width)
            .append(" \n");
      }
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
    sheetProperties.setTitle("Visual Layout vs Code");
    sheet.setProperties(sheetProperties);
    List<GridData> grid = new ArrayList<>();
    List<RowData> rowDataList = new ArrayList<>();

    CellData pageTitle = new CellData();
    pageTitle.setUserEnteredValue(new ExtendedValue().setStringValue("Visual Layout vs Code"));

    List<CellData> titleRow = new ArrayList<>();
    titleRow.add(pageTitle);

    rowDataList.add((new RowData()).setValues(titleRow));
    rowDataList.add(null);

    int i = 1;
    for (Map.Entry<Integer, HashMap<Element, Element>> error :
        MeaningfulSequence.errors.entrySet()) {
      Integer width = error.getKey();
      for (Map.Entry<Element, Element> errors : error.getValue().entrySet()) {
        Element element = errors.getKey();
        Element element2 = errors.getValue();
        // System.out.println(element.getXpath());
        CellData elementXPath = new CellData();
        elementXPath.setUserEnteredValue(new ExtendedValue().setStringValue(element.getXpath()));

        CellData element2XPath = new CellData();
        element2XPath.setUserEnteredValue(new ExtendedValue().setStringValue(element2.getXpath()));

        CellData issueID = new CellData();
        issueID.setUserEnteredValue(new ExtendedValue().setStringValue(String.valueOf(i)));

        CellData elementLineNumber = new CellData();
        elementLineNumber.setUserEnteredValue(
            new ExtendedValue().setStringValue(String.valueOf(element.getLineNumber())));

        CellData element2LineNumber = new CellData();
        element2LineNumber.setUserEnteredValue(
            new ExtendedValue().setStringValue(String.valueOf(element2.getLineNumber())));

        CellData elementYPosition = new CellData();
        elementYPosition.setUserEnteredValue(
            new ExtendedValue().setStringValue(String.valueOf(element.getY1())));

        CellData element2YPosition = new CellData();
        element2YPosition.setUserEnteredValue(
            new ExtendedValue().setStringValue(String.valueOf(element2.getY2())));

        CellData parentWidth = new CellData();
        parentWidth.setUserEnteredValue(new ExtendedValue().setStringValue(String.valueOf(width)));

        List<CellData> row = new ArrayList<>();
        row.add(issueID);
        row.add(elementXPath);
        row.add(element2XPath);
        row.add(elementLineNumber);
        row.add(element2LineNumber);
        row.add(elementYPosition);
        row.add(element2YPosition);
        row.add(parentWidth);

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
