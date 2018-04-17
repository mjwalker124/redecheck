package edu2.gatech.xpert.dom.visitors;

import edu2.gatech.xpert.dom.DomNode;

import java.util.ArrayList;
import java.util.List;

public class LevelAssignVisitor extends DomVisitor {

  List<List<DomNode>> levels;

  public LevelAssignVisitor() {
    init();
  }

  public void init() {
    levels = new ArrayList<>();
  }

  @Override
  public void visit(DomNode node) {
    DomNode parent = node.getParent();
    int level = -1;
    if (parent == null) {
      level = 0;
    } else {
      level = parent.getLevel() + 1;
    }
    if (level >= levels.size()) {
      List<DomNode> l = new ArrayList<DomNode>();
      l.add(node);
      levels.add(l);
    } else {
      levels.get(level).add(node);
    }
    node.setLevel(level);
  }

  public List<List<DomNode>> getLevels() {
    return levels;
  }
}
