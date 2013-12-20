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

package fi.vm.sade.organisaatio.ui.factory;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import fi.vm.sade.generic.common.I18N;
import fi.vm.sade.generic.ui.component.TreeAdapter;
import fi.vm.sade.organisaatio.api.model.types.YhteystietojenTyyppiDTO;
import fi.vm.sade.organisaatio.api.model.OrganisaatioService;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioSearchCriteriaDTO;
import fi.vm.sade.organisaatio.api.model.types.SearchCriteriaDTO;
import fi.vm.sade.organisaatio.helper.OrganisaatioDisplayHelper;


/**
 * Tree adapter for the search tree displayed in the yhteystietojen tyyppi spesification page.
 *
 * @author markus
 *
 */
public class YhteystietojenTyyppiTreeAdapter implements TreeAdapter<YhteystietojenTyyppiDTO>, Serializable {


    public YhteystietojenTyyppiTreeAdapter(OrganisaatioService oltService) {
        this.oltService = oltService;
    }

    
    private OrganisaatioService oltService;


    @Override
    public Collection<YhteystietojenTyyppiDTO> findAll() {
        List<YhteystietojenTyyppiDTO> ytts = oltService.findYhteystietojenTyyppis(new OrganisaatioSearchCriteriaDTO());
        Collections.sort(ytts, new Comparator<YhteystietojenTyyppiDTO>() {
            public int compare(YhteystietojenTyyppiDTO f1, YhteystietojenTyyppiDTO f2) {
                return OrganisaatioDisplayHelper.getYttCaption(I18N.getLocale(), f1).compareTo(OrganisaatioDisplayHelper.getYttCaption(I18N.getLocale(), f2));
            }
        });
        return ytts;
    }

    @Override
    public Object getId(YhteystietojenTyyppiDTO node) {
        
        return node.getOid();
    }

    @Override
    public Object getParentId(YhteystietojenTyyppiDTO node) {
        
        return null;
    }

    @Override
    public String getCaption(YhteystietojenTyyppiDTO node) {
        
        return OrganisaatioDisplayHelper.getYttCaption(I18N.getLocale(), node);
    }
    
    
    @Override
    public Collection<YhteystietojenTyyppiDTO> find(Object search) {
        List<YhteystietojenTyyppiDTO> ytts = oltService.findYhteystietojenTyyppis(new SearchCriteriaDTO());
        Collections.sort(ytts, new Comparator<YhteystietojenTyyppiDTO>() {
            public int compare(YhteystietojenTyyppiDTO f1, YhteystietojenTyyppiDTO f2) {
                return OrganisaatioDisplayHelper.getYttCaption(I18N.getLocale(), f1).compareTo(OrganisaatioDisplayHelper.getYttCaption(I18N.getLocale(), f2));
            }
        });
        return ytts;
    }
}