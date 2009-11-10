// $Id: RoleField.java,v 1.13 2009/05/12 20:26:26 geir.gronmo Exp $

package net.ontopia.topicmaps.nav2.webapps.ontopoly.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.ontopia.infoset.core.LocatorIF;
import net.ontopia.topicmaps.core.AssociationIF;
import net.ontopia.topicmaps.core.AssociationRoleIF;
import net.ontopia.topicmaps.core.DataTypes;
import net.ontopia.topicmaps.core.OccurrenceIF;
import net.ontopia.topicmaps.core.TopicIF;
import net.ontopia.topicmaps.core.TopicMapBuilderIF;
import net.ontopia.topicmaps.core.TopicNameIF;
import net.ontopia.topicmaps.nav2.webapps.ontopoly.utils.OntopolyModelUtils;
import net.ontopia.topicmaps.nav2.webapps.ontopoly.utils.Ordering;
import net.ontopia.utils.CollectionUtils;
import net.ontopia.utils.ObjectUtils;

/**
 * Represents an association field, which is a combination of an association
 * type and a role type. Association types are not fields, since they cannot be
 * assigned to a topic type as a field without a role type.
 */
public class RoleField extends FieldDefinition {


  private AssociationField associationField;
  private RoleType roleType;

  public RoleField(TopicIF topic, TopicMap tm) {
		this(topic, tm, null, null);
	}

  public RoleField(TopicIF topic, TopicMap tm, RoleType roleType, AssociationField associationField) {
		super(topic, tm);

    this.associationField = associationField;
    this.roleType = roleType;
  }

  public int getFieldType() {
    return FIELD_TYPE_ROLE;
  }

  public String getFieldName() {
    Collection names = getTopicIF().getTopicNames();
    Iterator it = names.iterator();
    while (it.hasNext()) {
      TopicNameIF name = (TopicNameIF) it.next();
      if (name.getType() == null && name.getScope().isEmpty())
        return name.getValue();
    }		
    AssociationType atype = getAssociationType();
    RoleType rtype = getRoleType();
    return (atype == null ? "" : atype.getName()) + " (" + (rtype == null ? "" : rtype.getName()) + ")";
  }

  public boolean equals(Object obj) {
    if (!(obj instanceof RoleField))
      return false;
		
    RoleField other = (RoleField)obj;
    return (getTopicIF().equals(other.getTopicIF()));
  }

  public boolean isSortable() {
    TopicMap tm = getTopicMap();
    TopicIF aType = OntopolyModelUtils.getTopicIF(tm, PSI.ON, "is-sortable-field");
    TopicIF rType = OntopolyModelUtils.getTopicIF(tm, PSI.ON, "field-definition");
    TopicIF player = getTopicIF();
    return OntopolyModelUtils.isUnaryPlayer(tm, aType, player, rType);
  }

  public EditMode getEditMode() {
    String query = "on:use-edit-mode(%FD% : on:field-definition, $EM : on:edit-mode)?";
    Map<String,TopicIF> params = Collections.singletonMap("FD", getTopicIF());

    QueryMapper<TopicIF> qm = getTopicMap().newQueryMapperNoWrap();
    TopicIF editMode = qm.queryForObject(query, params);
    return (editMode == null ? EditMode.getDefaultEditMode(getTopicMap()) : new EditMode(editMode, getTopicMap()));
  }

  public CreateAction getCreateAction() {
    String query = "on:use-create-action(%FD% : on:field-definition, $CA : on:create-action)?";
    Map<String,TopicIF> params = Collections.singletonMap("FD", getTopicIF());

    QueryMapper<TopicIF> qm = getTopicMap().newQueryMapperNoWrap();
    TopicIF createAction = qm.queryForObject(query, params);
    return (createAction == null ? CreateAction.getDefaultCreateAction(getTopicMap()) : new CreateAction(createAction, getTopicMap()));
  }

  /**
   * Gets the association type.
   * 
   * @Return the association type.
   */
  public AssociationType getAssociationType() {
    AssociationField afield = getAssociationField();
    return (afield == null ? null : getAssociationField().getAssociationType());
  }

