
package net.ontopia.topicmaps.query.impl.rdbms;

import java.io.IOException;

public class TypePredicateTest
  extends net.ontopia.topicmaps.query.core.TypePredicateTest {
  
  public TypePredicateTest(String name) {
    super(name);
  }
  
  protected void load(String filename) throws IOException {
    RDBMSTestUtils.load(this, filename);
  }
  
  protected void makeEmpty() {
    RDBMSTestUtils.makeEmpty(this);
  }
  
}
