
// $Id: FulltextIndexManager.java,v 1.9 2008/06/12 14:37:16 geir.gronmo Exp $

package net.ontopia.topicmaps.impl.utils;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import net.ontopia.infoset.fulltext.core.IndexerIF;
import net.ontopia.infoset.fulltext.core.SearchResultIF;
import net.ontopia.infoset.fulltext.core.SearcherIF;
import net.ontopia.infoset.fulltext.impl.lucene.LuceneSearcher;
import net.ontopia.infoset.fulltext.topicmaps.DefaultTopicMapDocumentGenerator;
import net.ontopia.infoset.fulltext.topicmaps.TopicMapDocumentGeneratorIF;
import net.ontopia.topicmaps.core.TopicNameIF;
import net.ontopia.topicmaps.core.OccurrenceIF;
import net.ontopia.topicmaps.core.TMObjectIF;
import net.ontopia.topicmaps.core.TopicMapIF;
import net.ontopia.topicmaps.core.VariantNameIF;
import net.ontopia.topicmaps.impl.basic.InMemoryTopicMapStore;
import net.ontopia.topicmaps.impl.basic.InMemoryTopicMapTransaction;
import net.ontopia.utils.OntopiaRuntimeException;

import org.apache.lucene.store.Directory;

/**
 * INTERNAL: The indexer manager will keep track of base names,
 * variants and occurrences that are changed in the topic map. These
 * fulltext index can later be synchronized through the
 * synchronizeIndex method.
 */
public class FulltextIndexManager extends BasicIndex implements SearcherIF {

  protected Directory luceneDirectory;
  protected SearcherIF luceneSearcher;
  
  protected Collection<TMObjectIF> added;

  protected Collection<String> removed;

  protected Collection<TMObjectIF> changed;

  protected TopicMapDocumentGeneratorIF docgen = DefaultTopicMapDocumentGenerator.INSTANCE;

  protected FulltextIndexManager(TopicMapIF topicmap) {

    EventManagerIF emanager = (EventManagerIF) topicmap;
		InMemoryTopicMapStore store = (InMemoryTopicMapStore)topicmap.getStore();
    ObjectTreeManager otree = ((InMemoryTopicMapTransaction) store
        .getTransaction()).getObjectTreeManager();

    // Initialize index maps
    added = new HashSet<TMObjectIF>();
    removed = new HashSet<String>();
    changed = new HashSet<TMObjectIF>();

    // Initialize object tree event handlers [objects added or removed]
    EventListenerIF listener_add = new TMObjectIF_added();
    EventListenerIF listener_rem = new TMObjectIF_removed();

    otree.addListener(listener_add, "TopicNameIF.added");
    otree.addListener(listener_rem, "TopicNameIF.removed");

    otree.addListener(listener_add, "VariantNameIF.added");
    otree.addListener(listener_rem, "VariantNameIF.removed");

    otree.addListener(listener_add, "OccurrenceIF.added");
    otree.addListener(listener_rem, "OccurrenceIF.removed");

    // Initialize object property event handlers
    EventListenerIF listener_chg = new TMObjectIF_changed();
    
    handlers.put("TopicNameIF.setValue", listener_chg);    
    handlers.put("VariantNameIF.setValue", listener_chg);
    handlers.put("OccurrenceIF.setValue", listener_chg);

    // Register dynamic index as event listener
    Iterator<String> iter = handlers.keySet().iterator();
    while (iter.hasNext())
      emanager.addListener(this, iter.next());

    // register ourselves as index
    AbstractIndexManager ixm = (AbstractIndexManager) store.getTransaction().getIndexManager();
    ixm.registerIndex("net.ontopia.infoset.fulltext.core.SearcherIF", this);
  }

  /**
   * INTERNAL: Registers the fulltext index manager with the event system of the
   * specified topic map.
   * 
   * @param topicmap
   * @return
   */
  public static FulltextIndexManager manageTopicMap(TopicMapIF topicmap) {
    return new FulltextIndexManager(topicmap);
  }

  public void setLuceneDirectory(Directory luceneDirectory) {
      this.luceneDirectory = luceneDirectory;
  }

  /**
   * INTERNAL: Returns true if index manager has seen changes to the topic map,
   * so that the index must be updated.
   * 
   * @return True if index must be updated.
   */
  public boolean needSynchronization() {
    return !(added.isEmpty() && changed.isEmpty() && removed.isEmpty());
  }