  /**
   * Gets the role type.
   * 
   * @Return the role type.
   */
  public RoleType getRoleType() {
    if (roleType == null) {
      TopicMap tm = getTopicMap();
      TopicIF aType = OntopolyModelUtils.getTopicIF(tm, PSI.ON, "has-role-type");
      TopicIF rType1 = OntopolyModelUtils.getTopicIF(tm, PSI.ON, "role-field");
      TopicIF player1 = getTopicIF();
      TopicIF rType2 = OntopolyModelUtils.getTopicIF(tm, PSI.ON, "role-type");
      Collection players = OntopolyModelUtils.findBinaryPlayers(tm, aType, player1, rType1, rType2);
      TopicIF roleType = (TopicIF)CollectionUtils.getFirst(players);
      return (roleType == null ? null : new RoleType(roleType, getTopicMap()));      
//			String query = "select $RT from on:has-role-type(%THIS% : on:role-field, $RT : on:role-type)?";
//			Map params = Collections.singletonMap("THIS", getTopicIF());
//			TopicIF rtype = (TopicIF)getTopicMap().getQueryWrapper().queryForObject(query, params);
//      if (rtype == null) return null;
//			this.roleType = new RoleType(rtype, getTopicMap());
		}
    return roleType;
  }

//!   /**
//!    * Sets the role type.
//!    * 
//!    * @param newRoleType the RoleType object to set. It cannot be null. 
//!    */
//!   public void setRoleType(RoleType newRoleType) {
//! 		TopicMap tm = getTopicMap();
//!     TopicIF HAS_ROLE_TYPE = OntopolyModelUtils.getTopicIF(tm, PSI.ON, "has-role-type");
//!     TopicIF ROLE_FIELD = OntopolyModelUtils.getTopicIF(tm, PSI.ON, "role-field");
//!     TopicIF ROLE_TYPE = OntopolyModelUtils.getTopicIF(tm, PSI.ON, "role-type");
//! 
//!     // on:has-role-type($FD : on:role-field, $RT : on:role-type)
//!     RoleType oldRoleType = getRoleType();
//!     if (oldRoleType != null) {
//!       AssociationIF associationIF = OntopolyModelUtils.findBinaryAssociation(tm, HAS_ROLE_TYPE,
//!         getTopicIF(), ROLE_FIELD,
//! 		  	oldRoleType.getTopicIF(), ROLE_TYPE);
//!       if (associationIF != null)
//!         associationIF.remove();
//!     }
//!     OntopolyModelUtils.makeBinaryAssociation(HAS_ROLE_TYPE, 
//! 			getTopicIF(), ROLE_FIELD,
//!       newRoleType.getTopicIF(), ROLE_TYPE);
//! 
//!     this.roleType = newRoleType;
//!   }

  public AssociationField getAssociationField() {
    if (associationField == null) {
			String query = "select $AF from on:has-association-field(%RF% : on:role-field, $AF : on:association-field)?";
			Map<String,TopicIF> params = Collections.singletonMap("RF", getTopicIF());
			
			QueryMapper<TopicIF> qm = getTopicMap().newQueryMapperNoWrap();	    
			TopicIF afield = qm.queryForObject(query, params);
			
			this.associationField = new AssociationField(afield, getTopicMap());
		}
    return associationField;
  }

  /**
   * Gets the other RoleField objects this object's association type topic takes part in.
   * 
   * @returns the other RoleField objects this object's association type topic takes part in.
   */
  public Collection<RoleField> getFieldsForOtherRoles() {
    AssociationField afield = getAssociationField();
    Collection<RoleField> fields = afield.getFieldsForRoles();
		List<RoleField> ofields = new ArrayList<RoleField>(fields);
		ofields.remove(this);
    return ofields;
  }

//!   /**
//!    * Sets the name of the association type in this direction. Means setting the
//!    * value of the the base name on the association type with the role type as
//!    * scope. If no such base name exists, it must be created.
//!    * 
//!    * @param label the name of the association type in this direction.
//!    */
//!   public void setLabel(String _label) {
//! 		String label = (_label == null ? "" : _label);
//! 		// replace existing
//!     Collection names = getTopicIF().getTopicNames();
//!     Iterator it = names.iterator();
//!     while (it.hasNext()) {
//!       TopicNameIF name = (TopicNameIF) it.next();
//!       if (name.getType() == null && name.getScope().isEmpty()) {
//!         name.setValue(label);
//!         return;
//!       }
//!     }
//! 		// or, create new name
//!     TopicMapBuilderIF builder = getTopicMap().getTopicMapIF().getBuilder();
//!     builder.makeTopicName(getTopicIF(), label);
//!   }

