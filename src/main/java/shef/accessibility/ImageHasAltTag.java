package shef.accessibility;

import org.apache.xpath.operations.Bool;
import org.json.JSONObject;
import shef.layout.Element;

import java.util.ArrayList;
import java.util.List;

public class ImageHasAltTag implements IAccessibilityIssue
{
    @Override
    public void checkIssue(Element element) {
        System.out.println("***** img test");
        if (element.getTag().equalsIgnoreCase("img")) {
            if (!element.hasAttribute("alt")) {
                System.out.println("****** Warning *****");
                System.out.println("Alt Required for images");
                System.out.println(element.getXpath());
                ImageHasAltTag.imagesWithoutAltTags.add(element);
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
        return ImageHasAltTag.imagesWithoutAltTags.size() + " images do not have alt tags";
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


    public static List<Element> getImagesWithoutAltTags() {
        return imagesWithoutAltTags;
    }

    private static List<Element> imagesWithoutAltTags = new ArrayList<>();
    private Boolean didPass = true;
    private int numberOfTimesTested = 0;
}
