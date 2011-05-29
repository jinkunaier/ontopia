
package net.ontopia.utils;

public class CompactIdentityHashSetTest extends CompactHashSetTest {
  
  public CompactIdentityHashSetTest(String name) {
    super(name);
  }

  public void setUp() {
    set = new CompactIdentityHashSet();
  }

  protected void tearDown() {
  }

  public void testProbabilistic() {
    // NOTE: This test won't work with the IdentityHashSet since it
    // uses pointer comparsion, and none of the generated objects are
    // ever the same.
  }
  
}
