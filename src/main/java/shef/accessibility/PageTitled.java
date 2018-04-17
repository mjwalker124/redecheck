package shef.accessibility;

import com.google.api.services.sheets.v4.model.Sheet;
import org.openqa.selenium.WebDriver;
import shef.layout.Element;

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

  @Override
  public void captureScreenshotExample(
      int errorID, String url, WebDriver webDriver, String fullurl, String timeStamp) {}

  @Override
  public void checkIssue(Element element, HashMap<String, Element> otherElements, int width) {
    System.out.println("***** title test");
    if (element.getTag().equalsIgnoreCase("title")) {
      didPass = true;
    }
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
