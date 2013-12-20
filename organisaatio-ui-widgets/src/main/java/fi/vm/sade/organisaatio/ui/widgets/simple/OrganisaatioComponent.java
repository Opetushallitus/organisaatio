/**
 *
 */
package fi.vm.sade.organisaatio.ui.widgets.simple;

import com.vaadin.data.Property;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window;
import fi.vm.sade.generic.common.I18N;
import fi.vm.sade.organisaatio.api.model.types.OrganizationStructureType;
import fi.vm.sade.vaadin.Oph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.addon.customfield.CustomField;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

@SuppressWarnings("serial")
public class OrganisaatioComponent extends CustomField implements Property.ValueChangeListener {
    private static final Logger LOG = LoggerFactory.getLogger(OrganisaatioComponent.class);
    private final String SEARCH_BUTTON_CAPTION_KEY = "OrganisaatioSearchWidget.organisaatioSearchBtn";

    private OrganisaatioProxy organisaatioProxy;

    private HorizontalLayout rootLayout;
    private Label name;
    private Button selectButton;
    private OrganisaatioPopup popup;
    private OrgStructure selectedOrg;

    // access restricted to package level, use WidgetFactory to create!
    OrganisaatioComponent(OrganisaatioProxy organisaatioProxy, final Set<String> organisationsHierarchy) {
        this.organisaatioProxy = organisaatioProxy;

        rootLayout = new HorizontalLayout();

        name = new Label();
        rootLayout.addComponent(name);

        selectButton = new Button(I18N.getMessage(SEARCH_BUTTON_CAPTION_KEY));
        selectButton.addStyleName(Oph.BUTTON_SMALL);
        selectButton.setDisableOnClick(true);
        popup = new OrganisaatioPopup(organisaatioProxy);
        popup.getTree().addListener(this);
        selectButton.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                popup.refreshData(organisationsHierarchy);
                popup.setWidth("50%");
                popup.setHeight("500px");
                popup.setModal(true);

                getApplication().getMainWindow().addWindow(popup);
            }
        });

        popup.addListener(new Window.CloseListener() {
            @Override
            public void windowClose(Window.CloseEvent closeEvent) {
                selectButton.setEnabled(true);
            }
        });

        rootLayout.addComponent(selectButton);
        setCompositionRoot(rootLayout);
        setImmediate(true);
        name.setImmediate(true);
    }

    @Override
    public Class<?> getType() {
        return String.class;
    }

    @Override
    public Object getValue() {
        if (selectedOrg != null) {
            return selectedOrg.getOid();
        }
        return null;
    }

    @Override
    public void setValue(Object newValue) {
        super.setValue(newValue);
        String oid = (String) newValue;

        setOrganizationName(oid);

    }

    private void setOrganizationName(String oid) {
        List<OrganizationStructureType> organisaatios;
        try {
            organisaatios = organisaatioProxy.getOrganisaatios(Arrays.asList(oid));
        } catch (Exception e) {
            LOG.warn("Couldn't find organisation");
            return;
        }

        for (OrganizationStructureType organisaatio : organisaatios) {
            if (organisaatio.getOid().equals(oid)) {
                name.setValue(OrganisaatioNameUtil.getPreferredOrganisaatioNameForLanguage(organisaatio, I18N.getLocale().getLanguage()));
            }
        }
    }

    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        super.valueChange(event);
        if (event.getProperty() != null && event.getProperty().getValue() != null &&
                event.getProperty().getValue().getClass().isAssignableFrom(OrgStructure.class)) {
            selectedOrg = (OrgStructure) event.getProperty().getValue();
            name.setValue(OrganisaatioNameUtil.getPreferredOrganisaatioNameForLanguage(selectedOrg, I18N.getLocale().getLanguage()));
            popup.closeWindow();
        }
    }

    public void setSearchButtonVisible(boolean visible) {
        if (selectButton != null) {
            selectButton.setVisible(visible);
        }
    }

    public void reset() {
        super.setValue(null);
        selectedOrg = null;
        name.setValue("");
    }

    @Override
    public void setPropertyDataSource(Property newDataSource) {
        super.setPropertyDataSource(newDataSource);
        if (newDataSource.getValue() != null) {
            OrgStructure org = new OrgStructure();
            org.setOid((String) newDataSource.getValue());
            selectedOrg = org;
            setValue(newDataSource.getValue());
        }
    }
}
