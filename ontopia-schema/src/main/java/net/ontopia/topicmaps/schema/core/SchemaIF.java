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

package net.ontopia.topicmaps.schema.core;

import net.ontopia.infoset.core.LocatorIF;

/**
 * PUBLIC: This interface is implemented by objects that represent
 * complete topic map schemas.
 */
public interface SchemaIF {

  /**
   * PUBLIC: Returns a validator object that can be used to validate
   * topic map objects against the schema.
   */
  public SchemaValidatorIF getValidator();

  /**
   * PUBLIC: Returns the address of the schema. The address may be
   * null.
   */
  public LocatorIF getAddress();
  
}





