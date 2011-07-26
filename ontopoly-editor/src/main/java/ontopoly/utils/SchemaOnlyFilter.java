
package ontopoly.utils;

import ontopoly.model.PSI;

import net.ontopia.topicmaps.core.AssociationIF;
import net.ontopia.topicmaps.core.AssociationRoleIF;
import net.ontopia.topicmaps.core.TopicIF;
import net.ontopia.topicmaps.utils.TypeHierarchyUtils;
import net.ontopia.utils.DeciderIF;

/**
 * INTERNAL: Filters out instances from the topic map so that only the
 * Ontopoly meta-ontology and the ontology remain.
 */
public class SchemaOnlyFilter implements DeciderIF {
  protected TypeHierarchyUtils thutils = new TypeHierarchyUtils();
  
  public boolean ok(Object object) {
    if (object instanceof TopicIF)
      return includeTopic((TopicIF) object);
    else if (object instanceof AssociationIF)
      return includeAssociation((AssociationIF) object);

    // if it's not a topic or association it's a component of a topic,
    // and the exporter will only ask if we've already said we're
    // including the topic. so whatever it is we want it included.
    return true;
  }

  protected boolean includeTopic(TopicIF topic) {
    // is this a system topic? if so, we include it
    TopicIF systemtopic = topic.getTopicMap().getTopicBySubjectIdentifier(PSI.ON_SYSTEM_TOPIC);
    if (thutils.isInstanceOf(topic, systemtopic))
      return true;
    
    // does every type of this topic include ON_TOPIC_TYPE among its types?
    // if so, this topic is an instance of a schema topic type, and it needs
    // to go.
    TopicIF metatype = topic.getTopicMap().
      getTopicBySubjectIdentifier(PSI.ON_TOPIC_TYPE);
    boolean allpass = true;
    for (TopicIF topictype : topic.getTypes()) {
      // so, is ON_TOPIC_TYPE the only type this topic has?
      if (topictype.getTypes().contains(metatype) &&
          topictype.getTypes().size() == 1)
        return false;
    }

    return true;
  }

  protected boolean includeAssociation(AssociationIF assoc) {
    for (AssociationRoleIF role : assoc.getRoles())
      if (includeTopic(role.getPlayer()))
        return true;
    return false; // not keeping any of the players, so...
  }
  
}
