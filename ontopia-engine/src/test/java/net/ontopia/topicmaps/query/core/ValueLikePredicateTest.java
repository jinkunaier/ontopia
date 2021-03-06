/*
 * #!
 * Ontopia Engine
 * #-
 * Copyright (C) 2001 - 2013 The Ontopia Project
 * #-
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * !#
 */

package net.ontopia.topicmaps.query.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.ontopia.topicmaps.core.OccurrenceIF;
import net.ontopia.topicmaps.core.TopicIF;
import net.ontopia.utils.OntopiaRuntimeException;
import net.ontopia.utils.OntopiaUnsupportedException;

public class ValueLikePredicateTest extends AbstractPredicateTest {
  
  public ValueLikePredicateTest(String name) {
    super(name);
  }

  @Override
  public void tearDown() {
    closeStore();
  }

  /// tests
  public void testWithoutFulltextIndex() throws IOException, InvalidQueryException {
    load("int-occs.ltm", false);
    try {
      findNothing("value-like($foo, \"foo\")?");
      fail("Value-like on a topicmap without a fulltext index should fail, but didn't");
    } catch (OntopiaRuntimeException e) {
      if (!(e.getCause() instanceof OntopiaUnsupportedException)) {
        fail("Value-like on a topicmap without a fulltext index threw wrong exception");
      }
      // expected
    }
  }

  public void testWithSpecificOccurrenceAndString()
    throws InvalidQueryException, IOException {
    load("int-occs.ltm", true);

    TopicIF topic = getTopicById("topic1");
    OccurrenceIF oc = topic.getOccurrences().iterator().next();
    String oid = oc.getObjectId();

    // first argument must be unbound
    getParseError("value-like(@" + oid + ", \"topic1\")?");
  }

  public void testWithSpecificTopicAndVariable() throws InvalidQueryException, IOException {
    load("int-occs.ltm", true);
    
    // this predicate will fail because the first argument is of type TopicIF
    findNothing(OPT_TYPECHECK_OFF +
                "value-like(topic1, $VALUE)?");
  }

  public void testWithSpecificTopicAndString() throws InvalidQueryException, IOException {
    load("int-occs.ltm", true);
    
    // this predicate will fail because the first argument is of type TopicIF
    findNothing(OPT_TYPECHECK_OFF +
                "value-like(topic1, \"topic1\")?");
  }

  public void testWithUnboundBoth1() throws InvalidQueryException, IOException {
    load("int-occs.ltm", true);

    // query parser will complain because the second argument is unbound
    getParseError("value-like($FOO, $VALUE)?");
  }

  public void testWithUnboundBoth2() throws InvalidQueryException, IOException {
    load("int-occs.ltm", true);

    // this test will fail because the $TOPIC variable is of type TopicIF
    findNothing(OPT_TYPECHECK_OFF +
                "topic($TOPIC), value-like($TOPIC, $VALUE)?");
  }

  public void testWithUnboundPatternValue() throws InvalidQueryException, IOException {
    load("int-occs.ltm", true);

    TopicIF topic = getTopicById("topic1");
    OccurrenceIF oc = topic.getOccurrences().iterator().next();
    String oid = oc.getObjectId();
    
    getParseError("value-like(@" + oid + ", $VALUE)?");
  }
  
  public void testWithAnyObject() throws InvalidQueryException, IOException {
    load("family.ltm", true);

    List matches = new ArrayList();
    addMatch(matches, "TOPIC", getTopicById("lms"));
    addMatch(matches, "TOPIC", getTopicById("gerd"));
    addMatch(matches, "TOPIC", getTopicById("asle"));
    
    verifyQuery(matches, "select $TOPIC from " +
                         "  value-like($BNAME, \"skalle\"), " +
                         "  topic-name($TOPIC, $BNAME)?");
  }
  
  public void testWithScoreBound() throws InvalidQueryException, IOException {
    load("family.ltm", true);
    
    getParseError("select $TOPIC from " +
                  "  value-like($BNAME, \"skalle\", 0.54), " +
                  "  topic-name($TOPIC, $BNAME)?");
  }
  
  // filtering on score is possible but not advised as it is searcher dependent and
  // might not be percentage based (like with lucene > 3.x)
  public void testWithScoreAbove001() throws InvalidQueryException, IOException {
    load("family.ltm", true);

    List matches = new ArrayList();
    addMatch(matches, "TOPIC", getTopicById("lms"));
    addMatch(matches, "TOPIC", getTopicById("gerd"));
    addMatch(matches, "TOPIC", getTopicById("asle"));
    
    verifyQuery(matches, "select $TOPIC from " +
                         "  value-like($BNAME, \"skalle\", $SCORE), " +
                         "  topic-name($TOPIC, $BNAME), $SCORE > 0.01?");
  }
  
  public void testWithEscapedQuotes() throws InvalidQueryException, IOException {
    load("family.ltm", true);

    // WARNING: rdbms: this will cause a parse error with Oracle Text
    findNothing("select $TOPIC from " +
                "  value-like($BNAME, \"and \"\"ho\"\" ho\"), " +
                "  topic-name($TOPIC, $BNAME)?");
  }

  public void testWithSingleQuote() throws InvalidQueryException, IOException {
    load("family.ltm", true);
    
    findNothing("select $TOPIC from " +
                "  value-like($BNAME, \"foo'bar\"), " +
                "  topic-name($TOPIC, $BNAME)?");
  }

  // see bug #955
  public void testWithEmptyString() throws InvalidQueryException, IOException {
    load("family.ltm", true);

    findNothing("select $TOPIC from " +
                "  value-like($BNAME, \"\"), " +
                "  topic-name($TOPIC, $BNAME)?");
  }

  public void testBug987() throws InvalidQueryException, IOException {
    load("int-occs.ltm", true);

    List matches = new ArrayList();
    addMatch(matches, "TOPIC", getTopicById("topic1"));

    verifyQuery(matches,
                "select $TOPIC from " +
                "  type($OCC, description), " +
                "  occurrence($TOPIC, $OCC), " + 
                "  value-like($OCC, \"topic1\")?");
  }

  public void testIssue302() throws InvalidQueryException, IOException {
    load("int-occs.ltm", true);

    List matches = new ArrayList();
    addMatch(matches, "TOPIC", getTopicById("topic1"));

    verifyQuery(matches,
                "select $TOPIC from " +
                "  $query = \"topic1\", " +
                "  type($OCC, description), " +
                "  occurrence($TOPIC, $OCC), " + 
                "  value-like($OCC, $query)?");
  }
}
