package shef.accessibility;

import com.google.api.services.sheets.v4.model.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.openqa.selenium.*;
import shef.analysis.RLGAnalyser;
import shef.layout.Element;
import shef.layout.LayoutFactory;
import shef.main.RLGExtractor;
import shef.main.Utils;
import shef.reporting.inconsistencies.ResponsiveLayoutFailure;
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
        // This generates one image with all of the issues highlighted on it
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
        //This also stores all of the responsive layout failures caused by the text resize
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
        // This forces the fontsize for the whole page to 200% this simulates browsers font size % setting
        // It then checks to see if the font size has changed
        // If the fontsize has changed then the website is tested with the rlganalyser.  This checks for layout issues
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
    public List<RowData> getOverviewRow() {

        List<RowData> rowDataList = new ArrayList<>();

        CellData rowTitle = Utils.generateCellData("Number Of Text Elements which could not resize", true);

        CellData rowValue = Utils.generateCellData(Integer.toString(errors.size()));

        List<CellData> titleRow = new ArrayList<>();
        titleRow.add(rowTitle);
        titleRow.add(rowValue);
        rowDataList.add((new RowData()).setValues(titleRow));

        return rowDataList;
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
        sheetProperties.setTitle("Font Size Static Issues");
        sheet.setProperties(sheetProperties);
        List<GridData> grid = new ArrayList<>();
        List<RowData> rowDataList = new ArrayList<>();

        CellData pageTitle =  Utils.generateCellData("Font Size Static", true, true);

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
        for (Element element : ResizeText.errors) {
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
