/*
 * #!
 * Ontopia Webed
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

package net.ontopia.topicmaps.webed.impl.actions;

import java.util.Collections;
import java.util.Iterator;
import net.ontopia.infoset.core.LocatorIF;
import net.ontopia.topicmaps.core.OccurrenceIF;
import net.ontopia.topicmaps.core.TopicIF;
import net.ontopia.topicmaps.core.TopicMapIF;
import net.ontopia.topicmaps.webed.core.ActionIF;
import net.ontopia.topicmaps.webed.core.ActionParametersIF;
import net.ontopia.topicmaps.webed.core.ActionResponseIF;
import net.ontopia.topicmaps.webed.core.ActionRuntimeException;
import net.ontopia.topicmaps.webed.impl.actions.occurrence.LastModifiedAt;

public class TestLastModifiedAt extends AbstractWebedTestCase {
  
  public TestLastModifiedAt(String name) {
    super(name);
  }

  
  /*
    1. Good, Normal use
    2. Good, Normal use, with scope
    
  */
  
  public void testNormalOperation() throws java.io.IOException{
    
    TopicIF topic = getTopicById(tm, "gamst");
    TopicMapIF topicmap = topic.getTopicMap();
    int numO = topic.getOccurrences().size();

    //make action
    ActionIF action = new LastModifiedAt();
    
    //build parms
    ActionParametersIF params = makeParameters(topic, "");
    ActionResponseIF response = makeResponse();
    
    //execute    
    action.perform(params, response);
    
    //test          
    assertFalse("New occurrence not added", topic.getOccurrences().size() == numO);
    
    Iterator<OccurrenceIF> occIT = topic.getOccurrences().iterator();
    boolean hasit = false;
    while (occIT.hasNext()){
      OccurrenceIF occ = occIT.next();   
      LocatorIF loc = occ.getType().getSubjectIdentifiers().iterator().next();
      if (loc.getAddress().equals("http://psi.ontopia.net/xtm/occurrence-type/last-modified-at"))
	hasit = true;
    }
    assertFalse("The Occurrence is not correct", !(hasit));
    
  }
  
  public void testNoParam() throws java.io.IOException{
        
    //make action
    ActionIF action = new LastModifiedAt();
    
    //build parms
    ActionParametersIF params = makeParameters(Collections.EMPTY_LIST);
    ActionResponseIF response = makeResponse();
    try{
      //execute    
      action.perform(params, response);
      //test      
      fail("Collection is empty");
    }catch (ActionRuntimeException e){
    }
  }

  
}
