/*
 * #!
 * Ontopia Navigator
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

package net.ontopia.topicmaps.nav2.taglibs.tolog;
import net.ontopia.topicmaps.query.core.QueryResultIF;

public interface BufferedQueryResultIF extends QueryResultIF {

  /** 
    * Bring this BufferedQueryResultIF back to the initial state after
    * it was created (instantiated to an implementing class).
    */
  void restart();
  
  /**
    * Get the query of the query result.
    */
  String getQuery();
}
