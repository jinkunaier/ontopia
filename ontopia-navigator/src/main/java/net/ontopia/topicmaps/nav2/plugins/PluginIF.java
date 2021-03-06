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

package net.ontopia.topicmaps.nav2.plugins;

import java.util.List;
import net.ontopia.topicmaps.nav2.taglibs.logic.ContextTag;

/** 
 * INTERNAL: The common interface for all navigator plugin objects.
 * This interface can either be implemented by the plugin, or a
 * default implementation (DefaultPlugin) can be used. 
 */
public interface PluginIF {

  int ACTIVATED = 0;
  int DEACTIVATED = 1;
  int ERROR = 2;
  
  /**
   * INTERNAL: Called by the framework to make the plugin produce the
   * HTML that is going to represent it on a web page in the web
   * application.
   *
   * @return An HTML string to be written into the page. If the returned
   *         string is null it means that the plugin does not wish to be
   *         displayed on this page.
   */
  String generateHTML(ContextTag context);
  
  /**
   * INTERNAL: Called by the framework to finalize initialization.
   * Called when there are no more parameters.
   */
  void init();

  
  // ----------------------------------------------------------
  // Accessor methods
  // ----------------------------------------------------------
  
  /**
   * INTERNAL: Returns the ID of this plugin.
   */
  String getId();

  /**
   * INTERNAL: Sets the ID of this plugin.
   */
  void setId(String id);

  /**
   * INTERNAL: Returns the groups this plugin belongs to.  Each group is
   * represented by a string containing the group id.
   */
  List getGroups();

  /**
   * INTERNAL: Reset all group settings for this plugin. After this
   * operation this plugin will belong to no group.
   */
  void resetGroups();

  /**
   * INTERNAL: Add the specified group to groups this plugin belongs to.
   */
  void addGroup(String groupId);
    
  /**
   * INTERNAL: Sets the groups this plugin belongs to.
   */
  void setGroups(List groups);

  /**
   * INTERNAL: Returns the title of this plugin.
   */
  String getTitle();

  /**
   * INTERNAL: Sets the title of this plugin.
   */
  void setTitle(String title);

  /**
   * INTERNAL: Gets the description of this plugin.
   */
  String getDescription();

  /**
   * INTERNAL: Sets the description of this plugin.
   */
  void setDescription(String description);

  /**
   * INTERNAL: Returns the URI of this plugin.
   */
  String getURI();

  /**
   * INTERNAL: Sets the URI of this plugin.
   * <p>
   * Note: This has not to contain the web application
   * context path.
   * <p>
   * Example: <code>plugins/hello/hello.jsp</code>
   */
  void setURI(String uri);

  /**
   * INTERNAL: Returns the URI frame target of this plugin.
   */
  String getTarget();

  /**
   * INTERNAL: Sets the URI frame target of this plugin.
   */
  void setTarget(String target);

  /**
   * INTERNAL: Returns the state of this plugin.
   */
  int getState();

  /**
   * INTERNAL: Sets the state of this plugin.
   */
  void setState(int state);
  
  /**
   * INTERNAL: Returns the value of the parameter.
   */
  String getParameter(String name);
  
  /**
   * INTERNAL: Called by the framework to give the plugin the value of a
   * configuration parameter.
   */
  void setParameter(String name, String value);

  /**
   * INTERNAL: Returns the path to the plugin directory. This is the
   * path in the file system the server is running in, if the web
   * application is deployed as an unexploded WAR.
   */
  String getPluginDirectory();
 
  /**
   * INTERNAL: Called by the framework to give the plugin the directory
   * its plugin.xml file was found in. This is useful for plugins
   * which wish to use other files found in the same directory.
   */
  void setPluginDirectory(String path);
  
}
