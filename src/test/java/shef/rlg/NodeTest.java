package shef.rlg;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

/** Created by thomaswalsh on 18/09/15. */
public class NodeTest {

  Node n;

  @Before
  public void setup() {
    n = new Node("testXPath");
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testAddWidthConstraint() {
    WidthConstraint wc = mock(WidthConstraint.class);
    n.addWidthConstraint(wc);
    assertEquals(n.getWidthConstraints().size(), 1);
  }

  @Test
  public void testAddVisibilityConstraint() {
    VisibilityConstraint vc = mock(VisibilityConstraint.class);
    n.addVisibilityConstraint(vc);
    assertEquals(n.getVisibilityConstraints().size(), 1);
  }

  @Test
  public void testAddParentConstraint() {
    AlignmentConstraint ac = mock(AlignmentConstraint.class);
    n.addParentConstraint(ac);
    assertEquals(n.getParentConstraints().size(), 1);
  }

  @Test
  public void testToString() {
    VisibilityConstraint vc = new VisibilityConstraint(300, 600);
    n.addVisibilityConstraint(vc);

    Node n2 = new Node("testParent");
    WidthConstraint wc = new WidthConstraint(300, 800, 1, n2, -50);
    n.addWidthConstraint(wc);

    String expected = "testXPath\n\tVisibility: 300 -> 600";
    assertEquals(n.toString(), expected);
  }

  //    @Test
  //    public void testGraphVizLabel() {
  //        VisibilityConstraint vc = new VisibilityConstraint(300,600);
  //        n.addVisibilityConstraint(vc);
  //
  //        Node n2 = new Node("testParent");
  //        WidthConstraint wc = new WidthConstraint(300,800, 1, n2, -50);
  //        n.addWidthConstraint(wc);
  //
  //        String expected = "300 -> 600\ntestXPath\n300 -> 800 : 100.0% of parent + -50.0";
  //        assertEquals(n.generateGraphVizLabel(), expected);
  //    }
}
