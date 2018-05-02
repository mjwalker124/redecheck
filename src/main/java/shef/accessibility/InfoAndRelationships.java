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

public class InfoAndRelationships implements IAccessibilityIssue {

  private static List<Element> errors = new ArrayList<>();
  private Boolean didPass = true;
  private int numberOfTimesTested = 0;
  private int width;
  private boolean cloudReportGenerated = false;
  private static HashMap<String, Element> idMap = new HashMap<>();
  private boolean idMapGenerated = false;

  public static List<Element> getErrors() {
    return errors;
  }

  @Override
  public void captureScreenshotExample(
      int errorID, String url, WebDriver webDriver, String fullurl, String timeStamp) {
    //This outputs all of the errors on one image.
    try {
      int captureWidth = width;
      HashMap<Integer, LayoutFactory> lfs = new HashMap<>();

      BufferedImage img;
      img = RLGExtractor.getScreenshot(captureWidth, errorID, lfs, webDriver, url);

      Graphics2D g2d = img.createGraphics();
      for (Element e : InfoAndRelationships.errors) {
        g2d.setColor(Color.RED);
        g2d.setStroke(new BasicStroke(3));
        int coords[] = e.getBoundingCoords();
        g2d.drawRect(coords[0], coords[1], coords[2] - coords[0], coords[3] - coords[1]);
      }

      g2d.dispose();
      try {
        File output = Utils.getOutputFilePath(url, timeStamp, errorID, true);
        FileUtils.forceMkdir(output);
        ImageIO.write(img, "png", new File(output + "/InfoAndRelationships" + ".png"));
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
    if(!idMapGenerated) {
        //If there isn't already an idMap then create on.  This stores the element against the id for all elements in the page this makes it possible to search by id using O(1) since it is a hashmap
      for (Map.Entry<String, Element> elementEntry : otherElements.entrySet()) {
        idMap.put(elementEntry.getValue().getAttr("id"), elementEntry.getValue());
      }
    }

    if (!element.getInHead()) {
      this.width = width;
      //This checks to see if a referenced id actually exists in the page.
      if ((element.hasAttribute("aria-labelledby") && (idMap.get(element.getAttr("aria-labelledby")) == null))
              || ((element.hasAttribute("for")) && (idMap.get(element.getAttr("for")) == null)
              )) {
        InfoAndRelationships.errors.add(element);
      }
    }
    return webDriver;
  }

  @Override
  public List<RowData> getOverviewRow() {

    List<RowData> rowDataList = new ArrayList<>();

    CellData rowTitle = Utils.generateCellData("Number Of Elements which refer to a missing element", true);

    CellData rowValue = Utils.generateCellData(Integer.toString(errors.size()));

    List<CellData> titleRow = new ArrayList<>();
    titleRow.add(rowTitle);
    titleRow.add(rowValue);
    rowDataList.add((new RowData()).setValues(titleRow));

    return rowDataList;
  }

  @Override
  public String getErrorMessage() {
    StringBuilder output =
        new StringBuilder(InfoAndRelationships.errors.size() + " Info and Relationship issues \n");
    for (Element element : InfoAndRelationships.errors) {
      output.append(element.getXpath()).append(" \n");
    }

    return output.toString();
  }

  @Override
  public String getFixInstructions() {
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
      sheetProperties.setTitle("Elements referring to a non existent element");
      sheet.setProperties(sheetProperties);
      List<GridData> grid = new ArrayList<>();
      List<RowData> rowDataList = new ArrayList<>();

      CellData pageTitle =  Utils.generateCellData("Elements referring to a non existent element", true, true);

      List<CellData> titleRow = new ArrayList<>();
      titleRow.add(pageTitle);

      CellData errorNumberTitle = Utils.generateCellData("ID", true);
      CellData xPathTitle =  Utils.generateCellData("XPath", true);
      CellData elementIDTitle =  Utils.generateCellData("Element ID", true);
      CellData lineNumber =  Utils.generateCellData("Estimated Line Number", true);

      List<CellData> headingRow = new ArrayList<>();
      headingRow.add(errorNumberTitle);
      headingRow.add(xPathTitle);
      headingRow.add(elementIDTitle);
      headingRow.add(lineNumber);

      rowDataList.add((new RowData()).setValues(titleRow));
      rowDataList.add((new RowData()).setValues(headingRow));
      rowDataList.add(null);

    int i = 1;
    for (Element element : InfoAndRelationships.errors) {
        CellData cellData = Utils.generateCellData(element.getXpath());
        CellData cellData2 = Utils.generateCellData(String.valueOf(i));
        CellData cellData3 = Utils.generateCellData(element.getAttr("id"));
        CellData cellData4 = Utils.generateCellData(element.getLineNumber().toString());
      List<CellData> row = new ArrayList<>();
        row.add(cellData2);
        row.add(cellData);
        row.add(cellData3);
        row.add(cellData4);

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