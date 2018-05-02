package shef.accessibility;

import com.google.api.services.drive.Drive;
import com.google.api.services.sheets.v4.model.*;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.WebDriver;
import shef.handlers.CloudReporting;
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

public class ColourContrast implements IAccessibilityIssue {

    private static HashMap<Integer, List<Element>> errors = new HashMap<>();
    private static HashMap<String, List<Integer>> xPathWidthStore = new HashMap<>();
    private int numberOfErrors = 0;

    public int[] coords;
    private String xpath;
    private Boolean didPass = true;
    private int numberOfTimesTested = 0;
    private int x1, x2, y1, y2;
    private int width;
    private boolean cloudReportGenerated = false;

    public static HashMap<Integer, List<Element>> getErrors() {
        return errors;
    }

    @Override
    public void captureScreenshotExample(
            int errorID, String url, WebDriver webDriver, String fullurl, String timeStamp) {

        //Here we are outputting an image for each width with all of the errors for that width being drawn around to highlight them

        for (Map.Entry<Integer, List<Element>> elementEntry : errors.entrySet()) {
            try {
                int captureWidth = elementEntry.getKey();
                HashMap<Integer, LayoutFactory> lfs = new HashMap<>();

                BufferedImage img;
                img = RLGExtractor.getScreenshot(captureWidth, errorID, lfs, webDriver, url);

                Graphics2D g2d = img.createGraphics();
                for (Element e : elementEntry.getValue()) {
                    g2d.setColor(Color.RED);
                    g2d.setStroke(new BasicStroke(3));
                    int coords[] = e.getBoundingCoords();
                    g2d.drawRect(coords[0], coords[1], coords[2] - coords[0], coords[3] - coords[1]);
                }

                g2d.dispose();
                try {
                    File output = Utils.getOutputFilePath(url, timeStamp, errorID, true);
                    FileUtils.forceMkdir(output);
                    Boolean makeFolders = new File(output + "/ColourContrastIssues").mkdir();
                    ImageIO.write(
                            img, "png", new File(output + "/ColourContrastIssues/" + captureWidth + ".png"));
                } catch (IOException e) {
                    //                e.printStackTrace();
                }
            } catch (NullPointerException npe) {
                npe.printStackTrace();
                System.out.println("Could not find one of the offending elements in screenshot.");
            }
        }
    }

    private void addError(Element element, int width) {
        //This adds an error to a width in the error store.  If necessary a new element list is created so the element
        //can be added to it.
        numberOfErrors++;
        if (ColourContrast.errors.get(width) == null) {
            List<Element> elements = new ArrayList<>();
            elements.add(element);
            ColourContrast.errors.put(width, elements);
        } else {
            ColourContrast.errors.get(width).add(element);
        }
    }

