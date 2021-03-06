package shef.main;

import com.google.api.services.drive.Drive;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import cz.vutbr.web.css.*;
import edu.gatech.xpert.dom.DomNode;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import shef.accessibility.AccessibilityAnalyser;
import shef.accessibility.IAccessibilityIssue;
import shef.analysis.RLGAnalyser;
import shef.handlers.CloudReporting;
import shef.layout.LayoutFactory;
import shef.mutation.CSSMutator;
import shef.reporting.inconsistencies.ResponsiveLayoutFailure;
import shef.rlg.ResponsiveLayoutGraph;
import shef.utils.BrowserFactory;
import shef.utils.StopwatchFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Created by thomaswalsh on 15/02/2016. */
public class RLGExtractor {
  static String scriptToExtract;
  final int[] SPOT_CHECK_WIDTHS = new int[] {480, 600, 640, 768, 1024, 1280};
  public ResponsiveLayoutGraph rlg;
  public WebDriver webDriver;
  public String browser;
  StopwatchFactory swf;
  HashMap<String, int[]> spotCheckWidths;
  int[] allWidths;
  private String current;
  private String fullUrl;
  private String shortUrl;
  private HashMap<Integer, DomNode> doms;
  private HashMap<Integer, LayoutFactory> lFactories;
  private String sampleTechnique;
  private boolean binarySearch, baselines;
  private int startW, endW, stepSize;
  private String preamble;
  private int sleep;
  private int[] sampleWidths;
  private int initialDoms;
  private ArrayList<Integer> breakpoints;
  private String ts;
  private String jsCommands;

  public RLGExtractor(
      String current,
      String jsCommands,
      String fullUrl,
      String shortUrl,
      HashMap<Integer, DomNode> doms,
      String b,
      String st,
      boolean bs,
      int start,
      int end,
      int ss,
      String preamble,
      int sleep,
      String timeStamp,
      boolean baselines)
      throws IOException {
    this.current = current;
    this.fullUrl = fullUrl;
    this.shortUrl = shortUrl;
    this.doms = doms;
    this.jsCommands = jsCommands;
    this.lFactories = new HashMap<>();
    this.browser = b;
    this.sampleTechnique = st;
    this.binarySearch = bs;
    this.startW = start;
    this.endW = end;
    this.stepSize = ss;
    this.preamble = preamble;
    swf = new StopwatchFactory();
    this.sleep = sleep;
    breakpoints = new ArrayList<>();
    ts = timeStamp;
    this.baselines = baselines;

    // BASELINE SCREENSHOT CAPTURE
    if (baselines) {
      spotCheckWidths = new HashMap<>();
      spotCheckWidths.put("kersley", new int[] {320, 480, 768, 1024});
      spotCheckWidths.put("responsinator", new int[] {320, 375, 384, 414, 768, 1024});
      spotCheckWidths.put("semalt", new int[] {320, 384, 600, 768, 1024});
      spotCheckWidths.put("wasserman", new int[] {320, 375, 414, 600, 768, 1280});
      runBaselines();
    }
  }

  public static BufferedImage getScreenshot(
      int captureWidth,
      int errorID,
      HashMap<Integer, LayoutFactory> lfs,
      WebDriver d,
      String fullUrl) {
    Tool.capturePageModel(
        fullUrl,
        new int[] {captureWidth},
        Tool.sleep,
        false,
        false,
        d,
        new StopwatchFactory(),
        lfs,
        new HashMap<>());
    return Utils.getScreenshot(fullUrl, captureWidth, Tool.sleep, d, errorID);
  }

