package shef.accessibility;

import com.google.api.services.sheets.v4.model.Sheet;
import org.openqa.selenium.WebDriver;
import shef.layout.Element;

import java.util.HashMap;

public interface IAccessibilityIssue {
  void captureScreenshotExample(
      int errorID, String url, WebDriver webDriver, String fullUrl, String timeStamp);

  void checkIssue(Element element, HashMap<String, Element> otherElements, int width);

  boolean getDidPass();

  String getErrorMessage();

  String getFixInstructions();

  String consoleOutput();

  boolean isAffectedByLayouts();

  int numberOfTimesTested();

  void incNumberOfTimesTested();

  Sheet generateCloudReport();

  boolean cloudReportMade();
}
