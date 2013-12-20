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
package fi.vm.sade.organisaatio.revised.ui.component.organisaatioform;

import com.vaadin.ui.OptionGroup;

import fi.vm.sade.generic.common.I18N;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.ui.PortletRole;


/**
 * The component for organisaatiotyyppi selection on OrganisaatioEditForm.
 *
 * @author markus
 *
 */
class OrganisaatiotyyppiComponent extends OptionGroup {

    
    private boolean editing;
    private boolean isParentOrg;
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    OrganisaatiotyyppiComponent(boolean isParentOrg, boolean editing) {
        this.editing = editing;
        this.isParentOrg = isParentOrg;
        setMultiSelect(true);
        setCaption(I18N.getMessage("c_orgTyyppi"));
        createOrganisatiotyyppiSelection();
    }

    /**
     * Selects the organisaatiotyyppi options according to organisaatiotyyppi of organisaatio.
     */
    private void createOrganisatiotyyppiSelection() {
        
        for (OrganisaatioTyyppi curTyyppi : OrganisaatioTyyppi.values()) {

            if (!isParentOrg) {
                if (curTyyppi.equals(OrganisaatioTyyppi.OPETUSPISTE) || curTyyppi.equals(OrganisaatioTyyppi.OPPILAITOS) || curTyyppi.equals(OrganisaatioTyyppi.OPPISOPIMUSTOIMIPISTE))
                addItemToOptionGroup(curTyyppi);
            } else {
                addItemToOptionGroup(curTyyppi);
            }
        }
    }
    
    private void addItemToOptionGroup(OrganisaatioTyyppi curTyyppi) {
        String translatedCaption = T(curTyyppi.name());
        addItem(curTyyppi.value());
        setItemCaption(curTyyppi.value(), translatedCaption);
        setItemEnabled(curTyyppi.value(),
                PortletRole.getInstance().getPermissionService()
                        .userCanCreateOrganisationOfType(curTyyppi));
    }

    /**
     * Translate key to property (named ) value.
     *
     * @param key
     * @return property "OrganisaatiotyyppiComponent." + key value
     */
    private String T(String key) {
        return I18N.getMessage("OrganisaatiotyyppiComponent." + key);
    }
}
