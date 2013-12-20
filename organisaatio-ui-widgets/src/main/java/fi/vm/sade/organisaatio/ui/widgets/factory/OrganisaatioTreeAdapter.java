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
package fi.vm.sade.organisaatio.ui.widgets.factory;

import fi.vm.sade.generic.common.I18N;
import fi.vm.sade.generic.ui.component.GenericTreeAdapter;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioDTO;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioSearchCriteriaDTO;
import fi.vm.sade.organisaatio.helper.OrganisaatioDisplayHelper;
import java.util.Collection;
import java.util.List;

/**
 * @author Antti
 */
public class OrganisaatioTreeAdapter extends GenericTreeAdapter<OrganisaatioDTO> {

    private OrganisaatioProxy organisaatioProxy;

    private boolean showDomainName = false;
    private final int MAX_RESULTS = 10;
    private List<String> organisaatioRestrictionOids = null;

    OrganisaatioTreeAdapter(OrganisaatioProxy organisaatioproxy) {
        this.organisaatioProxy = organisaatioproxy;
    }

    OrganisaatioTreeAdapter(OrganisaatioProxy organisaatioProxy, boolean showDomainName) {
        this(organisaatioProxy);
        this.showDomainName = showDomainName;
    }

    public void setOrganisaatioRestrictionOids(List<String> oids) {
        this.organisaatioRestrictionOids = oids;
    }

    @Override
    public String getCaption(OrganisaatioDTO node) {
        if (!showDomainName || node.getDomainNimi() == null) {
            return OrganisaatioDisplayHelper.getCaption(node, I18N.getLocale()); //Locale.); //super.getCaption(node);
        } else {
            return node.getDomainNimi() + " (" + node.getYtunnus() + ") ";
        }
    }

    public Collection<OrganisaatioDTO> findByParentOids(List<String> oids) {
        return organisaatioProxy.findByParentOids(oids);
    }

    @Override
    public Collection<OrganisaatioDTO> findAll() {
        OrganisaatioSearchCriteriaDTO criteria = new OrganisaatioSearchCriteriaDTO();
        criteria.setMaxResults(MAX_RESULTS);
        return organisaatioProxy.find(criteria);
    }

    @Override
    public Collection<OrganisaatioDTO> find(Object search) {
        OrganisaatioSearchCriteriaDTO criteria = (OrganisaatioSearchCriteriaDTO) search;
        criteria.setMaxResults(MAX_RESULTS);
        if (organisaatioRestrictionOids != null) {
            criteria.getOidResctrictionList().clear();
            criteria.getOidResctrictionList().addAll(this.organisaatioRestrictionOids);
        }

        return organisaatioProxy.find(criteria);
    }

    @Override
    public Object getParentId(OrganisaatioDTO node) {
        return getProperty(node, "parentOid");
    }

    @Override
    public Object getId(OrganisaatioDTO node) {
        return getProperty(node, "oid");
    }
}
