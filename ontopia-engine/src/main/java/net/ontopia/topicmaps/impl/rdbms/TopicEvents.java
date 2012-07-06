
package net.ontopia.topicmaps.impl.rdbms;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.ontopia.topicmaps.core.TMObjectIF;
import net.ontopia.topicmaps.core.TopicIF;
import net.ontopia.topicmaps.core.events.TopicMapListenerIF;
import net.ontopia.topicmaps.impl.utils.EventListenerIF;
import net.ontopia.topicmaps.impl.utils.EventManagerIF;
import net.ontopia.topicmaps.impl.utils.SnapshotTMObject;
import net.ontopia.topicmaps.impl.utils.SnapshotTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * INTERNAL: Internal event listener class that handles topic events.<p>
 */
public class TopicEvents implements EventListenerIF {
  
  // Define a logging category.
  static Logger log = LoggerFactory.getLogger(TopicEvents.class.getName());

  protected  RDBMSTopicMapStore store;
  
  protected Set<TopicIF> topicsAdded = new HashSet<TopicIF>();
  protected Set<TopicIF> topicsModified = new HashSet<TopicIF>(); 
  protected Map<TopicIF, TopicIF> topicsRemoved = new HashMap<TopicIF, TopicIF>();

  public TopicEvents(RDBMSTopicMapStore store) {
    this.store = store;
  }
  
  // -----------------------------------------------------------------------------
  // Transaction callbacks
  // -----------------------------------------------------------------------------
  
  protected void commitListeners() {
    if (store.topic_listeners != null) {
      TopicMapListenerIF[] topic_listeners = store.topic_listeners;
      for (TopicIF added : topicsAdded) {
        for (int i=0; i < topic_listeners.length; i++) {
          try {
            topic_listeners[i].objectAdded(SnapshotTopic.makeSnapshot(added, SnapshotTMObject.SNAPSHOT_REFERENCE, new HashMap<TMObjectIF, SnapshotTMObject>()));
          } catch (Exception e) {
            log.error("Exception was thrown from topic map listener " + topic_listeners[i], e);
          }
        }
      }
      for (TopicIF modified : topicsModified) {
        if (topicsAdded.contains(modified) || topicsRemoved.containsKey(modified)) continue;
        for (int i=0; i < topic_listeners.length; i++) {
          try {
            topic_listeners[i].objectModified(SnapshotTopic.makeSnapshot(modified, SnapshotTMObject.SNAPSHOT_REFERENCE, new HashMap<TMObjectIF, SnapshotTMObject>()));
          } catch (Exception e) {
            log.error("Exception was thrown from topic map listener " + topic_listeners[i], e);
          }
        }
      }
      for (TopicIF removed : topicsRemoved.keySet()) {
        if (!topicsAdded.contains(removed)) {
          for (int i=0; i < topic_listeners.length; i++) {
            try {
              topic_listeners[i].objectRemoved(topicsRemoved.get(removed));
            } catch (Exception e) {
              log.error("Exception was thrown from topic map listener " + topic_listeners[i], e);
            }
          }
        }
      }
      topicsAdded.clear();
      topicsModified.clear();
      topicsRemoved.clear();
    }
  }

  protected void abortListeners() {
    if (store.topic_listeners != null) {
      topicsAdded.clear();
      topicsRemoved.clear();
    }
  }
  
  // -----------------------------------------------------------------------------
  // Callbacks
  // -----------------------------------------------------------------------------
  
  // called when topic has just been added to the topic map
  void addedTopic(TopicIF topic) {
    if (store.topic_listeners != null) {
      topicsAdded.add(topic);
    }
  }

  // called when a topic is about to be removed from the topic map
  void removingTopic(TopicIF topic) {
    if (store.topic_listeners != null) {
      topicsAdded.remove(topic);
      if (!topicsRemoved.containsKey(topic))
        topicsRemoved.put(topic, SnapshotTopic.makeSnapshot(topic, SnapshotTMObject.SNAPSHOT_COMPLETE, new HashMap<TMObjectIF, SnapshotTMObject>()));
    }
  }
  
  // -----------------------------------------------------------------------------
  // EventListenerIF
  // -----------------------------------------------------------------------------

  void registerListeners(EventManagerIF emanager) {
    // listen to topic modification events
    emanager.addListener(this, "TopicIF.modified");
    emanager.addListener(this, "TopicMapTransactionIF.commit");
    emanager.addListener(this, "TopicMapTransactionIF.abort");
  }
  
  public void processEvent(Object object, String event, Object new_value, Object old_value) {
    if (store.topic_listeners != null && "TopicIF.modified".equals(event)) {
      topicsModified.add((TopicIF)object);
    }
    if ("TopicMapTransactionIF.commit".equals(event)) {
      commitListeners();
    }
    if ("TopicMapTransactionIF.abort".equals(event)) {
      abortListeners();
    }
  }
  
}
