package shef.analysis;

import com.google.common.collect.HashBasedTable;
import com.rits.cloning.Cloner;
import org.junit.Before;
import org.junit.Test;
import shef.reporting.regression.WidthError;
import shef.rlg.*;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/** Created by thomaswalsh on 25/09/15. */
public class RLGComparatorTest {
  RLGComparator comp;
  ResponsiveLayoutGraph rlg1, rlg2;

  @Before
  public void setup() {
    rlg1 = mock(ResponsiveLayoutGraph.class);
    rlg2 = mock(ResponsiveLayoutGraph.class);
    comp = spy(new RLGComparator(rlg1, rlg2, new int[] {}));
    comp.cloner = new Cloner();
  }

  @Test
  public void testCompare() {}

  @Test
  public void testCompareMatchedNodes() {}

  @Test
  public void testMatchNodesTrue() {
    Node n1 = new Node("first");
    Node n2 = new Node("second");
    HashMap<String, Node> map1 = new HashMap<>();
    HashMap<String, Node> map2 = new HashMap<>();
    map1.put(n1.getXpath(), n1);
    map1.put(n2.getXpath(), n2);
    map2.put(n1.getXpath(), n1);
    map2.put(n2.getXpath(), n2);

    comp.matchedNodes = new HashMap<>();

    when(comp.rlg1.getNodes()).thenReturn(map1);
    when(comp.rlg2.getNodes()).thenReturn(map2);

    comp.matchNodes();

    // Check both nodes are added to matchedNodes
    assertEquals(2, comp.matchedNodes.size());
  }

  @Test
  public void testMatchNodesFalse() {
    Node n1 = new Node("first");
    Node n2 = new Node("second");
    Node n3 = new Node("third");
    HashMap<String, Node> map1 = new HashMap<>();
    HashMap<String, Node> map2 = new HashMap<>();
    map1.put(n1.getXpath(), n1);
    map1.put(n2.getXpath(), n2);
    map2.put(n1.getXpath(), n1);
    map2.put(n3.getXpath(), n3);

    comp.matchedNodes = new HashMap<>();

    when(comp.rlg1.getNodes()).thenReturn(map1);
    when(comp.rlg2.getNodes()).thenReturn(map2);

    comp.matchNodes();

    // Check both nodes are added to matchedNodes
    assertEquals(1, comp.matchedNodes.size());
  }

  @Test
  public void testCompareVisibilityConstraintsTrue() {
    Node n1 = new Node("first");
    Node n2 = new Node("first");
    VisibilityConstraint vc = new VisibilityConstraint(400, 1000);
    n1.addVisibilityConstraint(vc);
    n2.addVisibilityConstraint(vc);
    comp.compareVisibilityConstraints(n1, n2);
    assertEquals(0, RLGComparator.vcErrors.size());
  }

  @Test
  public void testCompareVisibilityConstraintsFalse() {
    Node n1 = new Node("first");
    Node n2 = new Node("first");
    VisibilityConstraint vc = new VisibilityConstraint(400, 1000);
    VisibilityConstraint vc2 = new VisibilityConstraint(500, 1100);
    n1.addVisibilityConstraint(vc);
    n2.addVisibilityConstraint(vc2);
    comp.compareVisibilityConstraints(n1, n2);
    assertEquals(1, RLGComparator.vcErrors.size());
  }

