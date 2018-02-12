package shef.accessibility;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.WebDriver;
import shef.layout.Element;
import shef.layout.LayoutFactory;
import shef.reporting.inconsistencies.ResponsiveLayoutFailure;
import shef.rlg.Node;
import shef.rlg.ResponsiveLayoutGraph;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by Matthew Walker on 7/02/2018.
 * Based on the RLGAnalyser
 */
public class AccessibilityAnalyser {
    ResponsiveLayoutGraph rlg;
    ArrayList<ResponsiveLayoutFailure> errors;
    WebDriver driver;
    String url;
    ArrayList<Integer> bpoints;
    ArrayList<Node> onePixelOverflows;
    HashMap<Integer, LayoutFactory> layouts;
    int vmin, vmax;


    public AccessibilityAnalyser(ResponsiveLayoutGraph r, WebDriver webDriver, String fullUrl, ArrayList<Integer> breakpoints, HashMap<Integer, LayoutFactory> lFactories, int vmin, int vmax) {
        rlg = r;
        driver = webDriver;
        url = fullUrl;
        bpoints = breakpoints;
//        System.out.println(bpoints);
        onePixelOverflows = new ArrayList<>();
        layouts = lFactories;
        this.vmin = vmin;
        this.vmax = vmax;
        errors = new ArrayList<>();
    }

    public List<IAccessibilityIssue> analyse() {
        List<IAccessibilityIssue> issues = getAccessibilityIssues();

        //Loop through all layouts generated
        for(Map.Entry<Integer, LayoutFactory> entry : layouts.entrySet()) {
            Integer key = entry.getKey();
            LayoutFactory factory = entry.getValue();
            HashMap<String, Element> elements = factory.layout.getElements();
            for (int i = 0; i < issues.size(); i++) {
            //For the layout loop through all elements and test
                //Only run tests if it is necessary.
                if (issues.get(i).isAffectedByLayouts() || issues.get(i).numberOfTimesTested() == 0) {
                    for (Map.Entry<String, Element> elementEntry : elements.entrySet()) {
                        String elementKey = elementEntry.getKey();
                        Element element = elementEntry.getValue();

                        issues.get(i).checkIssue(element);
                        issues.get(i).incNumberOfTimesTested();
                    }
                }
            }
            // do what you have to do here
            // In your case, another loop.
        }

        return issues;
    }

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
                    outputFile = new File(new File(".").getCanonicalPath() + "/../reports/" + webpage + "/" + mutant + "/");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (url.contains("http://")) {
                String[] splits = url.split("http://");
                String webpage = splits[1];
                String mutant = ts;
                try {
                    outputFile = new File(new java.io.File(".").getCanonicalPath() + "/../reports/" + webpage + "/" + mutant + "/");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else {
                String[] splits = url.split("www.");
                String webpage = splits[1];
                String mutant = ts;
                try {
                    outputFile = new File(new File(".").getCanonicalPath() + "/../reports/" + webpage + "/" + mutant + "/");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            FileUtils.forceMkdir(outputFile);
            File dir = new File(outputFile+"/accessibility-report.txt");

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

    private List<IAccessibilityIssue> getAccessibilityIssues() {
        List<IAccessibilityIssue> issues = new ArrayList<>();
        issues.add(new ImageHasAltTag());
        return issues;
    }

}

