/*
 * Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
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
 */
package fi.vm.sade.organisaatio.business.impl;

import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.business.OrganisaatioFindBusinessService;
import fi.vm.sade.organisaatio.dao.OrganisaatioDAO;
import fi.vm.sade.organisaatio.dao.OrganisaatioSuhdeDAO;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.model.OrganisaatioSuhde;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author simok
 */
@Transactional
@Service("organisaatioFindBusinessService")
public class OrganisaatioFindBusinessServiceImpl implements OrganisaatioFindBusinessService {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    @Autowired
    private OrganisaatioDAO organisaatioDAO;

    @Autowired
    private OrganisaatioSuhdeDAO organisaatioSuhdeDAO;

    @Override
    @Transactional(readOnly = true)
    public List<Organisaatio> findBySearchCriteria(
            List<String> kieliList,
            List<String> kuntaList,
            List<String> oppilaitostyyppiList,
            List<String> vuosiluokkaList,
            List<String> ytunnusList,
            List<String> oidList,
            int limit) {

        return organisaatioDAO.findBySearchCriteria(kieliList, kuntaList, oppilaitostyyppiList, vuosiluokkaList, ytunnusList, oidList, limit);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Organisaatio> findGroups() {
        return organisaatioDAO.findGroups();
    }


    @Override
    @Transactional(readOnly = true)
    public List<Organisaatio> findByOids(Collection<String> oids) {
        return organisaatioDAO.findByOids(oids);
    }

    @Override
    @Transactional(readOnly = true)
    public Organisaatio findById(String id) {
        Organisaatio o = organisaatioDAO.findByOid(id);
        if (o == null) {
            o = organisaatioDAO.findByYTunnus(id);
        }
        if (o == null) {
            o = organisaatioDAO.findByVirastoTunnus(id);
        }
        if (o == null) {
            o = organisaatioDAO.findByOppilaitoskoodi(id);
        }
        if (o == null) {
            o = organisaatioDAO.findByToimipistekoodi(id);
        }
        return o;
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> findOidsBy(String searchTerms, int count, int startIndex, OrganisaatioTyyppi type) {
        return organisaatioDAO.findOidsBy(false, count, startIndex, type);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganisaatioSuhde> findLiitokset(Date date) {
        return organisaatioSuhdeDAO.findLiitokset(date);
    }

}
