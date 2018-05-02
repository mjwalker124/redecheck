package shef.accessibility;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.reflections.Reflections;
import shef.layout.Element;
import shef.layout.LayoutFactory;
import shef.reporting.inconsistencies.ResponsiveLayoutFailure;
import shef.rlg.Node;
import shef.rlg.ResponsiveLayoutGraph;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/** Created by Matthew Walker on 7/02/2018. Based on the RLGAnalyser */
public class AccessibilityAnalyser {
  ResponsiveLayoutGraph rlg;
  ArrayList<ResponsiveLayoutFailure> errors;
  WebDriver driver;
  String url;
  ArrayList<Integer> bpoints;
  ArrayList<Node> onePixelOverflows;
  HashMap<Integer, LayoutFactory> layouts;
  int vmin, vmax;

  /**
   * This is the basic initialisation for the AccessibilityAnalyser
   * @param r               This is the responsive layout graph, it's passed in for if the RLGAnalyser is needed
   * @param webDriver       This is the selenium web driver, this allows for control over the DOM and for standard selenium tests to be run
   * @param fullUrl         This is the full url of the website being tested
   * @param breakpoints     These are any breakpoints, these are only used if the RLGAnalyser is needed
   * @param lFactories      These are the layout factories, these are only used if the RLGAnalyser is needed
   * @param vmin            This is only used if the RLGAnalyser is needed
   * @param vmax            This is only used if the RLGAnalyser is needed
   */
  public AccessibilityAnalyser(
      ResponsiveLayoutGraph r,
      WebDriver webDriver,
      String fullUrl,
      ArrayList<Integer> breakpoints,
      HashMap<Integer, LayoutFactory> lFactories,
      int vmin,
      int vmax) {
    rlg = r;
    driver = webDriver;
    url = fullUrl;
    bpoints = breakpoints;
    onePixelOverflows = new ArrayList<>();
    layouts = lFactories;
    this.vmin = vmin;
    this.vmax = vmax;
    errors = new ArrayList<>();
  }


    /**
     * This loops through all of the layouts, and loops through all of the issues for each layout, it then checks if a
     * check is needed, and then runs it on all of the elements
     * @return This returns a list of all of the accessibility issue check classes, these will have all of the errors
     * stored within them.
     */
  public List<IAccessibilityIssue> analyse() {
    System.out.println("Start Accessibility Run");
    List<IAccessibilityIssue> issues = getAccessibilityIssues();
    int counter = 1;
    // Loop through all layouts generated
    for (Map.Entry<Integer, LayoutFactory> entry : layouts.entrySet()) {
      System.out.println("Layout test : " + counter + "/" + layouts.size());
      counter++;
      Integer key = entry.getKey();
      LayoutFactory factory = entry.getValue();
      HashMap<String, Element> elements = factory.layout.getElements();
      for (IAccessibilityIssue issue : issues) {

        // For the layout loop through all elements and test
        // Only run tests if it is necessary.
        if (issue.isAffectedByLayouts() || (!issue.isAffectedByLayouts() && issue.numberOfTimesTested() == 0 && key > 1000)) {
          System.out.println(issue.getClass().getName() + " Width: " + key);
          for (Map.Entry<String, Element> elementEntry : elements.entrySet()) {
            String elementKey = elementEntry.getKey();
            Element element = elementEntry.getValue();

            driver.manage().window().setSize(new Dimension(key, driver.manage().window().getSize().height));
            driver = issue.checkIssue(element, elements, key, driver, rlg, url, bpoints, layouts, vmin, vmax );
            issue.incNumberOfTimesTested();
          }
        }
      }
    }

    return issues;
  }


    /**
     * This writes and stores a text report of all of the accessibility issues
     * @param url       This is the url that is being tested
     * @param issues    This is the list of issues which have been tested
     * @param ts        This is the timestamp used within the report folder structure
     */
  public void writeReport(String url, List<IAccessibilityIssue> issues, String ts) {
    PrintWriter output = null;
    PrintWriter output2 = null;
    PrintWriter output3 = null;
    try {
      File outputFile = null;
      if (!url.contains("www.") && (!url.contains("http://"))) {
        String[] splits = url.split("/");
        String webpage = splits[0];
        String mutant = "index-" + ts;
        //                    splits[1];
        try {
          outputFile =
              new File(
                  new File(".").getCanonicalPath() + "/../reports/" + webpage + "/" + mutant + "/");
        } catch (IOException e) {
          e.printStackTrace();
        }
      } else if (url.contains("http://")) {
        String[] splits = url.split("http://");
        String webpage = splits[1];
        String mutant = ts;
        try {
          outputFile =
              new File(
                  new java.io.File(".").getCanonicalPath()
                      + "/../reports/"
                      + webpage
                      + "/"
                      + mutant
                      + "/");
        } catch (IOException e) {
          e.printStackTrace();
        }
      } else {
        String[] splits = url.split("www.");
        String webpage = splits[1];
        String mutant = ts;
        try {
          outputFile =
              new File(
                  new File(".").getCanonicalPath() + "/../reports/" + webpage + "/" + mutant + "/");
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      FileUtils.forceMkdir(outputFile);
      File dir = new File(outputFile + "/accessibility-report.txt");

      output = new PrintWriter(dir);
      //            output2 = new PrintWriter(countDir);
      //            output3 = new PrintWriter(typeFile);
      if (issues.size() > 0) {
        //                output2.append(Integer.toString(errors.size()));
        for (IAccessibilityIssue issue : issues) {
          output.append(issue.getErrorMessage() + "\n\n");
          //                    output3.append(errorToKey(rle) + "\n");
        }
      } else {
        output.append("NO ACCESSIBILITY ISSUES TESTED.");
        //                output2.append("0");
      }

      output.close();
      //            output2.close();
      //            output3.close();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }


    /**
     * This gets a list of accessibility issues which need to be tested.
     * In order to do this I am using reflection.
     * @return      A list of all of the accessibility issues inside shef.accessibility.
     */
  private List<IAccessibilityIssue> getAccessibilityIssues() {
    List<IAccessibilityIssue> issues = new ArrayList<>();
    Reflections reflections = new Reflections("shef.accessibility");
    Set<Class<? extends IAccessibilityIssue>> classes =
        reflections.getSubTypesOf(IAccessibilityIssue.class);
    for (Class<? extends IAccessibilityIssue> cls : classes)
        try {
            issues.add(cls.newInstance());
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }

    return issues;
  }
}
