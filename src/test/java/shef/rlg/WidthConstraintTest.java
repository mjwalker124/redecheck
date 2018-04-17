package shef.rlg;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/** Created by thomaswalsh on 21/09/15. */
public class WidthConstraintTest {
  Node parent;
  WidthConstraint wc;

  @Before
  public void setup() {
    parent = new Node("parent");
    wc = new WidthConstraint(400, 900, 0.75, parent, -20);
  }

  @Test
  public void testGetMin() {
    assertEquals(wc.getMin(), 400);
  }

  @Test
  public void testSetMin() {
    wc.setMin(450);
    assertEquals(wc.getMin(), 450);
  }

  @Test
  public void testGetMax() {
    assertEquals(wc.getMax(), 900);
  }

  @Test
  public void testSetMax() {
    wc.setMax(1200);
    assertEquals(wc.getMax(), 1200);
  }

  @Test
  public void testGetPercentage() {
    assertEquals(wc.getPercentage(), 0.75, 0.01);
  }

  @Test
  public void testSetPercentage() {
    wc.setPercentage(0.5);
    assertEquals(wc.getPercentage(), 0.5, 0.01);
  }

  @Test
  public void testGetParent() {
    assertEquals(wc.getParent(), parent);
  }

  @Test
  public void testSetParent() {
    Node newParent = new Node("new");
    wc.setParent(newParent);
    assertEquals(wc.getParent(), newParent);
  }

  @Test
  public void testGetAdjustment() {
    assertEquals(wc.getAdjustment(), -20, 0.01);
  }

  @Test
  public void testSetAdjustment() {
    wc.setAdjustment(50);
    assertEquals(wc.getAdjustment(), 50, 0.01);
  }

  @Test
  public void testToString() {
    String expected = "400 --> 900 : 75.0% of parent + -20.0";
    assertEquals(wc.toString(), expected);
  }

  @Test
  public void testGenerateEquationString() {
    String expected = "75.0% of parent + -20.0";
    assertEquals(wc.generateEquationString(), expected);
  }
}
