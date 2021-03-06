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

package net.ontopia.topicmaps.webed.impl.actions.tmobject;

import net.ontopia.topicmaps.core.ScopedIF;
import net.ontopia.topicmaps.core.TopicIF;
import net.ontopia.topicmaps.webed.core.ActionIF;
import net.ontopia.topicmaps.webed.core.ActionParametersIF;
import net.ontopia.topicmaps.webed.core.ActionResponseIF;
import net.ontopia.topicmaps.webed.core.ActionRuntimeException;
import net.ontopia.topicmaps.webed.impl.utils.ActionSignature;

/**
 * PUBLIC: Action for adding a theme to a scoped object.
 */
public class AddTheme implements ActionIF {
  
  @Override
  public void perform(ActionParametersIF params, ActionResponseIF response) {

    //test params
    ActionSignature paramsType = ActionSignature.getSignature("baov");
    paramsType.validateArguments(params, this);

    ScopedIF scoped = (ScopedIF) params.get(0);
    TopicIF theme = (TopicIF) params.getTMObjectValue();
    if (theme == null)
      throw new ActionRuntimeException("No topic ID given to AddTheme action");
    
    scoped.addTheme(theme);

  }
  
}
