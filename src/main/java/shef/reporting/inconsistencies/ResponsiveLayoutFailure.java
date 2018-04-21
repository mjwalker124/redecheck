package shef.reporting.inconsistencies;

import org.openqa.selenium.WebDriver;
import shef.rlg.Node;

import java.awt.image.BufferedImage;
import java.util.HashSet;

/** Created by thomaswalsh on 31/05/2016. */
public abstract class ResponsiveLayoutFailure {
  public abstract BufferedImage captureScreenShot(
          int errorID, WebDriver webDriver, String fullUrl);

  public abstract void captureScreenshotExample(
      int errorID, String url, WebDriver webDriver, String fullUrl, String timeStamp);

  public abstract HashSet<Node> getNodes();

  public abstract int[] getBounds();

  public abstract int getWindowWidth();
}