  @Test
  public void testCompareAlignmentConstraintsMatch() {
    Node n1 = new Node("first");
    Node n2 = new Node("first");
    Node parent = new Node("parent");
    AlignmentConstraint ac =
        new AlignmentConstraint(
            n1, parent, Type.PARENT_CHILD, 400, 800, new boolean[6], new boolean[2]);
    AlignmentConstraint ac2 =
        new AlignmentConstraint(
            n2, parent, Type.PARENT_CHILD, 400, 800, new boolean[6], new boolean[2]);
    HashBasedTable<String, int[], AlignmentConstraint> map1 = HashBasedTable.create();
    map1.put(ac.generateKey(), new int[] {400, 0}, ac);
    HashBasedTable<String, int[], AlignmentConstraint> map2 = HashBasedTable.create();
    map2.put(ac2.generateKey(), new int[] {400, 0}, ac2);

    when(rlg1.getAlignmentConstraints()).thenReturn(map1);
    when(rlg2.getAlignmentConstraints()).thenReturn(map2);

    comp.compareAlignmentConstraints(n1, n2);
    assertEquals(0, RLGComparator.acErrors.size());
  }

  @Test
  public void testCompareAlignmentConstraintsMatchDiffBounds() {
    Node n1 = new Node("first");
    Node n2 = new Node("first");
    Node parent = new Node("parent");
    AlignmentConstraint ac =
        new AlignmentConstraint(
            n1, parent, Type.PARENT_CHILD, 400, 800, new boolean[6], new boolean[2]);
    AlignmentConstraint ac2 =
        new AlignmentConstraint(
            n2, parent, Type.PARENT_CHILD, 400, 805, new boolean[6], new boolean[2]);
    HashBasedTable<String, int[], AlignmentConstraint> map1 = HashBasedTable.create();
    map1.put(ac.generateKey(), new int[] {400, 0}, ac);
    HashBasedTable<String, int[], AlignmentConstraint> map2 = HashBasedTable.create();
    map2.put(ac2.generateKey(), new int[] {400, 0}, ac2);

    when(rlg1.getAlignmentConstraints()).thenReturn(map1);
    when(rlg2.getAlignmentConstraints()).thenReturn(map2);

    comp.compareAlignmentConstraints(n1, n2);
    assertEquals(1, RLGComparator.acErrors.size());
    assertEquals(true, RLGComparator.acErrors.get(0).getDesc().equals("diffBounds"));
  }

  @Test
  public void testCompareAlignmentConstraintsMatchDiffAttributes() {
    Node n1 = new Node("first");
    Node n2 = new Node("first");
    Node parent = new Node("parent");
    AlignmentConstraint ac =
        new AlignmentConstraint(
            n1,
            parent,
            Type.PARENT_CHILD,
            400,
            800,
            new boolean[] {true, false, false, false, false, false},
            new boolean[2]);
    AlignmentConstraint ac2 =
        new AlignmentConstraint(
            n2,
            parent,
            Type.PARENT_CHILD,
            400,
            800,
            new boolean[] {false, true, false, false, false, false},
            new boolean[2]);
    HashBasedTable<String, int[], AlignmentConstraint> map1 = HashBasedTable.create();
    map1.put(ac.generateKey(), new int[] {400, 0}, ac);
    HashBasedTable<String, int[], AlignmentConstraint> map2 = HashBasedTable.create();
    map2.put(ac2.generateKey(), new int[] {400, 0}, ac2);

    when(rlg1.getAlignmentConstraints()).thenReturn(map1);
    when(rlg2.getAlignmentConstraints()).thenReturn(map2);

    comp.compareAlignmentConstraints(n1, n2);
    assertEquals(1, RLGComparator.acErrors.size());
    assertEquals(true, RLGComparator.acErrors.get(0).getDesc().equals("diffAttributes"));
  }

