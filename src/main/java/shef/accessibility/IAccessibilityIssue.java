package shef.accessibility;

import com.google.api.services.sheets.v4.model.Sheet;
import org.openqa.selenium.WebDriver;
import shef.layout.Element;
import shef.layout.LayoutFactory;
import shef.rlg.ResponsiveLayoutGraph;

import java.util.ArrayList;
import java.util.HashMap;

public interface IAccessibilityIssue {
  void captureScreenshotExample(
      int errorID, String url, WebDriver webDriver, String fullUrl, String timeStamp);

  WebDriver checkIssue(Element element, HashMap<String, Element> otherElements, int width, WebDriver webDriver, ResponsiveLayoutGraph r, String fullUrl, ArrayList<Integer> breakpoints, HashMap<Integer, LayoutFactory> lFactories, int vmin, int vmax);

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