  /**
   * INTERNAL: Applies all changes made to the topic map to the specified
   * fulltext indexer.
   * 
   * @param indexer
   *          The fulltext indexer to synchronize against.
   * @return True if the index was modified.
   * @throws IOException
   */
  public synchronized boolean synchronizeIndex(IndexerIF indexer)
      throws IOException {
    boolean changes = false;

    // delete removed objects
    if (!removed.isEmpty()) {
      Iterator<String> iter = removed.iterator();
      while (iter.hasNext()) {
        String oid = iter.next();
        indexer.delete("object_id", oid);
      }
      removed.clear();
      changes = true;
    }

    // delete changed objects
    if (!changed.isEmpty()) {
      Iterator<TMObjectIF> iter = changed.iterator();
      while (iter.hasNext()) {
        TMObjectIF o = iter.next();
        indexer.delete("object_id", o.getObjectId());
      }
    }

    // add added objects
    if (!added.isEmpty()) {
      Iterator<TMObjectIF> iter = added.iterator();
      while (iter.hasNext()) {
        TMObjectIF o = iter.next();
        if (o instanceof OccurrenceIF)
          indexer.index(docgen.generate((OccurrenceIF) o));
        else if (o instanceof TopicNameIF)
          indexer.index(docgen.generate((TopicNameIF) o));
        else if (o instanceof VariantNameIF)
          indexer.index(docgen.generate((VariantNameIF) o));
        else
          throw new OntopiaRuntimeException("Unknown type: " + o);
      }
      added.clear();
      changes = true;
    }

    // add changed objects
    if (!changed.isEmpty()) {
      Iterator<TMObjectIF> iter = changed.iterator();
      while (iter.hasNext()) {
        TMObjectIF o = iter.next();
        if (o instanceof OccurrenceIF)
          indexer.index(docgen.generate((OccurrenceIF) o));
        else if (o instanceof TopicNameIF)
          indexer.index(docgen.generate((TopicNameIF) o));
        else if (o instanceof VariantNameIF)
          indexer.index(docgen.generate((VariantNameIF) o));
        else
          throw new OntopiaRuntimeException("Unknown type: " + o);
      }
      changed.clear();
      changes = true;
    }
    // if changes then invalide lucene searcher
    if (changes && luceneSearcher != null) {
      try {
        luceneSearcher.close();
      } catch (IOException e) {
        throw new OntopiaRuntimeException(e);
      } finally {
        luceneSearcher = null;
      }
    }
    return changes;
  }

  // -----------------------------------------------------------------------------
  // IndexIF implementation
  // -----------------------------------------------------------------------------
  
  public synchronized SearchResultIF search(String query) throws IOException {
    if (luceneSearcher == null)
      luceneSearcher = new LuceneSearcher(luceneDirectory);

    return luceneSearcher.search(query);
  }
  
   public synchronized void close() throws IOException {
    if (luceneSearcher != null) { 
      luceneSearcher.close();
      luceneSearcher = null;
    }
   }
  
  // -----------------------------------------------------------------------------
  // Callbacks
  // -----------------------------------------------------------------------------

  protected synchronized void objectAdded(Object object) {
    TMObjectIF tmo = (TMObjectIF) object;
    removed.remove(tmo.getObjectId());
    changed.remove(tmo);
    added.add(tmo);
  }

  protected synchronized void objectRemoved(Object object) {
    TMObjectIF tmo = (TMObjectIF) object;
    added.remove(tmo);
    changed.remove(tmo);
    removed.add(tmo.getObjectId());
  }

  protected synchronized void objectChanged(Object object) {
    TMObjectIF tmo = (TMObjectIF) object;
    if (!added.contains(tmo) && !removed.contains(tmo.getObjectId()))
      changed.add(tmo);
  }

  // -----------------------------------------------------------------------------
  // Event handlers
  // -----------------------------------------------------------------------------

  /**
   * EventHandler: TMObjectIF changed
   */
  class TMObjectIF_changed extends EventHandler {
    public void processEvent(Object object, String event, Object new_value,
        Object old_value) {
      objectChanged(object);
    }
  }

  /**
   * EventHandler: TMObjectIF added to topic map
   */
  class TMObjectIF_added extends EventHandler {
    public void processEvent(Object object, String event, Object new_value,
        Object old_value) {
      objectAdded(new_value);
    }
  }

  /**
   * EventHandler: TMObjectIF removed from topic map
   */
  class TMObjectIF_removed extends EventHandler {
    public void processEvent(Object object, String event, Object new_value,
        Object old_value) {
      objectRemoved(old_value);
      
    }
  }

}
