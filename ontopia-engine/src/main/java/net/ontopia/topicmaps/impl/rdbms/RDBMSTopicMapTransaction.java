
package net.ontopia.topicmaps.impl.rdbms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;

import net.ontopia.infoset.core.LocatorIF;
import net.ontopia.persistence.proxy.IdentityIF;
import net.ontopia.persistence.proxy.PersistentIF;
import net.ontopia.persistence.proxy.TransactionIF;
import net.ontopia.topicmaps.core.TMObjectIF;
import net.ontopia.topicmaps.core.TopicIF;
import net.ontopia.topicmaps.core.TopicMapIF;
import net.ontopia.topicmaps.impl.utils.TopicMapTransactionIF;
import net.ontopia.topicmaps.core.TransactionNotActiveException;
import net.ontopia.topicmaps.core.ReadOnlyException;
import net.ontopia.topicmaps.core.events.TopicMapListenerIF;
import net.ontopia.topicmaps.impl.rdbms.index.IndexManager;
import net.ontopia.topicmaps.impl.utils.AbstractTopicMapTransaction;
import net.ontopia.topicmaps.impl.utils.EventListenerIF;
import net.ontopia.topicmaps.impl.utils.EventManagerIF;
import net.ontopia.topicmaps.impl.utils.ObjectTreeManager;
import net.ontopia.topicmaps.impl.utils.TopicModificationManager;
import net.ontopia.topicmaps.impl.utils.SnapshotTopic;
import net.ontopia.topicmaps.impl.utils.SnapshotTMObject;
import net.ontopia.utils.CollectionFactory;
import net.ontopia.utils.OntopiaRuntimeException;
import net.ontopia.utils.OntopiaUnsupportedException;

/**
 * INTERNAL: The rdbms topic map transaction implementation.<p>
 */

