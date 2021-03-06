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

package net.ontopia.utils;

import junit.framework.TestCase;

public class CachedIndexTest extends TestCase {
  private CachedIndex index;
  
  public CachedIndexTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() {
    index = new CachedIndex(new EmptyIndex());
  }

  // --- Test cases

  public void testEmpty() {
    assertTrue("found key in empty index",
           index.get("larsga") == null);
  }

  public void testFind() {
    check("larsga", "Lars Marius Garshol");
  }
  
  public void testFindMore() {
    check("larsga", "Lars Marius Garshol");
    check("grove", "Geir Ove Gronmo");
    check("tine", "Tine Holst");
    check("sylvias", "Sylvia Schwab");
    check("pepper", "Steve Pepper");
    check("hca", "Hans Christian Alsos");
    check("niko", "Niko Schmuck");
    check("pam", "Pamela Gennusa");
    check("kal", "Kal Ahmed");
    check("murray", "Murray Woodman");
    
    lookfor("larsga", "Lars Marius Garshol");
    lookfor("grove", "Geir Ove Gronmo");
    lookfor("tine", "Tine Holst");
    lookfor("sylvias", "Sylvia Schwab");
    lookfor("pepper", "Steve Pepper");
    lookfor("hca", "Hans Christian Alsos");
    lookfor("niko", "Niko Schmuck");
    lookfor("pam", "Pamela Gennusa");
    lookfor("kal", "Kal Ahmed");
    lookfor("murray", "Murray Woodman");

    assertTrue("non-existent key found",
           index.get("dummy") == null);
  }

  public void testExpand() {
    index = new CachedIndex(new EmptyIndex(), 1000, 5, true);
    
    check("larsga", "Lars Marius Garshol");
    check("grove", "Geir Ove Gronmo");
    check("tine", "Tine Holst");
    check("sylvias", "Sylvia Schwab");
    check("pepper", "Steve Pepper");
    check("hca", "Hans Christian Alsos");
    check("niko", "Niko Schmuck");
    check("pam", "Pamela Gennusa");
    check("kal", "Kal Ahmed");
    check("murray", "Murray Woodman");

    lookfor("larsga", "Lars Marius Garshol");
    lookfor("grove", "Geir Ove Gronmo");
    lookfor("tine", "Tine Holst");
    lookfor("sylvias", "Sylvia Schwab");
    lookfor("pepper", "Steve Pepper");
    lookfor("hca", "Hans Christian Alsos");
    lookfor("niko", "Niko Schmuck");
    lookfor("pam", "Pamela Gennusa");
    lookfor("kal", "Kal Ahmed");
    lookfor("murray", "Murray Woodman");

    assertTrue("non-existent key found",
           index.get("dummy") == null);
  }

  public void testPrune() {
    index = new CachedIndex(new SameIndex(), 250, 5, true);

    for (int ix = 0; ix < 10000; ix++) {
      String key = Integer.toString((int) (Math.random() * 500));
      assertTrue("didn't find value",
             index.get(key).equals(key));
    }

    assertTrue("number of keys in index too high",
           index.getKeyNumber() <= 250);
  }

  public void testChange() {
    check("larsga", "Lars Marius Garshol");
    check("larsga", "LMG");
    lookfor("larsga", "LMG");
    check("larsga", "Lars Marius Garshol");
    lookfor("larsga", "Lars Marius Garshol");
  }
  
  // --- Helper methods

  private void check(String key, String value) {
    index.put(key, value);
    lookfor(key, value);
  }

  private void lookfor(String key, String value) {
    String found = (String) index.get(key);
    assertTrue("did not find value on lookup",
           found != null);
    assertTrue("found '" + found + "' on lookup, expected '" + value + "'",
           found.equals(value));
  }
  
  // --- SameIndex

  class SameIndex implements LookupIndexIF {
    @Override
    public Object get(Object key) {
      return key;
    }

    @Override
    public Object put(Object key, Object value) {
      return value;
    }

    @Override
    public Object remove(Object key) {
      return key;
    }
  }
  
  // --- EmptyIndex

  class EmptyIndex implements LookupIndexIF {
    @Override
    public Object get(Object key) {
      return null;
    }

    @Override
    public Object put(Object key, Object value) {
      return value;
    }

    @Override
    public Object remove(Object key) {
      return null;
    }
  }
  
}