  /**
   * Gets the interface control assigned for this association field. If no interface control object is assigned, the
   * method will return the default interface control, which is drop-down-list.
   * 
   * @return the interface control assigned to this association field. 
   */
  public InterfaceControl getInterfaceControl() {
    String query = "on:use-interface-control(%FD% : on:field-definition, $IC : on:interface-control)?";

    Map<String,TopicIF> params = Collections.singletonMap("FD", getTopicIF());
    
    QueryMapper<TopicIF> qm = getTopicMap().newQueryMapperNoWrap();         
		TopicIF interfaceControl = qm.queryForObject(query, qm.newRowMapperOneColumn(), params);
		
		return interfaceControl == null ? InterfaceControl.getDefaultInterfaceControl(getTopicMap()) : new InterfaceControl(interfaceControl, getTopicMap());
  }

//  /**
//   * Assigns an interface control to this association field.
//   *  
//   * @param control the interface control to be assigned to this association field.
//   */
//  public void setInterfaceControl(InterfaceControl control) {
//		TopicMap tm = getTopicMap();
//    TopicIF aType = OntopolyModelUtils.getTopicIF(tm, PSI.ON, "use-interface-control");
//    TopicIF rType1 = OntopolyModelUtils.getTopicIF(tm, PSI.ON, "field-definition");
//    TopicIF rType2 = OntopolyModelUtils.getTopicIF(tm, PSI.ON, "interface-control");
//
//    Collection interfaceControlAssocs = OntopolyModelUtils
//        .findBinaryAssociations(tm, aType, getTopicIF(), rType1, rType2);
//    Iterator it = interfaceControlAssocs.iterator();
//    while (it.hasNext()) {
//      ((AssociationIF) it.next()).remove();
//    }
//    OntopolyModelUtils.makeBinaryAssociation(aType, getTopicIF(), rType1,
//        control.getTopicIF(), rType2);
//  }

  /**
   * Gets the topic types that have been declared as valid and which
   * may play the other roles in this association type.
   * 
   * @return the topic types which may play the other roles in this association type.
   */
  public Collection<TopicType> getDeclaredPlayerTypes() {
    String query = "select $ttype from on:has-field(%FD% : on:field-definition, $ttype : on:field-owner)?";

    Map<String,TopicIF> params = Collections.singletonMap("FD", getTopicIF());
    
    QueryMapper<TopicType> qm = getTopicMap().newQueryMapper(TopicType.class);
    return qm.queryForList(query, qm.newRowMapperOneColumn(), params);
  }

  public Collection<TopicType> getAllowedPlayerTypes(Topic currentTopic) {
    String query = getAllowedPlayersTypesQuery();
    if (query == null) {
      query = "subclasses-of($SUP, $SUB) :- { "
          + "xtm:superclass-subclass($SUP : xtm:superclass, $SUB : xtm:subclass) | "
          + "xtm:superclass-subclass($SUP : xtm:superclass, $MID : xtm:subclass), "
          + "subclasses-of($MID, $SUB) }. ";
      query += "select $avtype from "
        + "on:has-field(%field% : on:field-definition, $ttype : on:field-owner), "
        + "{ $avtype = $ttype | subclasses-of($ttype, $avtype) }, "
        + "not(on:is-abstract($avtype : on:topic-type))?";
    }

    Map<String,TopicIF> params = new HashMap<String,TopicIF>(2);
    params.put("field", getTopicIF());
    if (currentTopic != null)
      params.put("topic", currentTopic.getTopicIF());
    
    QueryMapper<TopicType> qm = getTopicMap().newQueryMapper(TopicType.class);
    return qm.queryForList(query, qm.newRowMapperOneColumn(), params);
  }

  private String getAllowedPlayersQuery() {
    TopicIF topicIf = getTopicIF();   
    TopicIF typeIf = OntopolyModelUtils.getTopicIF(getTopicMap(), PSI.ON, "allowed-players-query");
    OccurrenceIF occ = OntopolyModelUtils.findOccurrence(typeIf, topicIf);
    return (occ == null ? null : occ.getValue());
  }

  private String getAllowedPlayersSearchQuery() {
    TopicIF topicIf = getTopicIF();   
    TopicIF typeIf = OntopolyModelUtils.getTopicIF(getTopicMap(), PSI.ON, "allowed-players-search-query");
    OccurrenceIF occ = OntopolyModelUtils.findOccurrence(typeIf, topicIf);
    return (occ == null ? null : occ.getValue());
  }

  private String getAllowedPlayersTypesQuery() {
    TopicIF topicIf = getTopicIF();   
    TopicIF typeIf = OntopolyModelUtils.getTopicIF(getTopicMap(), PSI.ON, "allowed-players-types-query");
    OccurrenceIF occ = OntopolyModelUtils.findOccurrence(typeIf, topicIf);
    return (occ == null ? null : occ.getValue());
  }

  public Collection<Topic> getAllowedPlayers(Topic currentTopic) {

    Collection<Topic> result = new HashSet<Topic>();
    String query = getAllowedPlayersQuery();
    if (query != null) {
      Map<String,TopicIF> params = new HashMap<String,TopicIF>();
      params.put("field", getTopicIF());
      params.put("topic", currentTopic.getTopicIF());
      
      QueryMapper<Topic> qm = getTopicMap().newQueryMapper(Topic.class);
      return qm.queryForList(query, qm.newRowMapperOneColumn(), params);
    
    } else {
      Collection topicTypes = getAllowedPlayerTypes(currentTopic);
      Iterator iter = topicTypes.iterator();
      while (iter.hasNext()) {
        TopicType topicType = (TopicType)iter.next();
        result.addAll(topicType.getInstances());
      }
    }
    return result;
  }

