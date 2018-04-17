package edu2.gatech.xpert.dom.layout;

import edu2.gatech.xpert.dom.DomNode;

public abstract class AGEdge {

  public abstract DomNode getNode1();

  public abstract DomNode getNode2();

  public abstract String getAttributes();

  protected boolean isTopBottom(AGNode a, AGNode b) {
    return a.y2 < b.y1;
  }

  protected boolean isLeftRight(AGNode a, AGNode b) {
    return a.x2 < b.x1;
  }

  //	protected double topBottomConfidence(AGNode a, AGNode b) {
  //		return Math.abs(a.y2 - b.y1);
  //	}
  //
  //	protected double leftRightConfidence(AGNode a, AGNode b) {
  //		return Math.abs(a.x2 - b.x1);
  //	}

  //	protected double deltaConfidence(int a, int b, int delta) {
  //		return 1 - ( (double) Math.abs(a-b))/delta;
  //	}

  protected boolean equals(int a, int b, int delta) {
    return a <= b + delta && a >= b - delta;
  }
}
