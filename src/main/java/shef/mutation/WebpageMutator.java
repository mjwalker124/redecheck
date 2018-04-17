package shef.mutation;

import com.rits.cloning.Cloner;
import cz.vutbr.web.css.*;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import shef.main.Utils;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

// import org.openqa.selenium.phantomjs.PhantomJSDriver;
// import org.openqa.selenium.phantomjs.PhantomJSDriverService;

public class WebpageMutator {

  static String current;
  static Random random;
  WebDriver driver;
  LinkedHashSet<String> cssFiles;
  Cloner cloner;
  Document page;
  LinkedHashMap<String, StyleSheet> stylesheets;
  ArrayList<String> faultyXpaths;
  ArrayList<Element> faultyElements;
  CSSMutator cssMutator;
  ArrayList<RuleMedia> mqCandidates;
  ArrayList<RuleSet> ruleCandidates;
  int usedBlocks;
  int usedDeclarations;
  // Update this to the path to your project
  String preamble = "file:///Users/thomaswalsh/Documents/Workspace/main/testing/";
  String preamble2 = "file:///Users/thomaswalsh/Documents/PhD/Resources/fault-examples/";
  // Storage for mutation candidates and other things
  HashSet<String> usedClassesHTML, usedTagsHTML, usedIdsHTML;
  HashSet<String> usedClassesCSS, usedTagsCSS, usedIdsCSS;
  ArrayList<Element> classCandidates, htmlCandidates;
  String[] tagsIgnore = {
    "A",
    "AREA",
    "B",
    "BLOCKQUOTE",
    "BR",
    "CANVAS",
    "CENTER",
    "CSACTIONDICT",
    "CSSCRIPTDICT",
    "CUFON",
    "CUFONTEXT",
    "DD",
    "EM",
    "EMBED",
    "FIELDSET",
    "FONT",
    "FORM",
    "HR",
    "I",
    "LABEL",
    "LEGEND",
    "LINK",
    "MAP",
    "MENUMACHINE",
    "META",
    "NOFRAMES",
    "NOSCRIPT",
    "OBJECT",
    "OPTGROUP",
    "OPTION",
    "PARAM",
    "S",
    "SCRIPT",
    "SMALL",
    "SPAN",
    "STRIKE",
    "STRONG",
    "STYLE",
    "TT",
    "U"
  };
  String[] wantedProperties = {
    "padding-top",
    "padding-bottom",
    "padding-right",
    "padding-left",
    "width",
    "min-width",
    "max-width",
    "margin",
    "padding",
    "margin-top",
    "margin-bottom",
    "margin-left",
    "margin-right",
    "display",
    "position",
    "float",
    "clear"
  };
  private String htmlContent;
  // Instance variables
  private int numberOfMutants;
  private String shorthand;
  private String baseURL;
  public WebpageMutator(String url, String shorthand, int i, ArrayList<String> nodes) {
    this.baseURL = url;
    this.shorthand = shorthand;
    this.numberOfMutants = i;
    this.faultyXpaths = nodes;
    this.faultyElements = new ArrayList<>();
    random = new Random();
    mqCandidates = new ArrayList<>();
    ruleCandidates = new ArrayList<>();
    usedClassesHTML = new HashSet<>();
    usedClassesCSS = new HashSet<>();
    usedTagsHTML = new HashSet<>();
    usedTagsCSS = new HashSet<>();
    usedIdsHTML = new HashSet<>();
    usedIdsCSS = new HashSet<>();
    classCandidates = new ArrayList<>();
    htmlCandidates = new ArrayList<>();
    cloner = new Cloner();
    usedBlocks = 0;
    usedDeclarations = 0;

    try {
      current = new java.io.File(".").getCanonicalPath();
      DesiredCapabilities dCaps = new DesiredCapabilities();
      dCaps.setJavascriptEnabled(true);
      dCaps.setCapability("takesScreenshot", true);
      //            dCaps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY,
      // current + "/../resources/phantomjs");
      //            String[] phantomArgs = new String[]{"--webdriver-loglevel=NONE"};
      //            dCaps.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, phantomArgs);
      //            driver = new PhantomJSDriver(dCaps);
      extractCssFiles(baseURL);
      parseHTML(baseURL);
      loadInCss(this.baseURL);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    driver.quit();
    cssMutator =
        new CSSMutator(baseURL, shorthand, stylesheets, ruleCandidates, mqCandidates, null, 0);
  }

  public static int countLines(String filename) {
    InputStream is = null;
    try {
      is = new BufferedInputStream(new FileInputStream(filename));
      byte[] c = new byte[1024];
      int count = 0;
      int readChars = 0;
      boolean empty = true;
      while ((readChars = is.read(c)) != -1) {
        empty = false;
        for (int i = 0; i < readChars; ++i) {
          if (c[i] == '\n') {
            ++count;
          }
        }
      }
      is.close();
      return (count == 0 && !empty) ? 1 : count;
    } catch (Exception e) {
    }
    return 0;
  }

  public ArrayList<RuleMedia> getMqCandidates() {
    return mqCandidates;
  }

  public ArrayList<RuleSet> getRuleCandidates() {
    return ruleCandidates;
  }

  @SuppressWarnings("unchecked")
  public void extractCssFiles(String baseURL) {
    //    	DesiredCapabilities dCaps = new DesiredCapabilities();
    //        dCaps.setJavascriptEnabled(true);
    //        dCaps.setCapability("takesScreenshot", true);
    //        String[] phantomArgs = new  String[] {
    //        	    "--webdriver-loglevel=NONE"
    //        	};
    //        dCaps.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, phantomArgs);
    driver = new FirefoxDriver();

    driver.get(preamble2 + baseURL);
    JavascriptExecutor js = (JavascriptExecutor) driver;
    String script = null;
    try {
      current = new java.io.File(".").getCanonicalPath();
      script = Utils.readFile(current + "/../resources/getCssFiles.js");
      //            System.out.println(current + "/resources/getCssFiles.js");
      ArrayList<String> files = (ArrayList<String>) js.executeScript(script);
      //            System.out.println(files.size());
      cssFiles = new LinkedHashSet<>();
      for (int i = 0; i < files.size(); i++) {
        //                System.out.println(files.get(i));
        cssFiles.add(files.get(i));
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    //        driver.quit();
  }

  public void parseHTML(String url) {
    String contents = "";
    try {
      BufferedReader input =
          new BufferedReader(new FileReader((preamble2 + url).replace("file:", "")));
      String inputLine;
      while ((inputLine = input.readLine()) != null) {
        contents += inputLine;
      }
      Document doc = Jsoup.parse(contents);
      page = doc;
      for (Element e : doc.getAllElements()) {
        String xp = buildXpath(e);

        if (faultyXpaths.contains(xp)) {
          //                    System.out.println(xp + " " + e.className());

          faultyElements.add(e);

          //                    // Load in information for HTML mutation
          //                    if (e.classNames().size() > 0) {
          //                        for (String c : e.classNames()) {
          //                            usedClassesHTML.add(c);
          //                            classCandidates.add(e);
          //                        }
          //                    }
          //                    if (!e.ownText().equals("")) {
          //                        htmlCandidates.add(e);
          //                    }
          //                    if (!e.id().equals("")) {
          //                        usedIdsHTML.add("#" + e.id());
          //                    }
          //                    try {
          //                        usedTagsHTML.add(e.tagName());
          //                    } catch(Exception ex) {
          //
          //                    }
          //
          //                    // Do the same for CSS mutation
          //                    if (!ignoreTag(e.tagName().toUpperCase())) {
          //                        if (e.classNames().size() > 0) {
          //                            for (String c : e.classNames()) {
          //                                usedClassesCSS.add("." + c);
          //                            }
          //                        }
          //                        if (!e.id().equals("")) {
          //                            usedIdsCSS.add("#" + e.id());
          //                        }
          //                        try {
          //                            usedTagsCSS.add(e.tagName());
          //                        } catch (Exception ex) {}
          //                    }
        } else {
          //                    System.out.println(xp + " AGAINST " );
        }
      }
      for (Element e : doc.getAllElements()) {
        String xp = buildXpath(e);

        if (faultyXpaths.contains(xp)) {

          // Add in the parent
          if (!faultyElements.contains(e.parent())) {
            //                        System.out.println("P: " + buildXpath(e.parent()));
            faultyElements.add(e.parent());
          } else {
            //                        System.out.println("Already contained " +
            // buildXpath(e.parent()));
          }

          // Add in the siblings
          for (Element sib : e.siblingElements()) {
            if (!faultyElements.contains(sib)) {
              //                            System.out.println("S: " + sib);
              faultyElements.add(sib);
            } else {
              //                            System.out.println("Already contained " +
              // buildXpath(sib));
            }
          }

          // And finally, add in the children
          for (Element c : e.children()) {
            if (!faultyElements.contains(c)) {
              //                            System.out.println("C: " + c);
              faultyElements.add(c);
            } else {
              //                            System.out.println("Already contained " +
              // buildXpath(c));
            }
          }
          //                    System.out.println(faultyElements.size());
        }
      }
      this.htmlContent = contents;
      input.close();
    } catch (IOException e) {
      e.printStackTrace();
      System.out.println("There was a problem layout the HTML of the specified website :(");
    }
  }

  private String buildXpath(Element e) {
    String result = "";
    Element work = e;
    ArrayList<String> tags = new ArrayList<>();
    while (work.parent() != null) {
      int id = getIDAddon(work, work.parent().children());
      if (id > 1) {
        tags.add(work.tagName() + "[" + id + "]");
      } else {
        tags.add(work.tagName());
      }

      work = work.parent();
    }
    for (String t : tags) {
      result = "/" + t.toUpperCase() + result;
    }
    return result;
  }

  private int getIDAddon(Element e, Elements children) {
    // Check whether this is the only child with this tag
    ArrayList<Element> sameTags = new ArrayList<>();
    for (Element c : children) {
      //            if (c != e) {
      if (c.tagName() == e.tagName()) {
        //                    if (e.tagName().equals("div")) {
        //                        System.out.println(e.tagName());
        //                        System.out.println();
        //                    }
        sameTags.add(c);
      }
      //            }
    }
    return sameTags.indexOf(e) + 1;
  }

  @SuppressWarnings({"unused", "rawtypes"})
  public void loadInCss(String base) {
    stylesheets = new LinkedHashMap<String, StyleSheet>();
    URL cssUrl = null;
    URLConnection conn;
    LinkedHashMap<String, String> cssContent = new LinkedHashMap<String, String>();
    int counter = 0;
    for (String cssFile : cssFiles) {
      String contents = "";
      try {
        if (cssFile.contains("http")) {
          cssUrl = new URL(cssFile);
        } else if (cssFile.substring(0, 2).equals("//")) {
          cssUrl = new URL("http:" + cssFile);
          break;
        } else {
          cssUrl = new URL((preamble2 + shorthand + "/" + cssFile.replace("./", "")));
        }
        //                System.out.println(cssUrl);
        conn = cssUrl.openConnection();
        BufferedReader input = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        while ((inputLine = input.readLine()) != null) {
          contents += inputLine;
        }
        contents += "\n\n";
        cssContent.put(cssFile, contents);
      } catch (Exception e) {
        e.printStackTrace();
        System.out.println("Problem loading or layout the CSS file " + cssUrl.toString());
      }
      counter++;
    }
    StyleSheet ss = null;
    for (String k : cssContent.keySet()) {
      String s = cssContent.get(k);
      try {
        //				String prettified = CSSMutator.prettifyCss(s, driver);
        StyleSheet temp = CSSFactory.parse(s);
        StyleSheet toSave = CSSFactory.parse(s);
        stylesheets.put(k, toSave);
        //
        for (RuleBlock rb : temp.asList()) {
          if (rb instanceof RuleSet) {
            if (ruleBlockApplied(((RuleSet) rb).getSelectors())) {
              usedBlocks++;
              RuleSet rs = (RuleSet) rb;
              List<Declaration> decs = rs.asList();
              usedDeclarations += decs.size();
              ArrayList<Declaration> decsToKeep = new ArrayList<Declaration>();
              filterDeclarations(decs, decsToKeep);

              if (decsToKeep.size() > 0) {
                rs.replaceAll(decsToKeep);
                ruleCandidates.add(rs);
              }
            }
          } else if (rb instanceof RuleMedia) {
            RuleMedia rm = (RuleMedia) rb;
            if (CSSMutator.hasNumericQuery(rm)) {
              ArrayList<RuleSet> blocksToKeep = new ArrayList<RuleSet>();
              List<RuleSet> sets = rm.asList();
              for (RuleSet rs : sets) {
                if (ruleBlockApplied(rs.getSelectors())) {
                  usedBlocks++;
                  List<Declaration> decs = rs.asList();
                  usedDeclarations += decs.size();
                  ArrayList<Declaration> decsToKeep = new ArrayList<Declaration>();
                  filterDeclarations(decs, decsToKeep);

                  if (decsToKeep.size() > 0) {
                    rs.replaceAll(decsToKeep);
                    blocksToKeep.add(rs);
                  }
                } else {
                }
              }
              rm.replaceAll(blocksToKeep);
              if (rm.asList().size() > 0) {
                mqCandidates.add(rm);
                //                                System.out.println("Keeping " + rm.toString());
              }
            }
          }
        }

      } catch (IOException e) {
        e.printStackTrace();
      } catch (CSSException e) {
        e.printStackTrace();
      } catch (NullPointerException e) {
        e.printStackTrace();
      }
    }
  }

  private void filterDeclarations(List<Declaration> decs, ArrayList<Declaration> decsToKeep) {
    for (Declaration d : decs) {
      List<Term<?>> terms = d.asList();
      for (Term t : terms) {
        if ((t instanceof TermLength) || (t instanceof TermPercent) || (t instanceof TermIdent)) {
          for (String p : wantedProperties) {
            if (d.getProperty().toLowerCase().equals(p)) {
              if (!decsToKeep.contains(d)) {
                decsToKeep.add(d);
              }
            }
          }
        }
      }
    }
  }

  private boolean ignoreTag(String tagName) {
    for (int i = 0; i < tagsIgnore.length; i++) {
      if (tagsIgnore[i].equals(tagName)) {
        return true;
      }
    }
    return false;
  }

  public boolean ruleBlockApplied(List<CombinedSelector> sels) {
    for (CombinedSelector s : sels) {
      if (s.toString().contains(">")) {
        String[] splits = s.toString().split(">");
        //                String child = splits[splits.length - 1];
        //                String parent = splits[splits.length - 2];
        //                if (selectorUsed(child, false) && selectorUsed(parent, true)) {
        //                    return true;
        //                }
        return traceSelectors(splits);
      } else if (s.toString().contains(" ")) {
        String[] splits = s.toString().split(" ");
        //                String child = splits[splits.length - 1];
        //                String parent = splits[splits.length - 2];
        //                if (selectorUsed(child, false) && selectorUsed(parent, true)) {
        //                    return true;
        //                }
        return traceSelectors(splits);
      } else {
        return traceSelectors(new String[] {s.toString()});
        //                if (selectorUsed(s.toString(), false)) {
        //                    return true;
        //                }
      }
    }
    return false;
  }

  private boolean traceSelectors(String[] splits) {
    boolean foundMatch = false;

    for (Element e : faultyElements) {
      boolean wholeElementMatch = true;
      Element temp = e;
      String currentSelector;
      for (int i = splits.length - 1; i >= 0; i--) {
        currentSelector = splits[i];
        if (((temp.id().equals(currentSelector.replace("#", ""))
            || (temp.tagName().equals(currentSelector))
            || (temp.hasClass(currentSelector.replace(".", "")))))) {
          temp = e.parent();
        } else {
          wholeElementMatch = false;
          break;
        }
      }
      if (wholeElementMatch) {
        foundMatch = true;
      }
    }
    return foundMatch;
  }

  private boolean selectorUsed(String selector, boolean b) {
    for (Element e : faultyElements) {

      // Work out the element we're actually comparing against
      Element toActuallyCheck;
      if (!b) {
        toActuallyCheck = e;
      } else {
        toActuallyCheck = e.parent();
      }

      // Check whether the id is a match
      if (toActuallyCheck.id().equals(selector.replace("#", ""))) {
        return true;

        // Check whether the tag is a match
      } else if (toActuallyCheck.tagName().equals(selector)) {
        return true;

        // Check whether the element has the right class
      } else if (toActuallyCheck.hasClass(selector.replace(".", ""))) {
        return true;
      }
    }
    return false;
  }

  public String copyFromWebpageRepository() {
    try {
      String o = preamble2.replace("file://", "") + baseURL.split("/")[0];
      File original = new File(o);
      File copied =
          new File(new java.io.File(".").getCanonicalPath() + "/../fix-attempts/" + this.shorthand);
      //	        System.out.println(copied.getAbsolutePath());
      FileUtils.copyDirectory(original, copied, false);
      return copied.getAbsolutePath();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  private void copyResourcesDirectory(int num) {
    try {
      String current = new java.io.File(".").getCanonicalPath() + "/testing/" + this.shorthand;
      //            System.out.println(current);
      File original = new File(current + "/index/resources");
      File copied = new File(current + "/mutant" + num + "/resources");
      //            System.out.println(copied.getAbsolutePath());
      FileUtils.copyDirectory(original, copied, false);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  //    public static String getWebpageData(File f) {
  //        String result = "";
  //        String dirString = f.toString().replace("main-icst/reports", "fault-examples");
  //        try {
  //            int htmlLines = countLines((dirString+"/index.html").replace("file:", ""));
  //            System.out.println(htmlLines);
  //
  //            int numDomNodes = 2;
  //            int numBlocks = 0;
  //            int numDecs = 0;
  //            int numCSSSelectors;
  //            ArrayList<Element> worklist = new ArrayList<Element>();
  //            worklist.add(this.page.head());
  //            worklist.add(this.page.body());
  //            while (worklist.size() > 0) {
  //                Element e = worklist.remove(0);
  //                numDomNodes += e.children().size();
  //                worklist.addAll(e.children());
  //            }
  //        } catch (IOException e) {
  //            e.printStackTrace();
  //        }
  //        return result;
  //    }

  public String getStatistics(String url) throws IOException {
    int htmlLines = countLines((preamble + url + "/index/index.html").replace("file:", ""));
    int cssLines = 0;
    //        for (String cssFile : cssFiles) {
    //            cssLines += countLines(preamble.replace("file:","") + shorthand + "/index/" +
    // cssFile.replace("./",""));
    //        }
    int numDomNodes = 2;
    int numBlocks = 0;
    int numDecs = 0;
    int numCSSSelectors;
    ArrayList<Element> worklist = new ArrayList<Element>();
    worklist.add(this.page.head());
    worklist.add(this.page.body());
    while (worklist.size() > 0) {
      Element e = worklist.remove(0);
      numDomNodes += e.children().size();
      worklist.addAll(e.children());
    }

    for (StyleSheet ss : stylesheets.values()) {
      for (RuleBlock rb : ss) {
        numBlocks++;
        if (rb instanceof RuleSet) {
          RuleSet casted = (RuleSet) rb;
          for (Declaration d : casted.asList()) {
            numDecs++;
          }
        } else if (rb instanceof RuleMedia) {
          RuleMedia casted2 = (RuleMedia) rb;
          for (RuleSet rs : casted2.asList()) {
            numBlocks++;
            for (Declaration d : rs.asList()) {
              numDecs++;
            }
          }
        }
      }
    }
    //        String countscript = Utils.readFile(current + "/resources/countCSSRules.js");
    //        ((JavascriptExecutor)driver).executeScript(countscript)
    return " & " + numDomNodes + " & " + numDecs;
    //        + "(" + usedDeclarations + ")";
    //        return " & " + htmlLines + " & " + numDomNodes + " & " + cssLines + " & " + numBlocks
    // + "(" + usedBlocks + ") & " + numDecs + "(" + usedDeclarations + ")";
  }

  public int getElementCount(String url) throws IOException {
    int htmlLines = countLines((url + "/index.html").replace("file:", ""));
    int cssLines = 0;
    //        for (String cssFile : cssFiles) {
    //            cssLines += countLines(preamble.replace("file:","") + shorthand + "/index/" +
    // cssFile.replace("./",""));
    //        }
    int numDomNodes = 2;

    ArrayList<Element> worklist = new ArrayList<Element>();
    worklist.add(this.page.head());
    worklist.add(this.page.body());
    while (worklist.size() > 0) {
      Element e = worklist.remove(0);
      numDomNodes += e.children().size();
      worklist.addAll(e.children());
    }

    return numDomNodes;
  }

  public void mutate(String newUrl) {
    boolean mutated = false;
    while (!mutated) {
      try {
        Document toMutate = cloner.deepClone(page);
        int selector = random.nextInt(5);
        //                System.out.println(selector);
        if (selector == 3 || selector == 4) {
          if (mqCandidates.size() == 0) {
            throw new Exception("No media queries");
          }
        }

        if (selector <= 4) {
          //                    System.out.println("Mutating CSS");
          CSSMutator cssMutator =
              new CSSMutator(
                  baseURL, shorthand, stylesheets, ruleCandidates, mqCandidates, toMutate, 0);
          cssMutator.mutate(selector, newUrl);
          mutated = true;
        } else {
          HTMLMutator htmlMutator =
              new HTMLMutator(
                  baseURL,
                  shorthand,
                  stylesheets,
                  classCandidates,
                  htmlCandidates,
                  toMutate,
                  usedClassesHTML,
                  usedIdsHTML,
                  usedTagsHTML,
                  0);
          htmlMutator.mutate(selector);
        }
        //                copyFromWebpageRepository();
        //                copyResourcesDirectory(i);
      } catch (Exception e) {
        //                e.printStackTrace();
      }
    }
  }

  public String getShorthand() {
    return shorthand;
  }

  public void writeInitialParsedCSS(String newUrl) {
    Document toMutate = cloner.deepClone(page);
    CSSMutator cssMutator =
        new CSSMutator(baseURL, shorthand, stylesheets, ruleCandidates, mqCandidates, toMutate, 0);
    cssMutator.writeToFile(1, cssMutator.stylesheets, shorthand, newUrl);
  }

  public CSSMutator getCSSMutator() {
    return cssMutator;
  }

  //    public int getDeclarationCount() {
  //        int numBlocks = 0;
  //        int numDecs = 0;
  //        int numCSSSelectors;
  //
  //        for (StyleSheet ss : stylesheets.values()) {
  //            for (RuleBlock rb : ss) {
  //                numBlocks++;
  //                if (rb instanceof RuleSet) {
  //                    RuleSet casted  = (RuleSet) rb;
  //                    for (Declaration d : casted.asList()) {
  //                        numDecs++;
  //                    }
  //                } else if (rb instanceof RuleMedia) {
  //                    RuleMedia casted2 = (RuleMedia) rb;
  //                    for (RuleSet rs : casted2.asList()) {
  //                        numBlocks++;
  //                        for (Declaration d : rs.asList()) {
  //                            numDecs++;
  //                        }
  //                    }
  //                }
  //            }
  //        }
  //        return numDecs;
  //    }

  //	public static void main(String[] args) throws IOException {
  //
  //        String stats = "";
  //        String[] webpages = new String[] {
  //                "3-Minute-Journal",
  //                "AccountKiller",
  //                "AirBnb",
  //                "BugMeNot",
  //                "CloudConvert",
  //                "Covered-Calendar",
  //                "Days-Old",
  //                "Dictation",
  //                "Duolingo",
  //                "GetPocket",
  //                "Honey",
  //                "HotelWifiTest",
  //                "Mailinator",
  //                "MidwayMeetup",
  //                "Ninite-new",
  //                "Pdf-Escape",
  //                "PepFeed",
  //                "RainyMood",
  //                "RunPee",
  //                "StumbleUpon",
  //                "TopDocumentary",
  //                "UserSearch",
  //                "WhatShouldIReadNext",
  //                "WillMyPhoneWork",
  //                "ZeroDollarMovies"};
  ////                {"aftrnoon.com", "annettescreations.net", "ashtonsnook.com", "bittorrent.com",
  // "coursera.com", "denondj.com", "getbootstrap.com", "issta.cispa", "namemesh.com",
  // "paydemand.com", "rebeccamade.com", "reserve.com", "responsiveprocess.com", "shield.com",
  // "teamtreehouse.com"};
  //
  //
  //
  ////		System.setProperty("phantomjs.binary.path", current + "/../resources/phantomjs");
  //		for (String wp : webpages) {
  //            WebpageMutator mutator = new WebpageMutator(wp+"/index.html", wp, 0, nodes);
  ////
  //            Document toMutate = mutator.cloner.deepClone(mutator.page);
  //            stats += wp + mutator.getStatistics(wp) + "\n";
  ////            System.out.println(mutator.usedClassesHTML.size() + mutator.usedIdsHTML.size() +
  // mutator.usedTagsHTML.size());
  ////            System.out.println(mutator.usedClassesCSS.size() + mutator.usedIdsCSS.size() +
  // mutator.usedTagsCSS.size());
  ////            for (int i = 1; i <= mutator.numberOfMutants; i++) {
  ////                try {
  ////                    int selector = random.nextInt(8);
  ////                    if (selector == 2 || selector == 3) {
  ////                        if (mutator.mqCandidates.size() == 0) {
  ////                            throw new Exception();
  ////                        }
  ////                    }
  ////                    if (selector <= 3) {
  ////                        CSSMutator cssMutator = new CSSMutator(mutator.baseURL,
  // mutator.shorthand, mutator.stylesheets, mutator.ruleCandidates, mutator.mqCandidates, toMutate,
  // i);
  ////                        cssMutator.mutate(selector);
  ////                    } else {
  ////                        HTMLMutator htmlMutator = new HTMLMutator(mutator.baseURL,
  // mutator.shorthand, mutator.stylesheets, mutator.classCandidates, mutator.htmlCandidates,
  // toMutate, mutator.usedClassesHTML, mutator.usedIdsHTML, mutator.usedTagsHTML, i);
  ////                        htmlMutator.mutate(selector);
  ////                    }
  ////                    mutator.copyResourcesDirectory(i);
  ////                } catch (Exception e) {
  ////
  ////                }
  ////            }
  //        }
  //
  //        System.out.println(stats);
  //	}

}