  /**
   * Search for the topics that match the given search term. Only topics of allowed
   * player types are returned.
   * 
   * @param searchTerm the search term used to search for topics.
   * @return a collection of Topic objects
   */
  public List<Topic> searchAllowedPlayers(String searchTerm) {
    try {
      String query = getAllowedPlayersSearchQuery();
      if (query == null)
        query = "select $player, $score from "
          + "on:has-field(%field% : on:field-definition, $ttype : on:field-owner), "
          + "instance-of($player, $ttype), "
          + "topic-name($player, $tn), value-like($tn, %search%, $score) "
          + "order by $score desc, $player?";
  
      Map<String,Object> params = new HashMap<String,Object>(2);
      params.put("field", getTopicIF());
      params.put("search", searchTerm);
  
      QueryMapper<TopicIF> qm = getTopicMap().newQueryMapperNoWrap();         
      Collection<TopicIF> rows = qm.queryForList(query, qm.newRowMapperOneColumn(), params);
  
      Iterator it = rows.iterator();
      List<Topic> results = new ArrayList<Topic>(rows.size());
      Collection<TopicIF> duplicateChecks = new HashSet<TopicIF>(rows.size());
  
      while (it.hasNext()) {
        TopicIF topic = (TopicIF) it.next();
        if (duplicateChecks.contains(topic))
          continue; // avoid duplicates
        results.add(new Topic(topic, getTopicMap()));
        duplicateChecks.add(topic);
      } 
      return results;
    } catch (Exception e) {
      return Collections.emptyList();
    }
  }

//!   /**
//!    * Adds this association field to a topic type.
//!    * 
//!    * @param player the topic type this association field will be assigned to.
//!    */
//!   public void addUsedBy(TopicType player) {
//!     if (getUsedBy().contains(player))
//!       return; // The topic type player has already this field.
//!     player.addField(this);
//!   }
//! 
//!   /**
//!    * Removes this association field from a topic type.
//!    * 
//!    * @param player the topic type this association field will be removed from.
//!    */
//!   public void removeUsedBy(TopicType player) {
//!     if (getUsedBy().contains(player))
//!       player.removeField(this);
//!   }
//! 
//!   /**
//!    * Tests whether this association field is hierarchical.
//!    * 
//!    * @return true if this is association field is hierarchical.
//!    */
//!   public boolean isHierarchical() {
//!     AssociationType atype = getAssociationType();
//!     return (atype == null ? false : atype.isHierarchical());
//!   }
//! 
//!   /**
//!    * Sets this association field to parent (superordinate-role-type). 
//!    * This affects which role it takes in a hierarchical association.
//!    */
//!   public void setParent() {
//! 		TopicMap tm = getTopicMap();
//! 		RoleType roleType = getRoleType();
//!     roleType.getTopicIF().removeType(
//! 			OntopolyModelUtils.getTopicIF(tm, PSI.TECH, "#subordinate-role-type"));
//!     roleType.getTopicIF().addType(
//! 			OntopolyModelUtils.getTopicIF(tm, PSI.TECH, "#superordinate-role-type"));
//!   }
//! 
//!   /**
//!    * Sets this association field to child (subordinate-role-type). 
//!    * This affects which role it takes in a hierarchical association.
//!    */
//!   public void setChild() {
//! 		TopicMap tm = getTopicMap();
//! 		RoleType roleType = getRoleType();
//!     roleType.getTopicIF().removeType(
//! 		  OntopolyModelUtils.getTopicIF(tm, PSI.TECH, "#superordinate-role-type"));
//!     roleType.getTopicIF().addType(
//!       OntopolyModelUtils.getTopicIF(tm, PSI.TECH, "#subordinate-role-type"));
//!   }
//! 
//!   /**
//!    * Tests whether this association field is playing the parent role in hierarchical associations.
//!    * 
//!    * @return true if it is playing the parent role in hierarchical associations.
//!    */
//!   public boolean isParent() {
//!     TopicIF superordinate = OntopolyModelUtils.getTopicIF(getTopicMap(), PSI.TECH, "#superordinate-role-type");
//!     return getRoleType().getTopicIF().getTypes().contains(superordinate);
//!   }
//! 
//!   /**
//!    * Tests whether this association field is playing the child role in hierarchical associations.
//!    * 
//!    * @return true if it is playing the child role in hierarchical associations.
//!    */
//!   public boolean isChild() {
//!     TopicIF subordinate = OntopolyModelUtils.getTopicIF(getTopicMap(), PSI.TECH, "#subordinate-role-type");
//!     return getRoleType().getTopicIF().getTypes().contains(subordinate);
//!   }

