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

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ImageHasAltTag implements IAccessibilityIssue
{


    @Override
    public void captureScreenshotExample(int errorID, String url, WebDriver webDriver, String fullurl, String timeStamp) {
       System.out.println("attempt screen shot");
        try {
            int captureWidth = width;
            HashMap<Integer, LayoutFactory> lfs = new HashMap<>();

            BufferedImage img;
            img = RLGExtractor.getScreenshot(captureWidth, errorID, lfs, webDriver, url);

            Graphics2D g2d = img.createGraphics();
            for(Element e : ImageHasAltTag.errors) {
                g2d.setColor(Color.RED);
                g2d.setStroke(new BasicStroke(3));
                int coords[] = e.getBoundingCoords();
                g2d.drawRect(coords[0], coords[1], coords[2] - coords[0], coords[3] - coords[1]);
            }

            g2d.dispose();
            try {

                Drive driveService = CloudReporting.getDriveService();
                File output = Utils.getOutputFilePath(url, timeStamp, errorID, true);
                FileUtils.forceMkdir(output);
                ImageIO.write(img, "png", new File(output + "/ImageAltTagMissing" + captureWidth + ".png"));

                /*
                com.google.api.services.drive.model.File imageData = new com.google.api.services.drive.model.File();
                imageData.setName("ImageAltTagMissing.png");
                FileUtils.forceMkdir(output);
                java.io.File filePath = new java.io.File(output + "/ImageAltTagMissing" + captureWidth + ".png");
                FileContent mediaContent = new FileContent("image/png", filePath);
                com.google.api.services.drive.model.File imageFileUpload = driveService.files().create(imageData, mediaContent)
                        .setFields("id")
                        .execute();
                System.out.println("Content ID: " + imageFileUpload.getWebContentLink());
                System.out.println("View ID: " + imageFileUpload.getWebViewLink());
                */
            } catch (IOException e) {
//                e.printStackTrace();
            }
        } catch (NullPointerException npe) {
            npe.printStackTrace();
            System.out.println("Could not find one of the offending elements in screenshot.");
        }
    }

    @Override
    public void checkIssue(Element element, HashMap<String, Element> otherElements, int width) {
        System.out.println("***** img test");
        if (element.getTag().equalsIgnoreCase("img")) {
            if (!element.hasAttribute("alt")) {
                System.out.println("****** Warning *****");
                System.out.println("Alt Required for images");
                System.out.println(element.getXpath());
                ImageHasAltTag.errors.add(element);
                xpath = element.getXpath();
                coords = element.getBoundingCoords();
                this.width = width;
                x1 = element.getRectangle().x;
                x2 = element.getRectangle().x + element.getRectangle().width;
                y1 = element.getRectangle().y;
                y2 = element.getRectangle().y + element.getRectangle().height;
                didPass = false;
            } else {
                System.out.println("***** Found ****");
                System.out.println("Alt found");
                System.out.println(element.getAttr("alt"));
                didPass = true;
            }
        }
    }

    @Override
    public boolean getDidPass() {
        return didPass;
    }

    @Override
    public String getErrorMessage() {
        StringBuilder output = new StringBuilder(ImageHasAltTag.errors.size() + " images do not have alt tags \n");
        for ( Element element : ImageHasAltTag.errors) {
            output.append(element.getXpath()).append(" \n");
        }

        return output.toString();
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
        Sheet sheet = new Sheet();
        SheetProperties sheetProperties = new SheetProperties();
        sheetProperties.setTitle("Alt Tag Test");
        sheet.setProperties(sheetProperties);
        List<GridData> grid = new ArrayList<>();
        List<RowData> rowDataList = new ArrayList<>();

        CellData pageTitle = new CellData();
        pageTitle.setUserEnteredValue(new ExtendedValue().setStringValue("Images without alt tags"));

        List<CellData> titleRow = new ArrayList<>();
        titleRow.add(pageTitle);

        rowDataList.add((new RowData()).setValues(titleRow));
        rowDataList.add(null);

        int i = 1;
        for(Element element: ImageHasAltTag.errors) {
            System.out.println(element.getXpath());
            CellData cellData = new CellData();
            cellData.setUserEnteredValue(new ExtendedValue().setStringValue(element.getXpath()));

            CellData cellData2 = new CellData();
            cellData2.setUserEnteredValue(new ExtendedValue().setStringValue(String.valueOf(i)));

            List<CellData> row = new ArrayList<>();
            row.add(cellData2);
            row.add(cellData);

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


    public static List<Element> getErrors() {
        return errors;
    }

    private static List<Element> errors = new ArrayList<>();
    private String xpath;
    private Boolean didPass = true;
    private int numberOfTimesTested = 0;
    public int[] coords;
    private int x1,x2,y1,y2;
    private int width;
    private boolean cloudReportGenerated = false;
}
