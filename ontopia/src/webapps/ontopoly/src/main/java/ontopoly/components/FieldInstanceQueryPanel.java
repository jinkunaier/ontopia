package ontopoly.components;

import java.util.List;

import ontopoly.LockManager;
import ontopoly.OntopolySession;
import ontopoly.model.FieldAssignment;
import ontopoly.model.FieldDefinition;
import ontopoly.model.FieldInstance;
import ontopoly.model.Topic;
import ontopoly.model.TopicType;
import ontopoly.models.FieldDefinitionModel;
import ontopoly.models.FieldInstanceModel;
import ontopoly.models.FieldValueModel;
import ontopoly.models.FieldValuesModel;
import ontopoly.models.FieldsViewModel;
import ontopoly.models.TopicModel;
import ontopoly.utils.OntopolyUtils;

import org.apache.wicket.PageParameters;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.ResourceModel;

public class FieldInstanceQueryPanel extends AbstractFieldInstancePanel {

  public FieldInstanceQueryPanel(String id, final FieldInstanceModel fieldInstanceModel, 
      final FieldsViewModel fieldsViewModel, final boolean readonly, final boolean traversable) {
    this(id, fieldInstanceModel, fieldsViewModel, readonly, false, traversable);
  }

  public FieldInstanceQueryPanel(String id, final FieldInstanceModel fieldInstanceModel, 
      final FieldsViewModel fieldsViewModel, final boolean readonly, final boolean embedded, final boolean traversable) {
    super(id, fieldInstanceModel);

    FieldInstance fieldInstance = fieldInstanceModel.getFieldInstance();
    FieldAssignment fieldAssignment = fieldInstance.getFieldAssignment();
    FieldDefinition fieldDefinition = fieldAssignment.getFieldDefinition(); 

    //! add(new Label("fieldLabel", new Model(fieldDefinition.getFieldName())));
    add(new FieldDefinitionLabel("fieldLabel", new FieldDefinitionModel(fieldDefinition)));

    // set up container
    this.fieldValuesContainer = new WebMarkupContainer("fieldValuesContainer");
    fieldValuesContainer.setOutputMarkupId(true);    
    add(fieldValuesContainer);

    // add feedback panel
    this.feedbackPanel = new FeedbackPanel("feedback", new AbstractFieldInstancePanelFeedbackMessageFilter());
    feedbackPanel.setOutputMarkupId(true);
    fieldValuesContainer.add(feedbackPanel);

    WebMarkupContainer fieldValuesList = new WebMarkupContainer("fieldValuesList");
    fieldValuesContainer.add(fieldValuesList);

    // add field values component(s)
    this.fieldValuesModel = new FieldValuesModel(fieldInstanceModel); // NOTE: no comparator
    fieldValuesModel.setAutoExtraField(false);
    
    this.listView = new ListView<FieldValueModel>("fieldValues", fieldValuesModel) {
      public void populateItem(final ListItem<FieldValueModel> item) {
        final FieldValueModel fieldValueModel = item.getModelObject();

        // TODO: make sure non-existing value field gets focus if last edit happened there        
        Object value = fieldValueModel.getObject();
  //        TopicIF topic = (TopicIF)value;
  //        Topic oPlayer = new Topic(topic, fieldValueModel.getFieldInstanceModel().getFieldInstance().getInstance().getTopicMap());
        Topic oPlayer = (Topic)value;
        final String topicMapId = (oPlayer == null ? null : oPlayer.getTopicMap().getId());
        final String topicId = (oPlayer == null ? null : oPlayer.getId());

        final WebMarkupContainer fieldValueButtons = new WebMarkupContainer("fieldValueButtons");
        fieldValueButtons.setOutputMarkupId(true);
        item.add(fieldValueButtons);

        // acquire lock for embedded topic
        final boolean isLockedByOther;
        if (embedded && fieldValueModel.isExistingValue()) {
          OntopolySession session = (OntopolySession)Session.get();
          String lockerId = session.getLockerId(getRequest());
          LockManager.Lock lock = session.lock(oPlayer, lockerId);
          isLockedByOther = !lock.ownedBy(lockerId);
        } else {
          isLockedByOther = false;
        }
        final boolean _readonly = readonly || isLockedByOther;
        
        if (embedded) {
          TopicType defaultTopicType = OntopolyUtils.getDefaultTopicType(oPlayer);
          List<FieldInstance> fieldInstances = oPlayer.getFieldInstances(defaultTopicType, fieldsViewModel.getFieldsView());
          // if no matching fields show link to topic instead
          if (fieldInstances.isEmpty()) {
            // player link
            TopicLink<Topic> playerLink = new TopicLink<Topic>("fieldValue", new TopicModel<Topic>(oPlayer), fieldsViewModel);
            playerLink.setEnabled(traversable);
            item.add(playerLink);          
          } else {
            // embedded topic
            List<FieldInstanceModel> fieldInstanceModels = FieldInstanceModel.wrapInFieldInstanceModels(fieldInstances);
            FieldInstancesPanel fip = new FieldInstancesPanel("fieldValue", fieldInstanceModels, fieldsViewModel, _readonly, traversable);
            fip.setRenderBodyOnly(true);
            item.add(fip);
          }
        } else {
          // player link
          TopicLink<Topic> playerLink = new TopicLink<Topic>("fieldValue", new TopicModel<Topic>(oPlayer), fieldsViewModel);
          playerLink.setEnabled(traversable);
          item.add(playerLink);
        }

        // embedded goto button
        OntopolyImageLink gotoButton = new OntopolyImageLink("goto", "goto.gif", new ResourceModel("icon.goto.topic")) {
          @Override
          public boolean isVisible() {
            FieldValueModel fieldValueModel = item.getModelObject();
            return embedded && fieldValueModel.isExistingValue();  
          }
          @Override
          public void onClick(AjaxRequestTarget target) {
            // navigate to topic
            PageParameters pageParameters = new PageParameters();
            pageParameters.put("topicMapId", topicMapId);
            pageParameters.put("topicId", topicId);
            setResponsePage(getPage().getClass(), pageParameters);
            setRedirect(true);
          }
        };
        fieldValueButtons.add(gotoButton);

        // embedded lock button
        OntopolyImageLink lockButton = new OntopolyImageLink("lock", "lock.gif", new ResourceModel("icon.topic.locked")) {
          @Override
          public boolean isVisible() {
            return embedded && isLockedByOther;  
          }
          @Override
          public void onClick(AjaxRequestTarget target) {
          }
        };
        fieldValueButtons.add(lockButton);

      }
    };
    listView.setReuseItems(true);
    fieldValuesList.add(listView);

    // empty for now
    this.fieldInstanceButtons = new WebMarkupContainer("fieldInstanceButtons");
    fieldInstanceButtons.setOutputMarkupId(true);
    add(fieldInstanceButtons);
  }
  
}