  /**
   * Gets the instance topics on the other side of an association an instance topic takes part in.
   * 
   * @param topic the instance topic that takes part in the association.
   * @return the instance topics on the other side of an association an instance topic takes part in.
   */  
  public List<ValueIF> getValues(Topic topic) { 
    Collection roles = getRoles(topic);
    
    List<ValueIF> result = new ArrayList<ValueIF>(roles.size());
    Iterator iter = roles.iterator();
    while (iter.hasNext()) {
      AssociationRoleIF role = (AssociationRoleIF) iter.next();
			ValueIF value = createValue(this, role);
			if (value != null)
				result.add(value);
    }
    return result;
  }

  private Collection getRoles(Topic topic) {
    AssociationType atype = getAssociationType();
    if (atype == null) return Collections.EMPTY_SET;
    TopicIF associationTypeIf = atype.getTopicIF();

    RoleType rtype = getRoleType();
    if (rtype == null) return Collections.EMPTY_SET;
    TopicIF roleTypeIf = rtype.getTopicIF();

    TopicIF playerIf = topic.getTopicIF();
    
    return OntopolyModelUtils.findRoles(associationTypeIf, roleTypeIf, playerIf);    
  }
  
  public List getOrderedValues(Topic topic, RoleField ofield) { 
    List<ValueIF> values = getValues(topic);
    if (values.size() > 1) {
      Map<Topic,OccurrenceIF> topics_occs = getValuesWithOrdering(topic);
      Collections.sort(values, new MapValueComparator(topics_occs, ofield, topic));
    }
    return values;
  }

  private static class MapValueComparator implements Comparator<ValueIF> {
    //! private static final String DEFAULT_ORDER_VALUE = "999999999";    
    private static final String DEFAULT_ORDER_VALUE = null; // sorts before "000000000"    
    private Map entries;
    private RoleField ofield;
    private Topic oplayer;
    MapValueComparator(Map entries, RoleField ofield, Topic oplayer) {
      this.entries = entries;
      this.ofield = ofield;
      this.oplayer = oplayer;
    }
    public int compare(ValueIF v1, ValueIF v2) {
      try {
        Topic p1 = v1.getPlayer(ofield, oplayer);
        Topic p2 = v2.getPlayer(ofield, oplayer);
        OccurrenceIF oc1 = (OccurrenceIF)entries.get(p1);
        OccurrenceIF oc2 = (OccurrenceIF)entries.get(p2);
        Comparable c1 = (oc1 == null ? DEFAULT_ORDER_VALUE : oc1.getValue());
        Comparable c2 = (oc2 == null ? DEFAULT_ORDER_VALUE : oc2.getValue());
        return ObjectUtils.compare(c1, c2);
      } catch (Exception e) {
        // should not fail when comparing. bergen kommune has had an issue where this happens. we thus ignore for now.
//        e.printStackTrace();
        return 0;
      }
    }
  }

  private Map<Topic,OccurrenceIF> getValuesWithOrdering(Topic topic) {

    TopicIF topicIf = topic.getTopicIF();   
    TopicIF typeIf = OntopolyModelUtils.getTopicIF(topic.getTopicMap(), PSI.ON, "field-value-order");
    LocatorIF datatype = DataTypes.TYPE_STRING;

    TopicIF fieldDefinitionIf = getTopicIF();

    Map<Topic,OccurrenceIF> topics_occs = new HashMap<Topic,OccurrenceIF>();
    Iterator iter = OntopolyModelUtils.findOccurrences(typeIf, topicIf, datatype).iterator();
    while (iter.hasNext()) {
      OccurrenceIF occ = (OccurrenceIF)iter.next();
      Collection scope = occ.getScope();
      if (scope.size() == 2 && scope.contains(fieldDefinitionIf)) { // note: this is value ordering
        Iterator siter = scope.iterator();
        while (siter.hasNext()) {
          TopicIF theme = (TopicIF)siter.next();
          if (!theme.equals(fieldDefinitionIf)) {
            // FIXME: if map already contains key, we might want to delete occ
            topics_occs.put(new Topic(theme, topic.getTopicMap()), occ);
            break;
          }
        }
      }
    }
    return topics_occs;
  }

