package shef.accessibility;

import shef.layout.Element;

public interface IAccessibilityIssue {
    void checkIssue(Element element);
    boolean getDidPass();
    String getErrorMessage();
    String getFixInstructions();
    String consoleOutput();
    boolean isAffectedByLayouts();
    int numberOfTimesTested();
    void incNumberOfTimesTested();
}
