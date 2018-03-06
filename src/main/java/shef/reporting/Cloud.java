package shef.reporting;

import shef.accessibility.IAccessibilityIssue;

import java.util.List;

public class Cloud {
    public Cloud(List<IAccessibilityIssue> accessibilityIssues) {
        this.accessibilityIssues = accessibilityIssues;
    }

    public void upload() {

    }

    private List<IAccessibilityIssue> accessibilityIssues;
}