  /**
   * Adds an instance topic to the other side of an association an instance topic takes part in.
   * 
   * @param topic the instance topic that takes part in the association.
   * @param _value an object representing the instance topic that will be added to the other
   * side of the association the instance topic (topic) takes part in.
   */
  public void addValue(FieldInstance fieldInstance, Object _value, LifeCycleListener listener) {
    ValueIF value = (ValueIF) _value;
    ValueIF replacedValue = null;
    
    AssociationType atype = getAssociationType();
    if (atype == null) return;
		TopicIF atypeIf = atype.getTopicIF();
    TopicIF[] rtypes = getRoleTypes(value);
    TopicIF[] players = getPlayers(value);
    
    // if cardinality is 0:1 or 1:1 then clear existing values
    if (fieldInstance.getFieldAssignment().getCardinality().isMaxOne()) {      
      // remove all existing values
      ValueIF existingValue = null;
      Collection roles = getRoles(fieldInstance.getInstance());
      Iterator iter = roles.iterator();
      while (iter.hasNext()) {
        AssociationRoleIF role = (AssociationRoleIF)iter.next();
        ValueIF valueIf = createValue(this, role);
        if (valueIf == null) {
          continue;
        } if (valueIf.equals(value)) {
          existingValue = valueIf;
        } else {
          if (replacedValue == null) 
            replacedValue = valueIf; // keep track of one replaced value
          removeValue(fieldInstance, valueIf, listener);
        }
      }
      
      // create new
      if (existingValue == null)
        OntopolyModelUtils.makeAssociation(atypeIf, rtypes, players, Collections.EMPTY_SET);
    } else {
      Collection assocs = OntopolyModelUtils.findAssociations(atypeIf,
          rtypes, players, Collections.EMPTY_SET);
      
      if (assocs.isEmpty()) {
        // create new
        OntopolyModelUtils.makeAssociation(atypeIf, rtypes, players, Collections.EMPTY_SET);
      } else {
        // remove all except the first one
        Iterator iter = assocs.iterator();
        iter.next();
        while (iter.hasNext()) {
          AssociationIF assoc = (AssociationIF) iter.next();
          assoc.remove();
        }
      }
    }
    if (replacedValue != null)
      listener.onAfterReplace(fieldInstance, replacedValue, value);
    else
      listener.onAfterAdd(fieldInstance, value);
  }

//  protected void clear(FieldInstance fieldInstance, LifeCycleListener listener) {
//    Collection roles = getRoles(fieldInstance.getInstance());
//    Iterator iter = roles.iterator();
//    while (iter.hasNext()) {
//      AssociationRoleIF role = (AssociationRoleIF)iter.next();
//      ValueIF valueIf = createValue(this, role);
//      removeValue(fieldInstance, valueIf, listener);
//    }
//  }
  
  /**
   * Removes an instance topic from the other side of an association an instance topic takes part in.
   * 
   * @param fieldInstance the field instance that takes part in the association.
   * @param _value an object representing the instance topic that will be removed from the other
   * side of the association the instance topic (topic) takes part in.
   */  
  public void removeValue(FieldInstance fieldInstance, Object _value, LifeCycleListener listener) {
    ValueIF value = (ValueIF) _value;

    AssociationType atype = getAssociationType();
    if (atype == null) return;
		TopicIF atypeIf = atype.getTopicIF();
    TopicIF[] rtypes = getRoleTypes(value);
    TopicIF[] players = getPlayers(value);

    listener.onBeforeRemove(fieldInstance, value);
    
    Collection assocs = OntopolyModelUtils.findAssociations(atypeIf,
        rtypes, players, Collections.EMPTY_SET);

    if (!assocs.isEmpty()) {
      // remove all the matching
      Iterator iter = assocs.iterator();
      while (iter.hasNext()) {
        AssociationIF assoc = (AssociationIF) iter.next();
        assoc.remove();
      }
    }
    // TODO: consider removing field value order also
  }

  /**
   * Factory method for creating a ValueIF object, which represent an instance topic on one side of an association.
   * 
   * @param roleField the role field containing the association type and the role type representing another side of the association.
   * @param role the role type on the side of the association that the instance topic is going to be created.
   * @return the ValueIF object that represent an instance topic on one side of an association.
   */
  private static ValueIF createValue(RoleField roleField, AssociationRoleIF role) {
    Collection fields = roleField.getAssociationField().getFieldsForRoles();
    int fieldCount = fields.size();

    TopicMap topicMap = roleField.getTopicMap();
    AssociationIF assoc = role.getAssociation();
    
    // ignore roles where the arity does not match
    Collection aroles = assoc.getRoles();
    if (fieldCount != aroles.size())
      return null;
      
		ValueIF value = createValue(fieldCount);
    value.addPlayer(roleField, new Topic(role.getPlayer(), roleField.getTopicMap()));

    Object[] roles = aroles.toArray();
    Collection<AssociationRoleIF> matched = new HashSet<AssociationRoleIF>(roles.length);
    matched.add(role);

		int selfMatch = 0;
    Iterator iter = fields.iterator();
    while (iter.hasNext()) {
      RoleField ofield = (RoleField) iter.next();
			// only match your own field once
      if (ofield.equals(roleField)) {
				if (++selfMatch == 1)
					continue;
			}
      RoleType ortype = ofield.getRoleType();
      if (ortype == null) return null;
      boolean match = false;
      for (int i = 0; i < roles.length; i++) {
        AssociationRoleIF orole = (AssociationRoleIF) roles[i];
        if (matched.contains(orole))
          continue;
        if (ObjectUtils.equals(orole.getType(), ortype.getTopicIF())) {
          matched.add(orole);
          value.addPlayer(ofield, new Topic(orole.getPlayer(), topicMap));
          match = true;
        }
      }
      if (!match)
				return null;
    }
    return value;
  }

