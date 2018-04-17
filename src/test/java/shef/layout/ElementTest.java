package shef.layout;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/** Created by thomaswalsh on 18/11/2016. */
public class ElementTest {
  Element e;
  Element p, q;

  @Before
  public void setUp() {
    e = new Element("testXpath", 10, 10, 20, 20);
    p = new Element("parent", 0, 0, 30, 30);
    q = new Element("another", 50, 50, 100, 100);
  }

  @Test
  public void getContentCoords() {}

  @Test
  public void getStyles() {}

  @Test
  public void getXpath() {}

  @Test
  public void getBoundingCoordinates() {}

  @Test
  public void getRectangle() {}

  @Test
  public void setParent() {
    e.setParent(p);
    assertEquals(p, e.getParent());
    e.setParent(q);
    assertEquals(q, e.getParent());
  }

  @Test
  public void addChild() {}

  @Test
  public void setY1() {
    e.setY1(20);
    assertEquals(20, e.getY1());
  }

  @Test
  public void setY2() {
    e.setY2(60);
    assertEquals(60, e.getY2());
  }

  @Test
  public void getBoundingCoords() {
    int[] expected = new int[] {10, 10, 20, 20};
    int[] actual = e.getBoundingCoords();
    assertArrayEquals(expected, actual);
  }

  @Test
  public void setContentCoords() {}

  @Test
  public void getContentRectangle() {}

  @Test
  public void setStyles() {}
}