  @Test
  public void testCompareAlignmentConstraintsMatchUnmatchedOracle() {
    Node n1 = new Node("first");
    Node n2 = new Node("first");
    Node parent = new Node("parent");
    Node n3 = new Node("third");
    AlignmentConstraint ac =
        new AlignmentConstraint(
            n1,
            parent,
            Type.PARENT_CHILD,
            400,
            800,
            new boolean[] {true, false, false, false, false, false},
            new boolean[2]);
    AlignmentConstraint ac2 =
        new AlignmentConstraint(
            n2,
            parent,
            Type.PARENT_CHILD,
            400,
            800,
            new boolean[] {true, false, false, false, false, false},
            new boolean[2]);
    AlignmentConstraint ac3 =
        new AlignmentConstraint(n1, n3, Type.SIBLING, 500, 1000, new boolean[11], new boolean[2]);
    HashBasedTable<String, int[], AlignmentConstraint> map1 = HashBasedTable.create();
    map1.put(ac.generateKey(), new int[] {400, 800}, ac);
    map1.put(ac3.generateKey(), new int[] {500, 1000}, ac3);
    HashBasedTable<String, int[], AlignmentConstraint> map2 = HashBasedTable.create();
    map2.put(ac2.generateKey(), new int[] {400, 800}, ac2);

    when(rlg1.getAlignmentConstraints()).thenReturn(map1);
    when(rlg2.getAlignmentConstraints()).thenReturn(map2);

    comp.compareAlignmentConstraints(n1, n2);
    assertEquals(1, RLGComparator.acErrors.size());
    assertEquals(true, RLGComparator.acErrors.get(0).getDesc().equals("unmatched-oracle"));
  }

  @Test
  public void testCompareAlignmentConstraintsMatchUnmatchedTest() {
    Node n1 = new Node("first");
    Node n2 = new Node("first");
    Node parent = new Node("parent");
    Node n3 = new Node("third");
    AlignmentConstraint ac =
        new AlignmentConstraint(
            n1,
            parent,
            Type.PARENT_CHILD,
            400,
            800,
            new boolean[] {true, false, false, false, false, false},
            new boolean[2]);
    AlignmentConstraint ac2 =
        new AlignmentConstraint(
            n2,
            parent,
            Type.PARENT_CHILD,
            400,
            800,
            new boolean[] {true, false, false, false, false, false},
            new boolean[2]);
    AlignmentConstraint ac3 =
        new AlignmentConstraint(n2, n3, Type.SIBLING, 500, 1000, new boolean[11], new boolean[2]);
    HashBasedTable<String, int[], AlignmentConstraint> map1 = HashBasedTable.create();
    map1.put(ac.generateKey(), new int[] {400, 800}, ac);

    HashBasedTable<String, int[], AlignmentConstraint> map2 = HashBasedTable.create();
    map2.put(ac2.generateKey(), new int[] {400, 800}, ac2);
    map2.put(ac3.generateKey(), new int[] {500, 1000}, ac3);

    when(rlg1.getAlignmentConstraints()).thenReturn(map1);
    when(rlg2.getAlignmentConstraints()).thenReturn(map2);

    comp.compareAlignmentConstraints(n1, n2);
    assertEquals(1, RLGComparator.acErrors.size());
    assertEquals(true, RLGComparator.acErrors.get(0).getDesc().equals("unmatched-test"));
  }