  /**
   * Factory method for creating a ValueIF object, which represent an instance topic on one side of an association.
   * 
   * @param arity the number of players that the association value should have.
   * @return the ValueIF object that represent an instance topic on one side of an association.
   */
  public static ValueIF createValue(int arity) {
    return new Value(arity);
  }

  /**
   * Interface. This interface is implemented by the Value class.
   */
  public static interface ValueIF {

		public int getArity();

    public RoleField[] getRoleFields();

		public Topic[] getPlayers();

    public void addPlayer(RoleField roleField, Topic player);

    public Topic getPlayer(RoleField roleField, Topic oplayer);

  }

  /**
   * Static inner class containing a Map object, which connects
   * instance topics to associations.
   */
  private static class Value implements RoleField.ValueIF {

		int offset;
		RoleField[] roleFields;
		Topic[] players;

    Value(int arity) {
			this.roleFields = new RoleField[arity];
			this.players = new Topic[arity];
		}

		public int getArity() {
			return roleFields.length;
		}

    public RoleField[] getRoleFields() {
      return roleFields;
    }

		public Topic[] getPlayers() {
			return players;
		}

    public void addPlayer(RoleField roleField, Topic player) {
			roleFields[offset] = roleField;
			players[offset] = player;
			offset++;
		}

    public Topic getPlayer(RoleField roleField, Topic oPlayer) {
			// NOTE: all this logic is here to cater for symmetric associations
			Topic xPlayer = null;
			for (int i=0; i < roleFields.length; i++) {
				RoleField rf = roleFields[i];
				if (rf.equals(roleField)) {
					Topic player = players[i];
					if (ObjectUtils.different(player, oPlayer))
						return player;
					else
						xPlayer = oPlayer;
				}
			}
			if (xPlayer == null)
				throw new RuntimeException("Could not find player for RoleField: " + roleField + " (" + oPlayer + ")");			
			else
				return xPlayer;
		}

    @Override
		public String toString() {
			StringBuffer sb = new StringBuffer();
      sb.append("ValueIF(");
			sb.append(getArity());
			sb.append(": ");
			for (int i=0; i < roleFields.length; i++) {
				if (i > 0) sb.append(", ");
				if (roleFields[i] == null)
					sb.append("null");
				else {
          RoleType rtype = roleFields[i].getRoleType();
					sb.append((rtype == null ? null : rtype.getTopicIF()));
        }
				sb.append(":");
				if (players[i] == null)
					sb.append("null");
				else
					sb.append(players[i].getTopicIF());
			}
      sb.append(")");
			return sb.toString();
		}
  }

	public TopicIF[] getRoleTypes(ValueIF value) {
		RoleField[] roleFields = value.getRoleFields();
		int arity = value.getArity();
		TopicIF[] rtypes = new TopicIF[arity];
		for (int i=0; i < arity; i++) {
			rtypes[i] = roleFields[i].getRoleType().getTopicIF();
		}
		return rtypes;
	}
	
	private TopicIF[] getPlayers(ValueIF value) {
		Topic[] players = value.getPlayers();
		int arity = value.getArity();		
		TopicIF[] topics = new TopicIF[arity];
		for (int i=0; i < arity; i++) {
			topics[i] = players[i].getTopicIF();
		}
		return topics;
	}

  private Collection<Topic> getValues(Topic instance, RoleField ofield) {
    Collection<ValueIF> values = getValues(instance);
    Collection<Topic> result = new HashSet<Topic>(values.size());
    Iterator<ValueIF> iter = values.iterator();
    while (iter.hasNext()) {
      ValueIF rfv = iter.next();
      Topic player = rfv.getPlayer(ofield, instance);
      result.add(player);
    }
    return result;
  }

