package shef.layout;

/** Created by thomaswalsh on 13/05/2016. */
public abstract class Relationship {
  //    public abstract Element getNode1;
  public abstract Element getNode1();

  public abstract Element getNode2();

  protected boolean equals(int a, int b, int delta) {
    return a <= b + delta && a >= b - delta;
  }
}