  /**
   * This method determines the initial viewport widths at which to sample the page's layout
   *
   * @param technique the sample technique being used
   * @param shortUrl the url of the webpage
   * @param drive the selenium driver powering the browser
   * @param startWidth the minimum width
   * @param finalWidth the maxiumum width
   * @param stepSize the step size used for interval sampling
   * @param preamble the directory in which to precede the URL.
   * @param breakpoints the list of breakpoints
   * @return
   */
  public static int[] calculateSampleWidths(
      String technique,
      String shortUrl,
      WebDriver drive,
      int startWidth,
      int finalWidth,
      int stepSize,
      String preamble,
      ArrayList<Integer> breakpoints) {
    int[] widths = null;
    ArrayList<Integer> widthsAL = new ArrayList<Integer>();
    if (technique.equals("uniformBP")) {
      TreeSet<Integer> widthsTS = new TreeSet<Integer>();
      int currentWidth = startWidth;

      widthsTS.add(startWidth);

      while (currentWidth + stepSize <= finalWidth) {
        currentWidth = currentWidth + stepSize;
        widthsTS.add(currentWidth);
      }
      widthsTS.add(finalWidth);

      ArrayList<String> cssFiles = initialiseFiles(shortUrl, drive);
      ArrayList<RuleMedia> mqSet = getMediaQueries(shortUrl, cssFiles, preamble);
      int[] widthsBP = getBreakpoints(mqSet);
      for (int w : widthsBP) {
        if ((w >= startWidth) && (w <= finalWidth)) {
          widthsTS.add(w);
          breakpoints.add(w);
        }
      }
      widths = new int[widthsTS.size()];
      Iterator iter = widthsTS.iterator();
      int counter = 0;
      while (iter.hasNext()) {
        try {
          int i = (int) iter.next();
          widths[counter] = i;
          counter++;
        } catch (Exception e) {

        }
      }
    } else if (technique.equals("uniform")) {
      int currentWidth = startWidth;

      widthsAL.add(startWidth);

      while (currentWidth + stepSize <= finalWidth) {
        currentWidth = currentWidth + stepSize;
        widthsAL.add(currentWidth);
      }

      // Adds the upper bound to the sample width set, if it's not already there
      if (!Integer.toString(widthsAL.get(widthsAL.size() - 1))
          .equals(Integer.toString(finalWidth))) {
        widthsAL.add(finalWidth);
      }

      // Copy the contents of the arraylist into an array
      widths = new int[widthsAL.size()];

      int counter = 0;
      for (Integer i : widthsAL) {
        widths[counter] = i;
        counter++;
      }
    } else if (technique.equals("random")) {

    } else if (technique.equals("breakpoint")) {
      ArrayList<String> cssFiles = initialiseFiles(shortUrl, drive);
      ArrayList<RuleMedia> mqSet = getMediaQueries(shortUrl, cssFiles, preamble);
      widths = getBreakpoints(mqSet);

      widthsAL.add(startWidth);
      for (int i : widths) {
        if ((i >= startWidth) && (i <= finalWidth)) {
          widthsAL.add(i);
        }
      }
      widthsAL.add(finalWidth);

      widths = new int[widthsAL.size()];

      int counter = 0;
      for (Integer i : widthsAL) {
        widths[counter] = i;
        counter++;
      }
    }

    // Return the array of widths
    if (widths != null) {
      return widths;
    } else {
      return new int[] {};
    }
  }

  public static int[] getBreakpoints(ArrayList<RuleMedia> mqueries) {
    Pattern p = Pattern.compile("\\(([A-Za-z]*-width:[ ]+)([0-9]*[.]+[0-9]*)(em|px)\\)");
    TreeSet<Integer> bps = new TreeSet<Integer>();
    for (RuleMedia rm : mqueries) {
      List<MediaQuery> mqs = rm.getMediaQueries();
      for (MediaQuery mq : mqs) {
        for (MediaExpression me : mq.asList()) {
          try {
            if (me.toString().contains("width")) {
              String s = me.toString().trim();
              Matcher m = p.matcher(s);
              m.matches();
              double bp = Double.valueOf(m.group(2));
              int bpFinal = 0;
              if (m.group(3).equals("em")) {
                bpFinal = (int) (bp * 16);
              } else if (m.group(3).equals("px")) {
                bpFinal = (int) bp;
              }
              if (bpFinal != 0) {
                bps.add(bpFinal);
                if (me.toString().contains("min")) {
                  bps.add(bpFinal - 1);
                } else if (me.toString().contains("max")) {
                  bps.add(bpFinal + 1);
                }
              }
            }
          } catch (Exception e) {
          }
        }
      }
    }

    if (bps.size() > 0) {
      int[] extras = new int[bps.size()];
      Iterator iter = bps.iterator();
      int counter = 0;
      while (iter.hasNext()) {
        try {
          int i = (int) iter.next();
          extras[counter] = i;
          counter++;
        } catch (Exception e) {

        }
      }

      return extras;
    }
    return new int[0];
  }

