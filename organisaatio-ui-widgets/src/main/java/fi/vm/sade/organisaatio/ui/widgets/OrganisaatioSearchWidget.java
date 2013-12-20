/*
 *
 * Copyright (c) 2012 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software:  Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://www.osor.eu/eupl/
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * European Union Public Licence for more details.
 */

package fi.vm.sade.organisaatio.ui.widgets;

import com.vaadin.data.Property;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import fi.vm.sade.generic.common.I18N;
import fi.vm.sade.organisaatio.api.model.types.*;
import fi.vm.sade.organisaatio.helper.OrganisaatioDisplayHelper;
import fi.vm.sade.organisaatio.ui.widgets.factory.OrganisaatioProxy;
import fi.vm.sade.vaadin.util.UiUtil;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.addon.customfield.CustomField;

/**
 *
 * @author Tuomas Katva
 */
public class OrganisaatioSearchWidget extends CustomField implements Property.ValueChangeListener {

    protected final Logger log = LoggerFactory.getLogger(getClass());
    private final String SEARCH_BUTTON_CAPTION_KEY = "OrganisaatioSearchWidget.organisaatioSearchBtn";
    private OrganisaatioSearchTree organisaatioSearchTree;
    private PopupView searchTreePopup;
    private Label searchResult;
    private Button searchButton;
    private HorizontalLayout rootLayout;
    private OrganisaatioDTO selectedOrg;
    private OrganisaatioSearchPopup orgTreePopup;
    private boolean showDomainName = false;
    private static int counter = 0;
    private OrganisaatioProxy organisaatioProxy;
   // private String rootOid;

    public OrganisaatioSearchWidget(OrganisaatioProxy organisaatioProxy, OrganisaatioSearchTree organisaatioSearchTree) {
        super();
        this.organisaatioSearchTree = organisaatioSearchTree;
        this.organisaatioProxy = organisaatioProxy;

        organisaatioSearchTree.getTree().addListener(this);
        organisaatioSearchTree.getTree().setImmediate(true);
        rootLayout = new HorizontalLayout();
        searchResult = UiUtil.label(null, "");
        rootLayout.addComponent(searchResult);
        searchButton = UiUtil.button(null, I18N.getMessage(SEARCH_BUTTON_CAPTION_KEY));

        rootLayout.addComponent(searchButton);
        orgTreePopup = new OrganisaatioSearchPopup(organisaatioSearchTree);
        searchTreePopup = new PopupView(orgTreePopup);
        searchTreePopup.setHideOnMouseOut(false);
        rootLayout.addComponent(searchTreePopup);
        setCompositionRoot(rootLayout);
        searchButton.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                searchTreePopup.setPopupVisible(true);

            }
        });
        this.setDebugIds("orgSearchWidget_searchBtn", "orgSearchWidget_searchResultLbl");
    }

    public void reloadWithOids(List<String> oids) {

        if (orgTreePopup != null) {
          //  oids = removeRootOidFromList(oids);
            orgTreePopup.reloadWithOids(oids);
        }
    }

    //private List<String> removeRootOidFromList(List<String> oids) {
    //    if (rootOid != null && oids.contains(rootOid)) {
    //        oids.remove(rootOid);
    //    }
     //   return oids;
   // }

    public void setButtonStyle(String styleName) {
        searchButton.setStyleName(styleName);
    }

    public final void setDebugIds(String btnDebugId, String lblDebugId) {
        if (searchButton != null) {
            counter++;
            searchButton.setDebugId(btnDebugId + "_" + counter);

        }
        if (searchResult != null) {
            counter++;
            searchResult.setDebugId(lblDebugId + "_" + counter);
        }
    }

    public boolean isShowDomainName() {
        return this.showDomainName;
    }

    public void setShowDomainName(boolean showDomainName) {
        this.showDomainName = showDomainName;
    }

    public OrganisaatioSearchTree getOrganisaatioSearchTree() {
        return this.organisaatioSearchTree;

    }

    public void setSearchButtonVisible(boolean visible) {
        if (searchButton != null && selectedOrg != null) {
            searchButton.setVisible(visible);

        }
    }

    public String getSelectedName() {
        if (this.searchResult != null && this.searchResult.getValue() != null) {
            return (String) searchResult.getValue();
        } else {
            return null;
        }
    }

    @Override
    public void setImmediate(boolean immediate) {
        super.setImmediate(immediate);
        searchResult.setImmediate(true);
        searchTreePopup.setImmediate(immediate);
    }

    @Override
    public void setValue(Object newValue) throws ReadOnlyException, ConversionException {
        super.setValue(newValue);
        String oid = (String) newValue;
        selectedOrg = organisaatioProxy.findByOid(oid);
        if (selectedOrg != null) {
            setLabelNimi();
        }

    }

    /*
     * @Override protected void setInternalValue(Object newValue) {
     * //super.setInternalValue(newValue); if (newValue instanceof String) {
     * setValue(newValue); } }
     */
    @Override
    public void setPropertyDataSource(Property newDataSource) {
        super.setPropertyDataSource(newDataSource);
        if (newDataSource.getValue() != null) {
            setValue(newDataSource.getValue());
        }
    }

    @Override
    public Object getValue() {
        if (selectedOrg != null && selectedOrg.getOid() != null) {
            return selectedOrg.getOid();
        } else {
            return null;
        }
    }

    private String getCaption(String captionKey) {
        if (I18N.getMessage(captionKey) != null) {
            return I18N.getMessage(captionKey);
        } else {
            return captionKey;
        }
    }

    public OrganisaatioDTO getSelectedOrganisaatio() {
        return selectedOrg;
    }

    public void setPopupWidth(String width) {
        this.orgTreePopup.setPopupWidth(width);
    }

    public void setPopupHeight(String height) {
        this.orgTreePopup.setPopupHeight(height);
    }

    @Override
    public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
        super.valueChange(event);
        if (event.getProperty() != null && event.getProperty().getValue() != null
                && event.getProperty().getValue().getClass().isAssignableFrom(OrganisaatioDTO.class)) {
            selectedOrg = (OrganisaatioDTO) event.getProperty().getValue();
            setLabelNimi();
            searchTreePopup.setPopupVisible(false);
        }
    }

    private String setLabelNimi() {
        if (showDomainName && selectedOrg != null && selectedOrg.getDomainNimi() != null) {
            searchResult.setValue(selectedOrg.getDomainNimi());
            return selectedOrg.getDomainNimi();
        } else if (searchResult != null && selectedOrg != null && OrganisaatioDisplayHelper.getClosest(I18N.getLocale(), selectedOrg) != null) {
            String orgname = "";

            if (I18N.getLocale() != null) {
                orgname = OrganisaatioDisplayHelper.getClosest(I18N.getLocale(), selectedOrg);
            } else {
                orgname = OrganisaatioDisplayHelper.getAvailableName(selectedOrg) != null ? OrganisaatioDisplayHelper.getAvailableName(selectedOrg) : "";
            }

            searchResult.setValue(orgname);
            return orgname;
        } else {
            return null;
        }
    }

    public Button getSearchButton() {
        if (searchButton != null) {
            return searchButton;
        } else {
            return null;
        }
    }

    public Label getSearchValueLabel() {
        if (searchResult != null) {
            return searchResult;
        } else {
            return null;
        }
    }

    @Override
    public Class<?> getType() {
        // This field always returns string
        return String.class;
    }
//    public String getRootOid() {
  //      return rootOid;
 //   }

 //   public void setRootOid(String rootOid) {
 //       this.rootOid = rootOid;
//   }
}
