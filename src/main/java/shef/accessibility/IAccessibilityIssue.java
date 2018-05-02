package shef.accessibility;

import com.google.api.services.sheets.v4.model.RowData;
import com.google.api.services.sheets.v4.model.Sheet;
import org.openqa.selenium.WebDriver;
import shef.layout.Element;
import shef.layout.LayoutFactory;
import shef.rlg.ResponsiveLayoutGraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * This is the interface which all accessibility checks need to implement.
 * The system uses reflection in the AccessibilityAnalyser class to make sure that all classes which implement this
 * interface will be run.
 */
public interface IAccessibilityIssue {

    /**
     * This method needs to go through all of the errors which have been found, and output them on a screenshot of the
     * website.  At the moment it does the image generation, and saving at the same time.
     * In the future it may be worth seperating them out so that it will be a bit neater and could potentially store the
     * image both locally and remotly.  This will be particularly useful when google sheets allows images to be added as blobs.
     * @param errorID      This is required from ReDeCheck code which is used, it is unimportant though
     * @param url          This is the short form of the url which will be used in saving the image
     * @param webDriver    This is the webdriver which is used to get the screenshots from the page
     * @param fullUrl      This is the long form of the url which will be used in saving the image
     * @param timeStamp    This is the timestamp that the test has been run at for saving the image to the correct folder.
     */
    void captureScreenshotExample(
            int errorID, String url, WebDriver webDriver, String fullUrl, String timeStamp);

    /**
     * This method is the logic behind the check, it is called inside the AccessibilityAnalyser automatically, and it is
     * run on every element in the page.  This will return the webdriver that was passed in so that the webdriver stays
     * in sync.  It should store any errors it finds in a suitable manner inside the object.  It is not dictated as to
     * how that should happen as different checks will require different ways of storing the data.
     * @param element           This is the element which is currently the focus of the test
     * @param otherElements     These are all of the other elements in the page which the current element can be tested against if necessary
     * @param width             This is the current width of the screen at the time of the test running
     * @param webDriver         This is the selenium webdriver, this allows you to manipulate the dom while testing, please remember to reset any changes you make so as to not run the rest of the tests on the modified website
     * @param r                 This is the responsivelayoutgraph of the website
     * @param fullUrl           This is the full url of the website being tested
     * @param breakpoints       These are all of the breakpoints these will be needed if you wish to run the main ReDeCheck tests as part of an accessibility test
     * @param lFactories        These are all of the lFactories again these will be needed if you wish to run the main ReDeCheck tests as part of an accessibility test
     * @param vmin              This is necessary if you wish to run the main ReDeCheck tests as part of an accessibility test
     * @param vmax              This is necessary if you wish to run the main ReDeCheck tests as part of an accessibility test
     * @return                  This is the WebDriver, it should just be the webDriver that was passed in
     */
    WebDriver checkIssue(Element element, HashMap<String, Element> otherElements, int width, WebDriver webDriver, ResponsiveLayoutGraph r, String fullUrl, ArrayList<Integer> breakpoints, HashMap<Integer, LayoutFactory> lFactories, int vmin, int vmax);


    /**
     * @return      This returns a text error message which can be used in a log file
     */
    String getErrorMessage();


    /**
     * @return      This will return text to explain to the user how to fix this issue
     */
    String getFixInstructions();


    /**
     * @return      This will return a sumary row for the google sheets so that the user can see a quick overview of what
     * the check has found
     * Example: Number of Issues Found: 20
     */
    List<RowData> getOverviewRow();


    /**
     * @return      This is to be set by the programmer and will affect when the check is run.  If the check is affected by
     * layouts then the test will be run on every screen width that has been tested by ReDeCheck, other wise it will only be run once.
     */
    boolean isAffectedByLayouts();


    /**
     * @return      This will get the number of times the check has been run.
     */
    int numberOfTimesTested();


    /**
     * This is used automatically to increment the number of times that a test has been run, it is called inside the
     * AccessibilityAnayliser
     */
    void incNumberOfTimesTested();


    /**
     * @return      This needs to return a Google Sheet of all of the issues that have been detected and stored inside
     * this object.
     */
    Sheet generateCloudReport();

    /**
     * @return      This will return whether the google sheet has been made.
     */
    boolean cloudReportMade();
}