  //    @Test
  //    public void testCompareWidthConstraintsMatch() throws Exception {
  //        Node n1 = new Node("first");
  //        Node n2 = new Node("first");
  //        Node p = new Node("parent");
  //        WidthConstraint wc1 = new WidthConstraint(400,1000, 1, p, 0);
  //        n1.addWidthConstraint(wc1);
  //        n2.addWidthConstraint(wc1);
  //
  //        comp.compareWidthConstraints(n1, n2);
  //        assertEquals(0, comp.wcErrors.size());
  //    }
  //
  //    @Test
  //    public void testCompareWidthConstraintsDiffBounds() throws Exception {
  //        Node n1 = new Node("first");
  //        Node n2 = new Node("first");
  //        Node p = new Node("parent");
  //        WidthConstraint wc1 = new WidthConstraint(400,1000, 1, p, 0);
  //        WidthConstraint wc2 = new WidthConstraint(390, 1000, 1, p, 0);
  //        n1.addWidthConstraint(wc1);
  //        n2.addWidthConstraint(wc2);
  //
  //        comp.compareWidthConstraints(n1, n2);
  //        assertEquals(1, comp.wcErrors.size());
  //        assertEquals(true, comp.wcErrors.get(0).getDesc().equals("diffBounds"));
  //    }
  //
  //    @Test
  //    public void testCompareWidthConstraintsDiffCoefficients() throws Exception {
  //        Node n1 = new Node("first");
  //        Node n2 = new Node("first");
  //        Node p = new Node("parent");
  //        WidthConstraint wc1 = new WidthConstraint(400,1000, 1, p, 0);
  //        WidthConstraint wc2 = new WidthConstraint(400, 1000, 1, p, -20);
  //        n1.addWidthConstraint(wc1);
  //        n2.addWidthConstraint(wc2);
  //
  //        comp.compareWidthConstraints(n1, n2);
  //        assertEquals(1, comp.wcErrors.size());
  //        assertEquals(true, comp.wcErrors.get(0).getDesc().equals("diffCoefficients"));
  //    }
  //
  //    @Test
  //    public void testCompareWidthConstraintsUnmatchedOracle() throws Exception {
  //        Node n1 = new Node("first");
  //        Node n2 = new Node("first");
  //        Node p = new Node("parent");
  //        WidthConstraint wc1 = new WidthConstraint(400,1000, 1, p, 0);
  ////        WidthConstraint wc2 = new WidthConstraint(390, 1000, 1, p, 0);
  //        n1.addWidthConstraint(wc1);
  ////        n2.addWidthConstraint(wc2);
  //
  //        comp.compareWidthConstraints(n1, n2);
  //        assertEquals(1, comp.wcErrors.size());
  //        assertEquals(true, comp.wcErrors.get(0).getDesc().equals("unmatched-oracle"));
  //    }

  //    @Test
  //    public void testCompareWidthConstraintsUnmatchedTest() throws Exception {
  //        Node n1 = new Node("first");
  //        Node n2 = new Node("first");
  //        Node p = new Node("parent");
  ////        WidthConstraint wc1 = new WidthConstraint(400,1000, 1, p, 0);
  //        WidthConstraint wc2 = new WidthConstraint(390, 1000, 1, p, 0);
  ////        n1.addWidthConstraint(wc1);
  //        n2.addWidthConstraint(wc2);
  //
  //        comp.compareWidthConstraints(n1, n2);
  //        assertEquals(1, comp.wcErrors.size());
  //        assertEquals(true, comp.wcErrors.get(0).getDesc().equals("unmatched-test"));
  //    }

  @Test
  public void testWriteRLGDiffToFile() {}

  @Test
  public void testIsErrorUnseenTrue() {
    Node n1 = new Node("test");
    Node n2 = new Node("test");
    Node p = new Node("parent");

    WidthConstraint wc1 = new WidthConstraint(400, 800, 1, p, 0);
    n1.addWidthConstraint(wc1);
    WidthConstraint wc2 = new WidthConstraint(400, 820, 1, p, 0);
    n2.addWidthConstraint(wc2);

    WidthError ve = new WidthError(wc1, wc2, "diffBounds", n1.getXpath());

    assertEquals(true, comp.isErrorUnseen(ve, comp.defaultWidths));
  }

  @Test
  public void testIsErrorUnseenFalse() {
    Node n1 = new Node("test");
    Node n2 = new Node("test");
    Node p = new Node("parent");
    comp.defaultWidths = new int[] {400, 640, 768, 1024};

    WidthConstraint wc1 = new WidthConstraint(400, 800, 1, p, 0);
    n1.addWidthConstraint(wc1);
    WidthConstraint wc2 = new WidthConstraint(400, 760, 1, p, 0);
    n2.addWidthConstraint(wc2);

    WidthError ve = new WidthError(wc1, wc2, "diffBounds", n1.getXpath());

    assertEquals(false, comp.isErrorUnseen(ve, comp.defaultWidths));
  }
}
