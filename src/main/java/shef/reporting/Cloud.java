package shef.reporting;

import shef.accessibility.IAccessibilityIssue;

import java.util.List;

public class Cloud {
  private List<IAccessibilityIssue> accessibilityIssues;

  public Cloud(List<IAccessibilityIssue> accessibilityIssues) {
    this.accessibilityIssues = accessibilityIssues;
  }

  public void upload() {}
}
