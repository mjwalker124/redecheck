package shef.accessibility;

import com.google.api.services.drive.Drive;
import com.google.api.services.sheets.v4.model.Sheet;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.openqa.selenium.*;
import shef.analysis.RLGAnalyser;
import shef.handlers.CloudReporting;
import shef.layout.Element;
import shef.layout.LayoutFactory;
import shef.main.RLGExtractor;
import shef.main.Utils;
import shef.reporting.inconsistencies.ResponsiveLayoutFailure;
import shef.rlg.ResponsiveLayoutGraph;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ResizeText implements IAccessibilityIssue {

    private static List<Element> errors = new ArrayList<>();
    private static List<ResponsiveLayoutFailure> responsiveLayoutFailures = new ArrayList<>();
    private Boolean didPass = true;
    private int numberOfTimesTested = 0;
    private int width;
    private boolean cloudReportGenerated = false;
    public static List<Element> getErrors() {
        return errors;
    }
    private boolean testedResponsiveness = false;
    String[] tagsWhichTheFontsizeMustChange = { "H1", "H2", "H3", "H4","H5", "H6","A", "LABEL", "DIV", "P", "SPAN" };

    @Override
    public void captureScreenshotExample(
            int errorID, String url, WebDriver webDriver, String fullurl, String timeStamp) {
        try {
            int captureWidth = width;
            HashMap<Integer, LayoutFactory> lfs = new HashMap<>();

            BufferedImage img;
            img = RLGExtractor.getScreenshot(captureWidth, errorID, lfs, webDriver, url);

            Graphics2D g2d = img.createGraphics();
            for (Element e : ResizeText.errors) {
                g2d.setColor(Color.RED);
                g2d.setStroke(new BasicStroke(3));
                int coords[] = e.getBoundingCoords();
                g2d.drawRect(coords[0], coords[1], coords[2] - coords[0], coords[3] - coords[1]);
                System.out.println("Draw " + e.getTag());
            }

            g2d.dispose();
            try {
                File output = Utils.getOutputFilePath(url, timeStamp, errorID, true);
                FileUtils.forceMkdir(output);
                Boolean makeFolders = new File(output + "/ResizeText").mkdir();

                ImageIO.write(img, "png", new File(output + "/ResizeText/TextDidNotChange.png"));

            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (NullPointerException npe) {
            npe.printStackTrace();
            System.out.println("Could not find one of the offending elements in screenshot.");
        }


        int i = 0;
        for(ResponsiveLayoutFailure responsiveLayoutFailure : ResizeText.responsiveLayoutFailures) {
            BufferedImage img = responsiveLayoutFailure.captureScreenShot(errorID,webDriver, fullurl);

            try {
                File output = Utils.getOutputFilePath(url, timeStamp, errorID, true);
                FileUtils.forceMkdir(output);
                Boolean makeFolders = new File(output + "/ResizeText").mkdir();
                ImageIO.write(img, "png", new File(output + "/ResizeText/Responsive Error"+i+".png"));

            } catch (IOException e) {
                e.printStackTrace();
            }
            i++;
        }

    }

    @Override
    public WebDriver checkIssue(Element element, HashMap<String, Element> otherElements, int width, WebDriver webDriver, ResponsiveLayoutGraph r, String fullUrl, ArrayList<Integer> breakpoints, HashMap<Integer, LayoutFactory> lFactories, int vmin, int vmax) {
        System.out.println("***** resize test");
        this.width = width;
        WebElement htmlBaseElement = webDriver.findElement(By.tagName("html"));
        JavascriptExecutor js = (JavascriptExecutor) webDriver;

        String initialFontsize = webDriver.findElement(By.xpath(element.getXpath())).getCssValue("font-size");
        js.executeScript("arguments[0].setAttribute('style', 'font-size:200%;')", htmlBaseElement);

        String postFontsize = webDriver.findElement(By.xpath(element.getXpath())).getCssValue("font-size");
    if (initialFontsize.equalsIgnoreCase(postFontsize) && (ArrayUtils.contains(tagsWhichTheFontsizeMustChange,  element.getTag().toUpperCase()))) {
            didPass = false;
            errors.add(element);
        } else {
            System.out.println("Fontsize did change" + element.getTag());
            if (!testedResponsiveness ) {
                RLGAnalyser analyser =
                    new RLGAnalyser(r, webDriver, fullUrl, breakpoints, lFactories, vmin, vmax);

                responsiveLayoutFailures = analyser.analyse();
                if (responsiveLayoutFailures.size() > 0) {
                  didPass = false;
                }
                testedResponsiveness = true;
            }
        }

        js.executeScript("arguments[0].removeAttribute('style')", htmlBaseElement);

        return webDriver;
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
