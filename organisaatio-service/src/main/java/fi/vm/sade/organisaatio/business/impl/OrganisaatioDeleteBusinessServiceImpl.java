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

import fi.vm.sade.organisaatio.business.OrganisaatioDeleteBusinessService;
import fi.vm.sade.organisaatio.business.exception.OrganisaatioDeleteKoulutuksiaException;
import fi.vm.sade.organisaatio.business.exception.OrganisaatioDeleteParentException;
import fi.vm.sade.organisaatio.business.exception.OrganisaatioNotFoundException;
import fi.vm.sade.organisaatio.dao.OrganisaatioDAO;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.resource.IndexerResource;

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
@Service("organisaatioDeleteBusinessService")
public class OrganisaatioDeleteBusinessServiceImpl implements OrganisaatioDeleteBusinessService {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    @Autowired
    private OrganisaatioDAO organisaatioDAO;

    @Autowired
    private IndexerResource solrIndexer;

    @Autowired
    private OrganisaatioKoulutukset organisaatioKoulutukset;

    @Override
    public Organisaatio deleteOrganisaatio(String oid) {

        // Haetaan poistettava organisaatio
        Organisaatio org = organisaatioDAO.findByOid(oid);
        if (org == null) {
            LOG.warn("Cannot find organisaatio to be deleted: " + oid);
            throw new OrganisaatioNotFoundException(oid);
        }

        // Poistettavalla organisaatiolla ei saa olla lapsia
        if (org.getChildCount(null,new Date()) != 0) {
            LOG.warn("Organisaatio to be deleted: " + oid + " contains child organisations: " +
                    org.getChildCount(null,new Date()));
            throw new OrganisaatioDeleteParentException();
        }

        // Poistettavalla organisaatiolla ei saa olla alkavia koulutuksia
        if (organisaatioKoulutukset.alkaviaKoulutuksia(org.getOid(), new Date())) {
            LOG.warn("Cannot to be deleted: " + oid + " contains 'koulutuksia'");
            throw new OrganisaatioDeleteKoulutuksiaException();
        }

        // Merkitään organisaatio poistetuksi
        Organisaatio parent = organisaatioDAO.markRemoved(oid);

        // Poistetaan deletoitu organisaatio solr:sta
        solrIndexer.delete(oid);

        // Päivitetään poistetun organisaation parentin childCount solriin
        solrIndexer.index(parent);

        return parent;
    }
}
