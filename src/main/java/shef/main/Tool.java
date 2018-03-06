package shef.main;


import com.beust.jcommander.JCommander;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.SheetProperties;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.SpreadsheetProperties;
import edu.gatech.xpert.dom.DomNode;
import edu.gatech.xpert.dom.JsonDomParser;
import edu2.gatech.xpert.dom.layout.AGDiff;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
//import org.openqa.selenium.phantomjs.PhantomJSDriver;
//import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import shef.analysis.RLGComparator;
import shef.handlers.CloudReporting;
import shef.layout.LayoutFactory;
import shef.mutation.PositionalXMLReader;
import shef.mutation.ResultClassifier;
import shef.rlg.ResponsiveLayoutGraph;
import shef.utils.ResultProcessor;
import shef.utils.StopwatchFactory;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.awt.*;
import java.io.*;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Tool {
    // Instance variables
    public static String oracle;
    public static String test;
    public String url;
    public String instructionUrl;
    String[] clArgs;
    public String current;
    public static String preamble;
    private int startWidth;
    private int finalWidth;
    private static int browserHeight;
    private int stepSize;
    private String sampleTechnique = "uniformBP";
    private boolean binarySearch = true;
    private boolean timing;
    private int timingID;
    private String browser;
    private boolean fix;
    private String mutantID;
    private boolean screenshot;
    private boolean baselines, results;
    private boolean tool;
    public boolean xpert;
    private int[] widthsToCheck;
    private int[] allWidths;
    private TreeSet<Integer> allTS;
    public boolean saveToExtras;
    static HashMap<Integer, DomNode> oracleDoms;
    static HashMap<Integer, String> oracleDomStrings;
	static HashMap<Integer, DomNode> testDoms;
    static HashMap<Integer, String> testDomStrings;
    static HashMap<Integer, LayoutFactory> oFactories;
    static HashMap<Integer, LayoutFactory> tFactories;
    HashMap<Integer, LayoutFactory> layoutFactories;
//    private static PhantomJSDriver driver;
    public static JavascriptExecutor js;
    static String scriptToExtract;
    static String redecheck = "/Users/thomaswalsh/Documents/PhD/Code-Projects/Redecheck/";
    private final String REPORT_DIRECTORY = "/Users/thomaswalsh/Documents/PhD/Code-Projects/Redecheck/reports/";
    private static String timesDirectory = "/Users/thomaswalsh/Documents/PhD/Code-Projects/Redecheck/times/";
    private static String dataDirectory = "/Users/thomaswalsh/Documents/PhD/Papers/main-journal-paper-data/";
    static int[] manualWidths = {480, 600, 640, 768, 1024, 1280};
    static int sleep = 50;
    private CommandLineParser clp = new CommandLineParser();

    public Tool(String[] args) throws IOException, InterruptedException {

        current = new java.io.File( "." ).getCanonicalPath();
        System.setProperty("phantomjs.binary.path", current + "/../resources/phantomjs");
        System.setProperty("webdriver.chrome.driver", current + "/../resources/chromedriver");
        System.setProperty("webdriver.opera.driver", current + "/../resources/operadriver");
        System.setProperty("webdriver.gecko.driver", current + "/../resources/geckodriver");
        
        clArgs = args;
        new JCommander(clp, clArgs);
        oracle = clp.oracle;
        test = clp.test;
        preamble = clp.preamble;
        
        startWidth = clp.startWidth;
        finalWidth = clp.endWidth;

        java.awt.Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double width = screenSize.getWidth();
        double height = screenSize.getHeight();
        if (finalWidth > width) {
            finalWidth = (int) width;
        }
        browserHeight = 600;
        // browserHeight = (int) (height - 100);

        if (clp.ss != -1) {
        	stepSize = clp.ss;
        }
        sampleTechnique = clp.sampling;
        binarySearch = clp.binary;
        mutantID = clp.mutantID;
        screenshot = clp.screenshot;
        baselines = clp.baselines;
        results = clp.results;
        tool = clp.tool;
        xpert = clp.xpert;
        if (clp.browser != null) {
            browser = clp.browser;
        }
        fix = clp.fix;
        timing = clp.timing;
        timingID = clp.timingID;
        url = clp.url;
        instructionUrl = clp.instructionFile;
        widthsToCheck = new int[]{};

        oracleDoms = new HashMap<Integer, DomNode>();
        testDoms = new HashMap<Integer, DomNode>();
        oFactories = new HashMap<>();
        tFactories = new HashMap<>();
        allWidths = new int[(finalWidth - startWidth) + 1];
        allTS = new TreeSet<>();
        for (int i = 0; i < allWidths.length; i++) {
            allWidths[i] = i + startWidth;
            allTS.add(i+startWidth);
        }

        // Setup for new version of tool
        layoutFactories = new HashMap<>();
        if (!results && !fix) {
            runFaultDetector(current, url, instructionUrl, browser, sampleTechnique, binarySearch, startWidth, finalWidth, stepSize, baselines);
        }

        if (fix) {
            runFixer();
        }

        if (results) {
            ResultProcessor rp = new ResultProcessor();
//            rp.getInconsistencyResults();
            rp.writeArchiveWebpages();
        }
    }

    private void runFixer() {
        try {
            scriptToExtract = Utils.readFile(current +"/../resources/webdiff2.js");
            String fullUrl;
            if (preamble != null) {
                fullUrl = "file://" + preamble + url;
            } else {
                fullUrl = url;
            }
            System.out.println(fullUrl);

            FaultPatcher patcher = new FaultPatcher(fullUrl, url, browser, current);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void runFaultDetector(String current, String url, String instructionPath, String browser, String sampleTechnique, boolean binarySearch, int startWidth, int finalWidth, int stepSize, boolean baselines) {
        try {
            Date date = new Date();
            Format formatter = new SimpleDateFormat("YYYY-MM-dd_hh-mm-ss");
            String timeStamp = formatter.format(date);
            scriptToExtract = Utils.readFile(current +"/../resources/webdiff2.js");
            String fullUrl;
            if (preamble != null) {
                fullUrl = "file://" + preamble + url;
            } else {
                fullUrl = url;
            }
            System.out.println(fullUrl);
            //System.out.println("I've modified the code");
            RLGExtractor extractor = new RLGExtractor(current, instructionPath, fullUrl, url, oracleDoms, browser, sampleTechnique, binarySearch, startWidth, finalWidth, stepSize, preamble, sleep, timeStamp, baselines);
//            Thread t = new Thread(thread);
//            t.start();
//            while(t.isAlive()){}
            extractor.extract();

//            ResponsiveLayoutGraph rlg = thread.getRlg();
//            int numNodes = rlg.getNodes().size();
//            int numVCs = rlg.getVisCons().size();
//            int numACs = rlg.getAlignmentConstraints().size();
//            writeRlgStats(url, timeStamp, numNodes, numVCs, numACs);
//            writeTimes(url, thread.getSwf(), timeStamp);
//            if (timing) {
//                writeTimesSpecial(thread.swf, url, timingID);
//            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }






    /**
     * Samples each webpage and constructs an RLG for each, then compares the two, writing the model differences to a file.
     * @throws InterruptedException
     */
    public void runTool() throws InterruptedException, IOException {
        scriptToExtract = Utils.readFile(current +"/../resources/webdiff2.js");
        Date date = new Date();
        Format formatter = new SimpleDateFormat("YYYY-MM-dd_hh-mm-ss");
        String timeStamp = formatter.format(date);
        String oracleUrl = preamble + oracle + ".html";
        String testUrl = preamble + test + ".html";
        RLGExtractor rlg1 = new RLGExtractor(current, "", oracleUrl, test, oracleDoms, browser, sampleTechnique, binarySearch, startWidth, finalWidth, stepSize, preamble, sleep, timeStamp, baselines);
        RLGExtractor rlg2 = new RLGExtractor(current, "", testUrl, test, testDoms, browser, sampleTechnique, binarySearch, startWidth, finalWidth, stepSize, preamble, sleep, timeStamp, baselines);
//        Thread t1 = new Thread(rlg1);
//        Thread t2 = new Thread(rlg2);

//        t1.start();
//        t2.start();
//        while (t1.isAlive() || t2.isAlive()) {
            // Let the RLGs get gathered.
//        }
        rlg1.extract();
        rlg2.extract();

        System.out.println(oFactories.size()== tFactories.size());
        for (Integer i : tFactories.keySet()) {
            System.out.println(i);
        }


        ResponsiveLayoutGraph r = rlg2.getRlg();
        ResponsiveLayoutGraph r2 = rlg2.getRlg();
        RLGComparator comp = new RLGComparator(r, r2, widthsToCheck);
        comp.compare();
        comp.compareMatchedNodes();
        comp.printDiff();
        comp.writeRLGDiffToFile(testUrl, "report-" + sampleTechnique + "-" + stepSize + "-" + binarySearch, true);
        copyMutantInfo();

        if (mutantID != null) {
            comp.writeRLGDiffToFile(redecheck + "screenshots/" + mutantID, "/main-report", false);
        }

//        for (int w : rlg1.getSampleWidths()) {
//            DomNode or = rlg1.doms.get(w);
//            DomNode mod = rlg2.doms.get(w);
//            System.out.println(w + " " + ResultClassifier.domsEqual(or, mod));
//        }
        System.out.println(testDoms.size());
//        writeToFile(testUrl, String.valueOf(duration), "time-"+sampleTechnique+"-"+binarySearch );
//        writeToFile(testUrl, String.valueOf(testDoms.size()), "doms-"+sampleTechnique+"-" + stepSize + "-" + binarySearch, dataDirectory);
//        writeToFile(testUrl, String.valueOf(rlg2.getInitialDoms()), "doms-initial-"+sampleTechnique+"-" + stepSize + "-" + binarySearch, dataDirectory);
        processTimes(rlg2.getSwf(), testUrl);
    }

    private void processTimes(StopwatchFactory swf, String testUrl) {
        String results = "";
        DecimalFormat df = new DecimalFormat("#.##");
        results+= getTimeStringFromStopwatch(swf.getSetup()) + "\n";
        results+= getTimeStringFromStopwatch(swf.getCapture()) + "\n";
        results+= getTimeStringFromStopwatch(swf.getExtract()) + "\n";
        results+= getTimeStringFromStopwatch(swf.getSleep()) + "\n";
        results+= getTimeStringFromStopwatch(swf.getProcess()) + "\n";
        results+= getTimeStringFromStopwatch(swf.getTotal()) + "\n";


        writeToFile(testUrl, results, "timings-" + sampleTechnique + "-" + stepSize+ "-" + binarySearch, dataDirectory);
    }


    public static String getTimeStringFromStopwatch(StopWatch sw) {
//        System.out.println(sw.getTime());
//        String[] splits = sw.toString().split(":");
        double time = (sw.getTime()) / 1000.0;
        String timeS =  String.valueOf(time);
//        System.out.println(timeS);
        return timeS;
    }

    private static void copyMutantInfo() {
        try {
            String[] splits = test.split("/");
            String webpage = splits[0];
            String mutant = splits[1];
            File original = new File(redecheck+"testing/"+webpage+"/"+mutant +'/'+mutant+".txt");
//            System.out.println(original.toString());
            File copied = new File(dataDirectory +webpage+"/"+mutant +'/'+mutant+".txt");
            FileUtils.copyFile(original, copied, false);
        } catch (Exception e ) {
            e.printStackTrace();
        }
    }

    /**
     * This method samples the DOM of a webpage at a set of viewports, and saves the DOMs into a HashMap
     * @param url        The url of the webpage
     * @param widths    The viewport widths to sample
     * @param domStrings
     */
    public static void capturePageModel(String url, int[] widths, int sleep, boolean takeScreenshot, boolean saveDom, WebDriver wdriver, StopwatchFactory swf, HashMap<Integer, LayoutFactory> lFactories, HashMap<Integer, String> domStrings) {
        // Create a parser for the DOM strings
        JsonDomParser parser = new JsonDomParser();
        File domFile=null;
        try {
            // Set up storage directory
            String outFolder = "";

            if (saveDom) {
                String[] splits = url.split("/");
                outFolder = redecheck + "output/" + splits[7] + "/" + splits[8];
                File dir = new File(outFolder);
                FileUtils.forceMkdir(dir);
            }

            // Iterate through all viewport widths
            for (int w : widths) {
                // Check if DOM already saved for speed
                domFile = new File(outFolder + "/" + w + ".js");
                boolean consecutiveMatches = false;

                // Resize the browser window
                wdriver.manage().window().setSize(new Dimension(w, browserHeight));
                String previous = "";


                while (!consecutiveMatches) {
                    // Extract the DOM and save it to the HashMap.
//                    wdriver.manage().timeouts().implicitlyWait(sleep, TimeUnit.MILLISECONDS);
                    //System.out.println(scriptToExtract);
                    String extractedDom = extractDOM(wdriver, scriptToExtract);
                    String src = wdriver.getPageSource();


                    if (previous.equals(extractedDom)) {

                        lFactories.put(w, new LayoutFactory(extractedDom, src));
                        domStrings.put(w, extractedDom);
                        if (saveDom) {
                            FileUtils.writeStringToFile(domFile, extractedDom);
                        }
                        consecutiveMatches = true;
                    } else {

                        previous = extractedDom;
                    }


                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String extractDOM(WebDriver cdriver, String script) throws IOException {
        return (String) ((JavascriptExecutor) cdriver).executeScript(script);
    }


    /**
     * Loads the DOM of a given webpage at a specified set of resolutions into a Map for easy access
     * @param widths        the widths at which to load the DOM
     * @param url           the URL of the webpage
     * @return
     */
    public Map<Integer, DomNode> loadDoms(int[] widths, String url) {
        Map<Integer, DomNode> doms = new HashMap<Integer, DomNode>();
        JsonDomParser parser = new JsonDomParser();
        for (int width : widths) {
            String file = current + "/../output/" + url.replaceAll("/", "").replace(".html", "") + "/" + "width" + width + "/dom.js";
            try {
                String domStr = FileUtils.readFileToString(new File(file));
                doms.put(width, parser.parseJsonDom(domStr));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return doms;
    }



//    public static PhantomJSDriver getNewDriver(DesiredCapabilities dCaps) {
//        return new PhantomJSDriver(dCaps);
//    }

    public static void writeToFile(String testUrl, String content, String fileName, String directory) {
        PrintWriter output = null;
        String outFolder = "";
        try {
            String[] splits = testUrl.split("/");
            String webpage = splits[8];
//            String mutant = splits[10];
            outFolder = directory + webpage + "/";

            File dir = new File(outFolder+fileName+".txt");
            System.out.println(dir.toString());
            output = new PrintWriter(dir);
            output.append(content);
            output.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Main method to handle execution of the whole tool
     * @param args
     * @throws InterruptedException
     * @throws IOException
     */
    public static void main(String[] args) throws InterruptedException, IOException {
        Tool redecheck = new Tool(args);
    }
}
