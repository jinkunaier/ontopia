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
import net.ontopia.topicmaps.core.AssociationIF;
import net.ontopia.topicmaps.core.TopicIF;
import net.ontopia.topicmaps.webed.core.ActionIF;
import net.ontopia.topicmaps.webed.core.ActionParametersIF;
import net.ontopia.topicmaps.webed.core.ActionResponseIF;
import net.ontopia.topicmaps.webed.core.ActionRuntimeException;
import net.ontopia.topicmaps.webed.impl.actions.topicmap.CreateAssoc;
import net.ontopia.topicmaps.webed.impl.basic.Constants;
    
public class TestCreateAssoc extends AbstractWebedTestCase {

    public TestCreateAssoc(String name) {
	super(name);
    }


    // tests

    /*
      1. No parameters
      2. No topicmap input, StringType all params
      3. Empty string as type
      4. 
    */

    
    //good cases
  public void testTM() throws java.io.IOException, ActionRuntimeException {
    //Good TM.
    
    //make action
    ActionIF action = new CreateAssoc();
    //build parms
    ActionParametersIF params = makeParameters(makeList(tm));
    ActionResponseIF response = makeResponse();
    //execute
    
    action.perform(params, response);      

    // verify that an association was created correctly
    String id = response.getParameter(Constants.RP_ASSOC_ID);
    assertFalse("id of association not recorded in response parameters", id == null);
      
    AssociationIF assoc = (AssociationIF) tm.getObjectById(id);
    assertFalse("created association not found", assoc == null);
    assertFalse("created association in wrong TM", assoc.getTopicMap() != tm);
    assertFalse("created association has scope", !(assoc.getScope().isEmpty()) );
    assertFalse("created association has roles", !(assoc.getRoles().isEmpty()) );
  }
  
  public void testTMGoodPlayer() throws java.io.IOException, ActionRuntimeException {
    //Good TM good topic.
    
    TopicIF topicDummy = makeTopic(tm, "snus");
    //make action
    ActionIF action = new CreateAssoc();
    //build parms
    ActionParametersIF params = makeParameters(makeList(tm, topicDummy));
    ActionResponseIF response = makeResponse();
    //execute
    
    action.perform(params, response);
      
    // verify that an association was created correctly
    String id = response.getParameter(Constants.RP_ASSOC_ID);
    assertFalse("id of association not recorded in response parameters", id == null);
      
    AssociationIF assoc = (AssociationIF) tm.getObjectById(id);
    assertFalse("created association not found", assoc == null);
    assertFalse("created association in wrong TM", assoc.getTopicMap() != tm);
      
    assertFalse("created association has no type, it should have", 
                assoc.getType() == null);
    assertFalse("created association has scope", !(assoc.getScope().isEmpty()) );
    assertFalse("created association has roles", !(assoc.getRoles().isEmpty()) );
  }
  
  //bad cases
  
  public void testNoParameters() throws ActionRuntimeException {
    //make action
    ActionIF assoc = new CreateAssoc();
    //build parms
    ActionParametersIF params = makeParameters(Collections.EMPTY_LIST);
    ActionResponseIF response = makeResponse();
    //execute
    
    try {
      assoc.perform(params, response);      
      fail("Made assoc without TM, or any other parameters");
    } catch (ActionRuntimeException e) {
    }
    
  }
  
  public void testNoTopicmap() throws ActionRuntimeException {
    //make action
    ActionIF assoc = new CreateAssoc();
    
    //build parms
    ActionParametersIF params = makeParameters(makeList("topicmap", "type", "mama"));
    ActionResponseIF response = makeResponse();
    
    try {
      assoc.perform(params, response);      
      fail("Made assoc with TM as string, and other strings");
    } catch (ActionRuntimeException e) {
      
    }
  }
  
  
  public void testTMNullTopicAssoc() throws java.io.IOException{
    //Good TM but no excisiting topic.
    
    //make action
    ActionIF assoc = new CreateAssoc();
    //build parms
    ActionParametersIF params = makeParameters(makeList(tm, ""));
    ActionResponseIF response = makeResponse();
    //execute
    
    try {
      assoc.perform(params, response);      
      fail("Made assoc with TM, but empty string as topic assosiationtype");
    } catch (ActionRuntimeException e) {
      
    }
    
  }
  
  
}
