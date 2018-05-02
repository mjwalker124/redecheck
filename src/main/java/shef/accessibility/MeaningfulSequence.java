package shef.accessibility;

import com.google.api.services.sheets.v4.model.*;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.WebDriver;
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

public class MeaningfulSequence implements IAccessibilityIssue {

    private static HashMap<Integer, HashMap<Element, Element>> errors = new HashMap<>();
    private static HashMap<String, List<Integer>> xPathWidthStore = new HashMap<>();
    public int[] coords;
    private String xpath;
    private Boolean didPass = true;
    private int numberOfTimesTested = 0;
    private int numberOfErrors = 0;
    private int x1, x2, y1, y2;
    private int max, min;
    private boolean cloudReportGenerated = false;


    public static HashMap<Integer, HashMap<Element, Element>> getErrors() {
        return errors;
    }

    @Override
    public void captureScreenshotExample(
            int errorID, String url, WebDriver webDriver, String fullurl, String timeStamp) {
        // This generates a screenshot per width and error.
        for (Map.Entry<Integer, HashMap<Element, Element>> error :
                MeaningfulSequence.errors.entrySet()) {
            try {
                int captureWidth = error.getKey();
                HashMap<Integer, LayoutFactory> lfs = new HashMap<>();
                int errorNumber = 0;
                for (Map.Entry<Element, Element> internalError : error.getValue().entrySet()) {
                    BufferedImage img;
                    Element e1 = internalError.getKey();
                    Element e2 = internalError.getValue();
                    img = RLGExtractor.getScreenshot(captureWidth, errorID, lfs, webDriver, url);

                    Graphics2D g2d = img.createGraphics();

                    g2d.setColor(Color.BLUE);
                    g2d.setStroke(new BasicStroke(3));
                    int coords[] = e1.getBoundingCoords();
                    g2d.drawRect(coords[0], coords[1], coords[2] - coords[0], coords[3] - coords[1]);
                    g2d.setColor(Color.RED);
                    int coords2[] = e2.getBoundingCoords();
                    g2d.drawRect(coords2[0], coords2[1], coords2[2] - coords2[0], coords2[3] - coords2[1]);
                    g2d.dispose();

                    try {
                        File output = Utils.getOutputFilePath(url, timeStamp, errorID, true);
                        FileUtils.forceMkdir(output);
                        Boolean makeFolders = new File(output + "/MeaningfulSequence").mkdir();
                        ImageIO.write(
                                img,
                                "png",
                                new File(
                                        output
                                                + "/MeaningfulSequence/"
                                                + captureWidth + "-" + errorNumber
                                                +
                                                ".png"));
                        errorNumber++;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            } catch (NullPointerException npe) {
                npe.printStackTrace();
                System.out.println("Could not find one of the offending elements in screenshot.");
            }
        }
    }

    private void addError(Integer width, Element e1, Element e2) {
        numberOfErrors++;
        //To store errors, we store two elements against one width.  This is done by using a hashmap inside a hashmap.
        HashMap<Element, Element> hashmap = errors.get(width);
        if (hashmap == null) {
            hashmap = new HashMap<>();
        }
        hashmap.put(e1, e2);
        errors.put(width, hashmap);

    }

    private boolean shouldCheckError(Element element1, Element element2, int width) {
        List<Integer> currentWidthsTested = xPathWidthStore.get(element1.getXpath() + element2.getXpath());
        //This checks to see if a check is needed, first checking if the elements have ever been tested against each other
        // if they have then it goes through all widths stored for those xpaths and checks to see whether the width is
        // within a 150px either side range
        if (currentWidthsTested != null) {
            for (Integer testedWidth : currentWidthsTested) {
                if (testedWidth < width + 150 && testedWidth > width - 150) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public WebDriver checkIssue(Element element, HashMap<String, Element> otherElements, int width, WebDriver webDriver, ResponsiveLayoutGraph r, String fullUrl, ArrayList<Integer> breakpoints, HashMap<Integer, LayoutFactory> lFactories, int vmin, int vmax) {
        if (!element.getInHead()) {

            // Loop all elements compare position on page to position in code
            if (element.getParent() != null) {
                for (Element sibling : element.getParent().getChildren()) {
                    if (!element.equals(sibling)) {
                        if (shouldCheckError(element, sibling, width) && !elementMatchesCodePositionToElement(element, sibling)) {
                            element.setParentWidth(width);
                            addError(width, element, sibling);
                        }

                        List<Integer> storedWidths = new ArrayList<>();
                        if (xPathWidthStore.containsKey(element.getXpath() + sibling.getXpath())) {
                            storedWidths = xPathWidthStore.get(element.getXpath() + sibling.getXpath());
                        }
                        storedWidths.add(width);
                        xPathWidthStore.put(element.getXpath() + sibling.getXpath(), storedWidths);
                    }
                }
            }
        }
        return webDriver;
    }

    private boolean elementMatchesCodePositionToElement(Element e1, Element e2) {
        //This returns whether one element is higher in the code than the other, and higher on the screen than the other
        return !(e1.getLineNumber() > e2.getLineNumber() && (e1.getY1() < e2.getY1()));
    }

    @Override
    public String getErrorMessage() {
        StringBuilder output =
                new StringBuilder(
                        MeaningfulSequence.errors.size() + " elements in bad positions \n");
        for (Map.Entry<Integer, HashMap<Element, Element>> error : MeaningfulSequence.errors.entrySet()) {
            Integer width = error.getKey();
            for (Map.Entry<Element, Element> errors : error.getValue().entrySet()) {
                Element e1 = errors.getKey();
                Element e2 = errors.getValue();
                output
                        .append(e1.getXpath() + " - " + e2.getXpath() + " : " + width)
                        .append(" \n");
            }
        }

        return output.toString();
    }

    @Override
    public List<RowData> getOverviewRow() {
        List<RowData> rowDataList = new ArrayList<>();
        CellData rowTitle = Utils.generateCellData("Number Of out of order Elements", true);
        CellData rowValue = Utils.generateCellData(Integer.toString(numberOfErrors));
        List<CellData> titleRow = new ArrayList<>();
        titleRow.add(rowTitle);
        titleRow.add(rowValue);
        rowDataList.add((new RowData()).setValues(titleRow));

        return rowDataList;
    }

    @Override
    public String getFixInstructions() {
        return null;
    }

    @Override
    public boolean isAffectedByLayouts() {
        return true;
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
        sheetProperties.setTitle("Meaningful Sequence");
        sheet.setProperties(sheetProperties);
        List<GridData> grid = new ArrayList<>();
        List<RowData> rowDataList = new ArrayList<>();

        CellData pageTitle =  Utils.generateCellData("Meaningful Sequence", true, true);

        List<CellData> titleRow = new ArrayList<>();
        titleRow.add(pageTitle);


        CellData errorNumberTitle = Utils.generateCellData("ID", true);
        CellData xPathTitle =  Utils.generateCellData("Element 1 XPath", true);
        CellData element1IDTitle =  Utils.generateCellData("Element 1 ID", true);
        CellData xPath2Title =  Utils.generateCellData("Element 2 XPath", true);
        CellData element2IDTitle =  Utils.generateCellData("Element 2 ID", true);
        CellData lineNumber =  Utils.generateCellData("Element 1 Estimated Line Number", true);
        CellData line2Number =  Utils.generateCellData("Element 2 Estimated Line Number", true);
        CellData y1Position =  Utils.generateCellData("Element 1 Y Coordinate", true);
        CellData y2Position =  Utils.generateCellData("Element 2 Y Coordinate", true);
        CellData screenWidthTitle =  Utils.generateCellData("Screen Width", true);

        List<CellData> headingRow = new ArrayList<>();
        headingRow.add(errorNumberTitle);
        headingRow.add(xPathTitle);
        headingRow.add(element1IDTitle);
        headingRow.add(xPath2Title);
        headingRow.add(element2IDTitle);
        headingRow.add(lineNumber);
        headingRow.add(line2Number);
        headingRow.add(y1Position);
        headingRow.add(y2Position);
        headingRow.add(screenWidthTitle);
        rowDataList.add((new RowData()).setValues(titleRow));
        rowDataList.add((new RowData()).setValues(headingRow));
        rowDataList.add(null);

        int i = 1;
        for (Map.Entry<Integer, HashMap<Element, Element>> error :
                MeaningfulSequence.errors.entrySet()) {
            Integer width = error.getKey();
            for (Map.Entry<Element, Element> errors : error.getValue().entrySet()) {
                Element element = errors.getKey();
                Element element2 = errors.getValue();
                // System.out.println(element.getXpath());
                CellData elementXPath = Utils.generateCellData(element.getXpath());
                CellData element2XPath = Utils.generateCellData(element2.getXpath());
                CellData elementID = Utils.generateCellData(element.getAttr("id"));
                CellData element2ID = Utils.generateCellData(element2.getAttr("id"));
                CellData issueID = Utils.generateCellData(String.valueOf(i));
                CellData elementLineNumber = Utils.generateCellData(String.valueOf(element.getLineNumber()));
                CellData element2LineNumber = Utils.generateCellData(String.valueOf(element2.getLineNumber()));
                CellData elementYPosition = Utils.generateCellData(String.valueOf(element.getY1()));
                CellData element2YPosition = Utils.generateCellData(String.valueOf(element2.getY2()));
                CellData parentWidth = Utils.generateCellData(String.valueOf(width));

                List<CellData> row = new ArrayList<>();
                row.add(issueID);
                row.add(elementXPath);
                row.add(elementID);
                row.add(element2XPath);
                row.add(element2ID);
                row.add(elementLineNumber);
                row.add(element2LineNumber);
                row.add(elementYPosition);
                row.add(element2YPosition);
                row.add(parentWidth);

                RowData rowData = new RowData();
                rowData.setValues(row);

                rowDataList.add(rowData);
                i++;
            }
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
