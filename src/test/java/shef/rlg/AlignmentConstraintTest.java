package shef.rlg;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/** Created by thomaswalsh on 18/09/15. */
public class AlignmentConstraintTest {
  Node n1, n2, n3;
  AlignmentConstraint acPC, acSib;

  @Before
  public void setup() {
    n1 = new Node("first");
    n2 = new Node("second");
    n3 = new Node("third");
    acPC =
        new AlignmentConstraint(
            n1,
            n2,
            Type.PARENT_CHILD,
            300,
            800,
            new boolean[] {true, false, false, false, true, false},
            null);
    acSib =
        new AlignmentConstraint(
            n1,
            n2,
            Type.SIBLING,
            400,
            1000,
            new boolean[] {
              true, false, false, false, false, false, true, true, false, false, false
            },
            null);
  }

  @Test
  public void testToStringContains() {
    String expected = "first , second , PARENT_CHILD , 300 , 800 ,  {centeredtop}";
    assertEquals(acPC.toString(), expected);
  }

  @Test
  public void testToStringSibling() {
    String expected = "first , second , SIBLING , 400 , 1000 ,  {aboveyMidAlignleftAlign}";
    assertEquals(acSib.toString(), expected);
  }

  @Test
  public void testGetNode1() {
    assertEquals(acPC.getNode1(), n1);
  }

  @Test
  public void testSetNode1() {
    acPC.setNode1(n3);
    assertEquals(acPC.getNode1(), n3);
  }

  @Test
  public void testGetNode2() {
    assertEquals(acPC.getNode2(), n2);
  }

  @Test
  public void testSetNode2() {
    acPC.setNode2(n3);
    assertEquals(acPC.getNode2(), n3);
  }

  @Test
  public void testGetType() {
    assertEquals(acPC.getType(), Type.PARENT_CHILD);
  }

  @Test
  public void testSetType() {
    acPC.setType(Type.SIBLING);
    assertEquals(acPC.getType(), Type.SIBLING);
  }

  @Test
  public void testSetMin() {
    acPC.setMin(500);
    assertEquals(acPC.getMin(), 500);
  }

  @Test
  public void testGetAttributesContains() {
    assertTrue(
        Arrays.equals(
            acPC.getAttributes(), new boolean[] {true, false, false, false, true, false}));
  }

  @Test
  public void testSetAttributesContains() {
    acPC.setAttributes(new boolean[] {false, false, true, false, true, false});
    assertTrue(
        Arrays.equals(
            acPC.getAttributes(), new boolean[] {false, false, true, false, true, false}));
  }

  //    new boolean[] {true, false, false, false, false, false, true, true}
  @Test
  public void testGetAttributesSibling() {
    assertTrue(
        Arrays.equals(
            acSib.getAttributes(),
            new boolean[] {
              true, false, false, false, false, false, true, true, false, false, false
            }));
  }

  @Test
  public void testSetAttributesSibling() {
    acSib.setAttributes(
        new boolean[] {false, false, true, false, true, false, false, false, false, false});
    assertTrue(
        Arrays.equals(
            acSib.getAttributes(),
            new boolean[] {false, false, true, false, true, false, false, false, false, false}));
  }

  @Test
  public void testGenerateKeyWithoutLabelsContains() {
    assertEquals(acPC.generateKeyWithoutLabels(), "second contains first");
  }

  @Test
  public void testGenerateKeyWithoutLabelsSibling() {
    assertEquals(acSib.generateKeyWithoutLabels(), "first sibling of second");
  }

  @Test
  public void testGenerateKeyContains() {
    assertEquals(acPC.generateKey(), "first contains second {centeredtop}");
  }

  @Test
  public void testGenerateKeySibling() {
    assertEquals(acSib.generateKey(), "first sibling of second {aboveyMidAlignleftAlign}");
  }

  @Test
  public void testGenerateLabellingParentChild() {
    assertEquals(acPC.generateLabelling(), " {centeredtop}");
  }

  @Test
  public void generateLabellingPCRestOfAttributes() {
    boolean[] newAtts = new boolean[] {false, true, true, true, false, true};
    acPC.setAttributes(newAtts);
    assertEquals(acPC.generateLabelling(), " {leftJustrightJustmiddlebottom}");
  }

  @Test
  public void generateLabellingSibRestOfAttributes() {
    boolean[] newAtts =
        new boolean[] {
          false, true, true, true, true, true, false, false, false, false, false, false, false
        };
    acSib.setAttributes(newAtts);
    assertEquals(acSib.generateLabelling(), " {belowleftOfrightOftopAlignbottomAlign}");
  }

  @Test
  public void testGenerateLabellingSibling() {
    assertEquals(acSib.generateLabelling(), " {aboveyMidAlignleftAlign}");
  }

  @Test
  public void testGetMax() {
    assertEquals(acPC.getMax(), 800);
  }

  @Test
  public void testSetMax() {
    acPC.setMax(1000);
    assertEquals(acPC.getMax(), 1000);
  }

  @Test
  public void testGetMin() {
    assertEquals(acPC.getMin(), 300);
  }

  @Test
  public void testCompareToPos() {
    assertTrue(acPC.compareTo(acSib) > 0);
  }

  @Test
  public void testCompareToNeg() {
    assertTrue(acSib.compareTo(acPC) < 0);
  }
}