  /**
   * Change field value order so that the first value is ordered directly after the second value.
   **/
  public void moveAfter(Topic instance, RoleField ofield, RoleField.ValueIF rfv1, RoleField.ValueIF rfv2) {
    Topic p1 = rfv1.getPlayer(ofield, instance);
    Topic p2 = rfv2.getPlayer(ofield, instance);
//    System.out.println("P1: " + p1 + " P2: " + p2);

    TopicIF typeIf = OntopolyModelUtils.getTopicIF(instance.getTopicMap(), PSI.ON, "field-value-order");
    LocatorIF datatype = DataTypes.TYPE_STRING;
    TopicIF fieldDefinitionIf = getTopicIF();

    TopicIF topicIf = instance.getTopicIF();
    TopicIF p1topic = p1.getTopicIF();
    TopicIF p2topic = p2.getTopicIF();

    Collection alltopics = getValues(instance, ofield);
//    System.out.println("ALL: " + alltopics);

    Map<Topic,OccurrenceIF> topics_occs = getValuesWithOrdering(instance);
//    System.out.println("TO: " + topics_occs);

    List<OccurrenceIF> occs = new ArrayList<OccurrenceIF>(topics_occs.values());
    Collections.sort(occs, new Comparator<OccurrenceIF>() {
        public int compare(OccurrenceIF occ1, OccurrenceIF occ2) {
          return ObjectUtils.compare(occ1.getValue(), occ2.getValue());
        }
      });

    TopicMapBuilderIF builder = topicIf.getTopicMap().getBuilder();

    OccurrenceIF maxOcc = (OccurrenceIF)(occs.isEmpty() ? null : occs.get(occs.size()-1));
    int fieldOrderMax = (maxOcc == null ? 0 : Ordering.stringToOrder(maxOcc.getValue()));

    // make sure this value has an order value
    OccurrenceIF p1occ = null;
    OccurrenceIF p2occ = (OccurrenceIF)topics_occs.get(p2);
    OccurrenceIF next_occ = null;
    int fieldOrderP2;
    int nextOrder = Ordering.MAX_ORDER;
//    System.out.println("PO1: " + p1occ + " PO2: " + p2occ);
    if (p2occ == null) {
      fieldOrderP2 = (fieldOrderMax == 0 ? 0 : fieldOrderMax + Ordering.ORDER_INCREMENTS);
      p2occ = builder.makeOccurrence(topicIf, typeIf, Ordering.orderToString(fieldOrderP2), datatype);
      p2occ.addTheme(fieldDefinitionIf);
      p2occ.addTheme(p2topic);
    } else {
      fieldOrderP2 = Ordering.stringToOrder(p2occ.getValue());
      // find occurrence after p2occ
      int indexP2occ = occs.indexOf(p2occ);
      if (indexP2occ < (occs.size()-1))
        next_occ = (OccurrenceIF)occs.get(indexP2occ+1);
      if (next_occ != null) {
        // if next then average this and next field orders
        int fieldOrderNext = Ordering.stringToOrder(next_occ.getValue());
        nextOrder = (fieldOrderP2 + fieldOrderNext)/2;
//        System.out.println("NO1: " + nextOrder + " " + fieldOrderP2);
        if (nextOrder != fieldOrderP2) {
          p1occ = (OccurrenceIF)topics_occs.get(p1);
          if (p1occ != null) {
            p1occ.setValue(Ordering.orderToString(nextOrder));
          } else {
            p1occ = builder.makeOccurrence(topicIf, typeIf, Ordering.orderToString(nextOrder), datatype);
            p1occ.addTheme(fieldDefinitionIf);
            p1occ.addTheme(p1topic);
          }
        }
      }
    }
//    System.out.println("FO2: " + fieldOrderP2);
//    System.out.println("PO1: " + p1occ + " PO2: " + p2occ);
    if (nextOrder == Ordering.MAX_ORDER)
      nextOrder = fieldOrderP2;
//    System.out.println("NO2: " + nextOrder);
    if (p1occ == null) {
      nextOrder += Ordering.ORDER_INCREMENTS;
      p1occ = (OccurrenceIF)topics_occs.get(p1);
      if (p1occ != null) {
        p1occ.setValue(Ordering.orderToString(nextOrder));
      } else {
        p1occ = builder.makeOccurrence(topicIf, typeIf, Ordering.orderToString(nextOrder), datatype);
        p1occ.addTheme(fieldDefinitionIf);
        p1occ.addTheme(p1topic);
      }

      // we need to reshuffle all existing orders after p2
      int indexP2occ = occs.indexOf(p2occ);
      if (indexP2occ > 0) {
        for (int i=indexP2occ+1; i < occs.size(); i++) {
          OccurrenceIF occ = (OccurrenceIF)occs.get(i);
          nextOrder += Ordering.ORDER_INCREMENTS;
          occ.setValue(Ordering.orderToString(nextOrder));      
        }
      }
    }
    // assign ordering to all topics with no existing ordering
    alltopics.remove(p1);
    alltopics.remove(p2);
    Iterator aiter = alltopics.iterator();
    while (aiter.hasNext()) {
      Topic atopic = (Topic)aiter.next();
      if (!topics_occs.containsKey(atopic)) {
        nextOrder += Ordering.ORDER_INCREMENTS;
//        System.out.println("NN: " + atopic + " " + nextOrder);
        OccurrenceIF occ = builder.makeOccurrence(topicIf, typeIf, Ordering.orderToString(nextOrder), datatype);
        occ.addTheme(fieldDefinitionIf);
        occ.addTheme(atopic.getTopicIF());
      }
    }

//    System.out.println("OCCS: " + occs);
//    System.out.println("OCC MAX: " + fieldOrderMax);
//    System.out.println("Last: " + nextOrder);
//
//    System.out.println("P2 order: " + p2occ.getValue()); 
//    System.out.println("P1 order: " + p1occ.getValue());   
  }

}
