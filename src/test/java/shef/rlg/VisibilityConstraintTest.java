package shef.rlg;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;

/** Created by thomaswalsh on 18/09/15. */
public class VisibilityConstraintTest {
  VisibilityConstraint vc;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
    vc = new VisibilityConstraint(300, 0);
  }

  @Test
  public void testGetSetDisappear() {
    vc.setDisappear(500);
    assertEquals(vc.getDisappear(), 500);
  }

  @Test
  public void testToString() {
    vc.setDisappear(600);
    assertEquals(vc.toString(), "300 -> 600");
  }
}