    private boolean shouldCheckError(Element element, int width) {
        //This checks through all of the error widths to see if a test is required
        List<Integer> storedWidths = xPathWidthStore.get(element.getXpath());

        if (storedWidths != null) {
            for (Integer storedWidth : storedWidths) {
                if ((width > storedWidth - 150 && width < storedWidth + 150)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public List<RowData> getOverviewRow() {
        List<RowData> rowDataList = new ArrayList<>();

        CellData rowTitle = Utils.generateCellData("Elements with a Contrast Issue", true);
        CellData rowValue = Utils.generateCellData(Integer.toString(numberOfErrors));

        List<CellData> titleRow = new ArrayList<>();
        titleRow.add(rowTitle);
        titleRow.add(rowValue);
        rowDataList.add((new RowData()).setValues(titleRow));

        return rowDataList;
    }

    @Override
    public WebDriver checkIssue(Element element, HashMap<String, Element> otherElements, int width, WebDriver webDriver, ResponsiveLayoutGraph r, String fullUrl, ArrayList<Integer> breakpoints, HashMap<Integer, LayoutFactory> lFactories, int vmin, int vmax) {
        this.width = width;

        if (!element.getInHead() && shouldCheckError(element, width)) {
            Double backgroundLuminance = luminanceCalculator(element.getActualBackgroundColour());
            Double foregroundLuminance = luminanceCalculator(element.getActualForegroundColour());

            //This checks the contrasts and the fontsize to follow the wcag for seeing if there is an error
            if (!element.getText().equals("") && ((element.getFontSize() < 24
                    && luminanceContrast(backgroundLuminance, foregroundLuminance) < 4.5)
                    || (element.getFontSize() >= 24
                    && luminanceContrast(backgroundLuminance, foregroundLuminance) < 3))) {
                addError(element, width);
            }
            List<Integer> storedWidths = new ArrayList<>();
            if (xPathWidthStore.containsKey(element.getXpath())) {
                storedWidths = xPathWidthStore.get(element.getXpath());
            }
            storedWidths.add(width);
            xPathWidthStore.put(element.getXpath(), storedWidths);

        }
        return webDriver;
    }

    private Double luminanceCalculator(Double[] colour) {
        //This is an implementation of the algorithm found https://www.w3.org/WAI/GL/wiki/Relative_luminance
        Double[] colourCalcs = new Double[3];
        for (int i = 0; i < 3; i++) {
            if (colour[i] <= 0.03928) {
                colourCalcs[i] = colour[i] / 12.92;
            } else {
                colourCalcs[i] = Math.pow(((colour[i] + 0.055) / 1.055), 2.4);
            }
        }

        return (0.2126 * colourCalcs[0]) + (0.7152 * colourCalcs[1]) + (0.0722 * colourCalcs[2]);
    }

    private Double luminanceContrast(Double colour1, Double colour2) {
        Double c1;
        Double c2;

        //Here we swap th colours round so that c1 is always the largest
        if (colour1 > colour2) {
            c1 = colour1;
            c2 = colour2;
        } else {
            c1 = colour2;
            c2 = colour1;
        }

        return (c1 + 0.05) / (c2 + 0.05);
    }

    private String outputColour(Double[] colour) {
        //This was used in debugging.
        return "["
                + (colour[0] * 255)
                + ", "
                + (colour[1] * 255)
                + ", "
                + (colour[2] * 255)
                + ", "
                + colour[3]
                + "]";
    }

    @Override
    public String getErrorMessage() {
        StringBuilder output =
                new StringBuilder(
                        ColourContrast.errors.size()
                                + " foreground and background colours do not have enough contrast \n");


        return output.toString();
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
        sheetProperties.setTitle("Colour Contrast");
        sheet.setProperties(sheetProperties);
        List<GridData> grid = new ArrayList<>();
        List<RowData> rowDataList = new ArrayList<>();

        CellData pageTitle =  Utils.generateCellData("Colour Contrast Checks", true, true);

        List<CellData> titleRow = new ArrayList<>();
        titleRow.add(pageTitle);

        CellData errorNumberTitle = Utils.generateCellData("ID", true);
        CellData xPathTitle =  Utils.generateCellData("XPath", true);
        CellData textTitle =  Utils.generateCellData("Text", true);
        CellData elementIDTitle =  Utils.generateCellData("Element ID", true);
        CellData lineNumber =  Utils.generateCellData("Estimated Line Number", true);
        CellData backgroundColourTitle =  Utils.generateCellData("Background Colour", true);
        CellData foregroundColourTitle =  Utils.generateCellData("Foreground Colour", true);
        CellData screenWidthTitle =  Utils.generateCellData("Screen Width", true);

        List<CellData> headingRow = new ArrayList<>();
        headingRow.add(errorNumberTitle);
        headingRow.add(xPathTitle);
        headingRow.add(textTitle);
        headingRow.add(elementIDTitle);
        headingRow.add(lineNumber);
        headingRow.add(backgroundColourTitle);
        headingRow.add(foregroundColourTitle);
        headingRow.add(screenWidthTitle);
        rowDataList.add((new RowData()).setValues(titleRow));
        rowDataList.add((new RowData()).setValues(headingRow));
        rowDataList.add(null);

        int i = 1;
        for (Map.Entry<Integer, List<Element>> elementEntry : errors.entrySet()) {
            for (Element element : elementEntry.getValue()) {
                //System.out.println(element.getXpath());
                CellData cellData = Utils.generateCellData(element.getXpath());
                CellData cellData1 = Utils.generateCellData(element.getText());
                CellData cellData2 = Utils.generateCellData(String.valueOf(i));
                CellData cellData3 = Utils.generateCellData(element.getAttr("id"));
                CellData cellData4 = Utils.generateCellData(element.getLineNumber().toString());
                CellData cellData5 = Utils.generateCellData("", new com.google.api.services.sheets.v4.model.Color().setRed(element.getActualBackgroundColour()[0].floatValue()).setGreen(element.getActualBackgroundColour()[1].floatValue()).setBlue(element.getActualBackgroundColour()[2].floatValue()));
                CellData cellData6 = Utils.generateCellData("", new com.google.api.services.sheets.v4.model.Color().setRed(element.getActualForegroundColour()[0].floatValue()).setGreen(element.getActualForegroundColour()[1].floatValue()).setBlue(element.getActualForegroundColour()[2].floatValue()));
                CellData cellData7 = Utils.generateCellData(elementEntry.getKey().toString());

                List<CellData> row = new ArrayList<>();
                row.add(cellData2);
                row.add(cellData);
                row.add(cellData1);
                row.add(cellData3);
                row.add(cellData4);
                row.add(cellData5);
                row.add(cellData6);
                row.add(cellData7);

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
