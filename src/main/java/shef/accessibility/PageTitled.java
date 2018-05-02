package shef.accessibility;

import com.google.api.services.sheets.v4.model.CellData;
import com.google.api.services.sheets.v4.model.RowData;
import com.google.api.services.sheets.v4.model.Sheet;
import org.openqa.selenium.WebDriver;
import shef.layout.Element;
import shef.layout.LayoutFactory;
import shef.main.Utils;
import shef.rlg.ResponsiveLayoutGraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PageTitled implements IAccessibilityIssue {

  private static List<Element> errors = new ArrayList<>();
  private Boolean didPass = false;
  private int numberOfTimesTested = 0;
  private int width;
  private boolean cloudReportGenerated = false;

  public static List<Element> getErrors() {
    return errors;
  }

  //Capture Screenshot is empty as there is no way of showing this on a screen shot
  @Override
  public void captureScreenshotExample(
          int errorID, String url, WebDriver webDriver, String fullurl, String timeStamp) {}

  @Override
  public WebDriver checkIssue(Element element, HashMap<String, Element> otherElements, int width, WebDriver webDriver, ResponsiveLayoutGraph r, String fullUrl, ArrayList<Integer> breakpoints, HashMap<Integer, LayoutFactory> lFactories, int vmin, int vmax) {
    //This checks to see if the element is the title, if it is then the test has passed.  This doesn't get set to false as didPass defaults to false so will only pass if a title is found
    if (element.getTag().equalsIgnoreCase("title")) {
      didPass = true;
    }
    return webDriver;
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
  public List<RowData> getOverviewRow() {
    List<RowData> rowDataList = new ArrayList<>();
    CellData rowTitle = Utils.generateCellData("Page has title:", true);
    CellData rowValue = Utils.generateCellData((didPass ? "Yes" : "No"));
    List<CellData> titleRow = new ArrayList<>();
    titleRow.add(rowTitle);
    titleRow.add(rowValue);
    rowDataList.add((new RowData()).setValues(titleRow));

    return rowDataList;
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

  //This is returning null as there is no way to display this in a whole sheet, it is better of in the overall view page.
  @Override
  public Sheet generateCloudReport() {
    return null;
  }

  @Override
  public boolean cloudReportMade() {
    return cloudReportGenerated;
  }
}