  @SuppressWarnings("unchecked")
  public static ArrayList<String> initialiseFiles(String url, WebDriver driver) {
    //		JavascriptExecutor js = (JavascriptExecutor) driver;
    String script;
    try {
      script =
          Utils.readFile(new java.io.File(".").getCanonicalPath() + "/../resources/getCssFiles.js");
      return (ArrayList<String>) ((JavascriptExecutor) driver).executeScript(script);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return new ArrayList<String>();
  }

  @SuppressWarnings({"unused", "rawtypes"})
  public static ArrayList<RuleMedia> getMediaQueries(
      String base, ArrayList<String> cssFiles, String preamble) {
    ArrayList<RuleMedia> mqCandidates = new ArrayList<RuleMedia>();
    URL cssUrl = null;
    URLConnection conn;
    if (cssFiles != null) {
      String[] cssContent = new String[cssFiles.size()];
      int counter = 0;

      for (String cssFile : cssFiles) {
        //            System.out.println(cssFile);
        String contents = "";
        try {
          if (cssFile.contains("http")) {
            cssUrl = new URL(cssFile);
          } else if (cssFile.substring(0, 2).equals("//") || cssFile.substring(0, 1).equals("/")) {
            cssUrl = new URL(base + cssFile);
          } else {
            //                    System.out.println("LOCAL");
            cssUrl =
                new URL(
                    ("file://"
                        + preamble
                        + base.replace("/index.html", "")
                        + "/"
                        + cssFile.replace("./", "")));
            //                    cssUrl = new URL(("file://" + preamble + base.split("/")[0] + "/"
            // + base.split("/")[1] + "/" + cssFile.replace("./", "")));
          }
          //                    System.out.println(cssUrl);

          conn = cssUrl.openConnection();

          BufferedReader input = new BufferedReader(new InputStreamReader(conn.getInputStream()));

          //                long start = System.nanoTime();
          String inputLine;
          while ((inputLine = input.readLine()) != null) {
            contents += inputLine;
          }
          //                long end = System.nanoTime();
          //                double duration = ((end - start) / 1000000000.0);
          //                System.out.println(duration);
          contents += "\n\n";
          cssContent[counter] = contents;

        } catch (Exception e) {
          //                e.printStackTrace();
          //                System.out.println("Problem loading or layout the CSS file " +
          // cssUrl.toString());
        }
        counter++;
      }

      StyleSheet ss = null;
      for (int i = 0; i < cssContent.length; i++) {
        String s = cssContent[i];
        //            System.out.println(s);
        try {
          String prettified = s;
          //System.out.println(prettified);
          //                        CSSMutator.prettifyCss(s);
          StyleSheet temp = CSSFactory.parse(prettified);
          //                System.out.println(temp);
          for (RuleBlock rb : temp.asList()) {
            if (rb instanceof RuleMedia) {
              RuleMedia rm = (RuleMedia) rb;
              if (CSSMutator.hasNumericQuery(rm)) {

                if (rm.asList().size() > 0) {
                  mqCandidates.add(rm);
                }
              }
            }
          }
        } catch (IOException e) {
                              e.printStackTrace();
        } catch (CSSException e) {
                              e.printStackTrace();
        } catch (NullPointerException e) {
                             System.out.println("Null pointer for some reason on " + i);
        }
      }
    }
    return mqCandidates;
  }

  /** Manages the whole RLG extraction process from start to finish */
  public void extract() {
    try {
      // Start the timer
      this.swf.getRlg().start();

      if (webDriver == null) {
        webDriver = BrowserFactory.getNewDriver(browser);
      }
      JavascriptExecutor js = (JavascriptExecutor) webDriver;
      // Load up the webpage in the browser, using a pop-up to make sure we can resize down to 320
      // pixels wide
      String winHandleBefore = webDriver.getWindowHandle();
      webDriver.get(fullUrl);
      String externalJS = "";
      if (jsCommands != null && !jsCommands.equals("")) {
        externalJS =
            StringEscapeUtils.escapeJava(new String(Files.readAllBytes(Paths.get(jsCommands))));
        System.out.println(externalJS.split("\r\n|\r|\n").length - 2);
        // webDriver.get(fullUrl);
        // ((JavascriptExecutor) webDriver).executeScript(externalJS);
      }

      ((JavascriptExecutor) webDriver)
          .executeScript(
              "var newwindow=window.open(\""
                  + fullUrl
                  + "\",'test','width=320,height=1024,top=50,left=50', scrollbars='no', menubar='no', resizable='no', toolbar='no', top='+top+', left='+left+', 'false');\n"
                  + "newwindow.focus();"
                  + "newwindow.onload = function() {"
                  + "var script = document.createElement('script');"
                  + "try {"
                  + "script.appendChild(document.createTextNode('"
                  + externalJS
                  + "'));"
                  + "} catch (e) {"
                  + "script.text = '"
                  + externalJS
                  + "';"
                  + ""
                  + "}"
                  + "this.document.head.appendChild(script);"
                  + "}; ");

      // System.out.println(webDriver.getPageSource());

      for (String winHandle : webDriver.getWindowHandles()) {
        System.out.println(winHandle);
        webDriver.switchTo().window(winHandle);

        if (winHandle.equals(winHandleBefore)) {
          webDriver.close();
        }
      }
      webDriver.manage().window().setPosition(new Point(0, 0));

      // Calculate the initial sample widths
      sampleWidths =
          calculateSampleWidths(
              sampleTechnique, shortUrl, webDriver, startW, endW, stepSize, preamble, breakpoints);
      initialDoms = sampleWidths.length;

      // Capture the layout of the page at each width
      Tool.capturePageModel(
          fullUrl, sampleWidths, sleep, false, false, webDriver, swf, lFactories, new HashMap<>());
      ArrayList<LayoutFactory> oracleLFs = new ArrayList<>();

      // For each sampled width, analyse the DOM to construct the specific layout structure
      for (int width : sampleWidths) {
        LayoutFactory lf = lFactories.get(width);
        oracleLFs.add(lf);
      }

      // Use the initial layouts to build the full RLG
      this.rlg =
          new ResponsiveLayoutGraph(
              oracleLFs, sampleWidths, fullUrl, lFactories, binarySearch, webDriver, swf, sleep);
      this.swf.getRlg().stop();
      this.swf.getDetect().start();

      // Use the extracted RLG to find any layout inconsistencies the developer/tester should know
      // about
      RLGAnalyser analyser =
          new RLGAnalyser(this.getRlg(), webDriver, fullUrl, breakpoints, lFactories, startW, endW);
      AccessibilityAnalyser accessibilityAnalyser =
          new AccessibilityAnalyser(
              this.getRlg(), webDriver, fullUrl, breakpoints, lFactories, startW, endW);
      ArrayList<ResponsiveLayoutFailure> errors = analyser.analyse();
      List<IAccessibilityIssue> accessibilityIssues = accessibilityAnalyser.analyse();
      this.swf.getDetect().stop();

      this.swf.getReport().start();

      //          For each detected RLF, capture a screenshot for the report
      if (errors.size() > 0) {
        for (ResponsiveLayoutFailure error : errors) {
          error.captureScreenshotExample(
              errors.indexOf(error) + 1, shortUrl, webDriver, fullUrl, ts);
        }
      }

      //          For each detected accessibility issue, capture a screenshot for the report
      analyser.writeReport(shortUrl, errors, ts);
      accessibilityAnalyser.writeReport(shortUrl, accessibilityIssues, ts);

      List<Sheet> sheetReports = new ArrayList<>();

      //Create Title Page
      if (accessibilityIssues.size() > 0) {
        Sheet sheet = new Sheet();
        SheetProperties sheetProperties = new SheetProperties();
        sheetProperties.setTitle("Accessibility Overview");
        sheet.setProperties(sheetProperties);
        List<GridData> grid = new ArrayList<>();
        List<RowData> rowDataList = new ArrayList<>();

        CellData rowTitle = Utils.generateCellData("Last updated", true);

        CellData rowValue = Utils.generateCellData(ts);

        List<CellData> titleRow = new ArrayList<>();
        titleRow.add(rowTitle);
        titleRow.add(rowValue);
        rowDataList.add((new RowData()).setValues(titleRow));

        for (IAccessibilityIssue issue : accessibilityIssues) {
          rowDataList.addAll(issue.getOverviewRow());
        }
        grid.add((new GridData()).setRowData(rowDataList));
        sheet.setData(grid);
        sheetReports.add(sheet);
      }

      if (accessibilityIssues.size() > 0) {
        for (IAccessibilityIssue issue : accessibilityIssues) {
          issue.captureScreenshotExample(
              errors.indexOf(issue) + 1, shortUrl, webDriver, fullUrl, ts);
          if (!issue.cloudReportMade()) {
            sheetReports.add(issue.generateCloudReport());
          }
        }
      }

      Sheets sheetsService = CloudReporting.getSheetsService();
      Drive driveService = CloudReporting.getDriveService();
      Spreadsheet sheet = new Spreadsheet();
      SpreadsheetProperties properties = new SpreadsheetProperties();
      properties.setTitle(shortUrl);
      sheet.setProperties(properties);
      sheet.setSheets(sheetReports);

      //Remove previous sheets
        String fileQueryParam ="name = '"+shortUrl+"' ";
        com.google.api.services.drive.Drive.Files.List qryFile =    driveService.files().list().setFields("files(id, name)").setQ(fileQueryParam).setSpaces("drive");

        com.google.api.services.drive.model.FileList gLstFile = qryFile.execute();
        for (com.google.api.services.drive.model.File gFlFile : gLstFile.getFiles())
        {
            (driveService.files().delete(gFlFile.getId())).execute();
        }



      Spreadsheet response = sheetsService.spreadsheets().create(sheet).execute();

      String queryParam ="name = 'ReDeCheck Accessibility' ";
      com.google.api.services.drive.Drive.Files.List qry =    driveService.files().list().setFields("files(id, name)").setQ(queryParam).setSpaces("drive");

      com.google.api.services.drive.model.FileList gLst = qry.execute();
      String id = "";
      for (com.google.api.services.drive.model.File gFl : gLst.getFiles())
      {
        id = gFl.getId();
        System.out.println("ID==>"+id);
      }



        if (id.equals("")) {
        com.google.api.services.drive.model.File fileMetadata =
                new com.google.api.services.drive.model.File();
        fileMetadata.setName("ReDeCheck Accessibility");
        fileMetadata.setMimeType("application/vnd.google-apps.folder");

        com.google.api.services.drive.model.File folderStore =
                driveService.files().create(fileMetadata).setFields("id").execute();
        id = folderStore.getId();

      }


      com.google.api.services.drive.model.File file =
          driveService.files().get(response.getSpreadsheetId()).setFields("parents").execute();
      StringBuilder previousParents = new StringBuilder();
      for (String parent : file.getParents()) {
        previousParents.append(parent);
        previousParents.append(',');
      }

      file =
          driveService
              .files()
              .update(response.getSpreadsheetId(), null)
              .setAddParents(id)
              .setRemoveParents(previousParents.toString())
              .setFields("id, parents")
              .execute();

      // Image upload

      // Write the text report to disk

      // Stop the timer for the report generation
      this.swf.getReport().stop();

    } catch (Exception e) {
      e.printStackTrace();
    }

    // Make sure the WebDriver is closed down
    if (webDriver != null) {
      webDriver.close();
      webDriver.quit();
    }
  }

  /** @throws IOException */
  private void runBaselines() throws IOException {
    webDriver.manage().window().setSize(new org.openqa.selenium.Dimension(1400, 1000));
    webDriver.get(fullUrl);
    File spotcheckDir, exhaustiveDir;
    if (!shortUrl.contains("www")) {
      String[] splits = shortUrl.split("/");
      String webpage = splits[0];
      String mutant = "index";
      spotcheckDir = new File(Tool.redecheck + "/screenshots/" + webpage + "/spotcheck/");
      exhaustiveDir = new File(Tool.redecheck + "/screenshots/" + webpage + "/exhaustive/");
    } else {
      String[] splits = shortUrl.split("www.");
      spotcheckDir = new File(Tool.redecheck + "/screenshots/" + splits[1] + "/spotcheck/");
      exhaustiveDir = new File(Tool.redecheck + "/screenshots/" + splits[1] + "/exhaustive/");
    }
    for (String scTechnique : spotCheckWidths.keySet()) {
      File scTechFile = new File(spotcheckDir + "/" + scTechnique + "/");
      try {
        FileUtils.forceMkdir(scTechFile);
      } catch (IOException e) {
        e.printStackTrace();
      }
      int[] scws = spotCheckWidths.get(scTechnique);
      for (int scw : scws) {
        BufferedImage ss = Utils.getScreenshot(shortUrl, scw, sleep * 2, webDriver, scw);
        BufferedImage dest = ss.getSubimage(0, 0, scw, ss.getHeight());
        Graphics2D g2d = ss.createGraphics();
        g2d.setColor(Color.RED);
        g2d.drawRect(0, 0, scw, ss.getHeight());
        File outputfile = new File(scTechFile + "/" + scw + ".png");
        ImageIO.write(dest, "png", outputfile);
      }
    }
  }

  public ResponsiveLayoutGraph getRlg() {
    return this.rlg;
  }

  public StopwatchFactory getSwf() {
    return swf;
  }

  public int getInitialDoms() {
    return initialDoms;
  }

  public int[] getSampleWidths() {
    return sampleWidths;
  }
}
