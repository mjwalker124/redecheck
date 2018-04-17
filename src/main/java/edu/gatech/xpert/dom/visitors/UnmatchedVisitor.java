package edu.gatech.xpert.dom.visitors;

import edu.gatech.xpert.dom.DomNode;

import java.util.ArrayList;
import java.util.List;

public class UnmatchedVisitor extends DomVisitor {

  List<DomNode> unmatched;

  public UnmatchedVisitor() {
    init();
  }

  public void init() {
    unmatched = new ArrayList<>();
  }

  @Override
  public void visit(DomNode node) {
    if (!node.isMatched()) {
      unmatched.add(node);
    }
  }

  public List<DomNode> getUnmatched() {
    return unmatched;
  }
}