public class RDBMSTopicMapTransaction extends AbstractTopicMapTransaction
  implements EventManagerIF {
  protected TransactionIF txn;
  protected boolean readonly; 

  protected long actual_id;

  protected SubjectIdentityCache sicache;
  protected RoleTypeCache rtcache;
  protected RoleTypeAssocTypeCache rtatcache;
  protected Map listeners;  

  protected ObjectTreeManager otree;
  protected TopicModificationManager topicmods;
  TopicEvents te;
  
  RDBMSTopicMapTransaction(RDBMSTopicMapStore store, long topicmap_id, boolean readonly) {
    this.store = store;
    this.readonly = readonly;

    // Begin new transaction
    this.txn = store.getStorage().createTransaction(readonly);
    this.txn.begin();

    // Create or look up topic map object
    if (topicmap_id > 0) {
      // Get hold of topic map object
      topicmap = (TopicMapIF)txn.getObject(txn.getAccessRegistrar().createIdentity(TopicMap.class, topicmap_id));
      if (topicmap == null) throw new OntopiaRuntimeException("Topic map with id '" + topicmap_id + " not found.");
    }
    else {
      if (readonly)
        throw new ReadOnlyException();
      else {
        // Create new topic map object and register with database
        topicmap = new TopicMap(txn);
        txn.create((PersistentIF)topicmap);
      }
    }
    
    IdentityIF identity = ((PersistentIF)topicmap)._p_getIdentity();
    this.actual_id = ((Long)(identity.getKey(0))).longValue();
    
    // Register store with topic map
    if (readonly)
      ((ReadOnlyTopicMap)topicmap).setTransaction(this);
    else
      ((TopicMap)topicmap).setTransaction(this);
    
    // Activate transaction (note: must be activated at this point, because of dependencies)
    this.active = true;    
    
    // Initialize collection factory
    this.cfactory = new CollectionFactory();

    // Initialize listeners
    this.listeners = cfactory.makeSmallMap();

    // Register object tree event listener with store event manager
    this.otree = new ObjectTreeManager(this, cfactory);
    this.topicmods = new TopicModificationManager(this, cfactory);
    this.te = new TopicEvents(store);
    this.te.registerListeners(this);
    this.topicmods.addListener(this.te, "TopicIF.modified");
    
    // QueryCache: subject identity cache
    this.sicache = new SubjectIdentityCache(this, cfactory);
    this.sicache.registerListeners(this, otree);

    // QueryCache: role type cache
    this.rtcache = new RoleTypeCache(this, this, otree);

    // QueryCache: role type assoc type cache
    this.rtatcache = new RoleTypeAssocTypeCache(this, this, otree);

    // Create new index manager
    this.imanager = new IndexManager(this, cfactory);

    // Create topic map builder
    this.builder = new TopicMapBuilder(txn, topicmap);
  }

  public long getActualId() {
    return actual_id;
  }

  public ObjectTreeManager getObjectTreeManager() {
    return otree;
  }
  
  public void commit() {
    if (!readonly) {
      synchronized (this) {
        super.commit();
        
        // commit proxy transaction
        txn.commit();
        
        // reset query caches
        sicache.commit();
        rtcache.commit();
        rtatcache.commit();

        // notify cluster
        txn.getStorageAccess().getStorage().notifyCluster();
        
        // commmit listeners
        processEvent(this, "TopicMapTransactionIF.commit", null, null);
      }
    } else {
      txn.commit();
    }
  }

  public void abort() {
    if (!readonly) {
      synchronized (this) {
        super.abort();

        // abort listeners
        processEvent(this, "TopicMapTransactionIF.abort", null, null);
      }
    }
  }

  public void abort(boolean invalidate) {
    if (!readonly || invalidate) {
      synchronized (this) {
        super.abort(invalidate);
        
        // Invalidate transaction
        invalid = (invalid || invalidate);
        
        // Abort proxy transaction
        if (txn.isActive()) txn.abort();
        
        if (invalidate) {
          if (active) {
            // Close proxy transaction
            txn.close();
            
            // Deactivate topic map transaction
            active = false;
          }
        } else {
          // Reset query caches
          sicache.abort();
          rtcache.abort();
          rtatcache.abort();
        }
      }
    } else if (readonly) {
      txn.abort();
    }
  }

  public boolean validate() {
    // if transaction has been aborted the store is invalid
    if (invalid) return false;
    // check proxy transaction
    return txn.validate();    
  }

  public TopicMapTransactionIF createNested() {
    // Nested transactions are not supported
    throw new OntopiaUnsupportedException("Nested transactions not supported.");
  }

  /**
   * INTERNAL: Returns the proxy transaction used by the topic map
   * transaction.
   */
  public TransactionIF getTransaction() {
    if (!isActive()) throw new TransactionNotActiveException("Transaction is not active.");
    return txn;
  }
  
  // ---------------------------------------------------------------------------
  // EventManagerIF implementation
  // ---------------------------------------------------------------------------
  
  public void addListener(EventListenerIF listener, String event) {
    // Adding itself causes infinite loops.
    if (listener == this) return;
    // Initialize event entry
    synchronized (listeners) {
      // Add listener to list of event entry listeners. This is not
      // very elegant, but it works.
      if (!listeners.containsKey(event))
        listeners.put(event, new Object[0]);
      Collection event_listeners = new ArrayList(Arrays.asList((Object[])listeners.get(event)));
      event_listeners.add(listener);
      listeners.put(event, event_listeners.toArray());      
    }
  }

  public void removeListener(EventListenerIF listener, String event) {
    synchronized (listeners) {
      if (listeners.containsKey(event)) {
        // Remove listener from list of event entry listeners. This is
        // not very elegant, but it works.
        Collection event_listeners = new ArrayList(Arrays.asList((Object[])listeners.get(event)));
        event_listeners.remove(listener);
        if (event_listeners.isEmpty())
          listeners.remove(event);
        else
          listeners.put(event, event_listeners.toArray());
      }
    }
  }

  public void processEvent(Object object, String event, Object new_value, Object old_value) {
    // Look up event listeners
    Object[] event_listeners = (Object[])listeners.get(event);
    if (event_listeners != null) {
      // Loop over event listeners
      int size = event_listeners.length;
      for (int i=0; i < size; i++)
        // Notify listener
        ((EventListenerIF)event_listeners[i]).processEvent(object, event, new_value, old_value);
    }
  }

  // ---------------------------------------------------------------------------
  // Prefetch: roles by type and association type
  // ---------------------------------------------------------------------------

  public void prefetchRolesByType(Collection players, 
                                  TopicIF rtype, TopicIF atype) {
    this.rtatcache.prefetchRolesByType(players, rtype, atype);
  }

  // ---------------------------------------------------------------------------
  // Subject identity cache
  // ---------------------------------------------------------------------------

  public TMObjectIF getObjectByItemIdentifier(LocatorIF locator) {
    if (locator == null) throw new NullPointerException("null is not a valid argument.");

    // Get from subject identity cache
    return sicache.getObjectByItemIdentifier(locator);
  }

  public TopicIF getTopicBySubjectLocator(LocatorIF locator) {
    if (locator == null) throw new NullPointerException("null is not a valid argument.");
          
    // Get from subject identity cache
    return sicache.getTopicBySubjectLocator(locator);
  }
  
  public TopicIF getTopicBySubjectIdentifier(LocatorIF locator) {
    if (locator == null) throw new NullPointerException("null is not a valid argument.");
          
    // Get from subject identity cache
    return sicache.getTopicBySubjectIdentifier(locator);
  }

  // ---------------------------------------------------------------------------
  // Role type cache
  // ---------------------------------------------------------------------------

  public Collection getRolesByType(TopicIF player, TopicIF rtype) {
    return rtcache.getRolesByType(player, rtype);
  }

  // ---------------------------------------------------------------------------
  // Role type and association type cache
  // ---------------------------------------------------------------------------

  public Collection getRolesByType(TopicIF player, TopicIF rtype, TopicIF atype) {
    return rtatcache.getRolesByType(player, rtype, atype);
  }

  public String toString() {
    return "[rdbms.Transaction, " + actual_id + ", " + readonly + "]";
  }

}
