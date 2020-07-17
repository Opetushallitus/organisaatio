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
import fi.vm.sade.organisaatio.business.exception.OrganisaatioDeleteHakukohteitaException;
import fi.vm.sade.organisaatio.business.exception.OrganisaatioDeleteKoulutuksiaException;
import fi.vm.sade.organisaatio.business.exception.OrganisaatioDeleteParentException;
import fi.vm.sade.organisaatio.business.exception.OrganisaatioNotFoundException;
import fi.vm.sade.organisaatio.dao.OrganisaatioDAO;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.service.util.OrganisaatioUtil;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static fi.vm.sade.organisaatio.business.impl.OrganisaatioFindBusinessServiceImpl.JALKELAISET_CACHE_NAME;
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
    private OrganisaatioTarjonta organisaatioTarjonta;

    @Override
    @CacheEvict(cacheNames = JALKELAISET_CACHE_NAME, allEntries = true)
    public Organisaatio deleteOrganisaatio(String oid) {
        Organisaatio parent;

        // Haetaan poistettava organisaatio
        Organisaatio org = organisaatioDAO.findByOid(oid);
        if (org == null) {
            LOG.warn("Cannot find organisaatio to be deleted: " + oid);
            throw new OrganisaatioNotFoundException(oid);
        }

        // Poistettavalla organisaatiolla ei saa olla lapsia
        if (org.getChildCount(new Date()) != 0) {
            LOG.warn("Organisaatio to be deleted: " + oid + " contains child organisations: " +
                    org.getChildCount(new Date()));
            throw new OrganisaatioDeleteParentException();
        }

        // Ryhmä ja organisaatio käsitellään eri tavalla
        if (OrganisaatioUtil.isRyhma(org)) {
            // Poistettavalla ryhmällä ei saa olla hakukohteita
            if (organisaatioTarjonta.hakukohteita(org.getOid())) {
                LOG.warn("Cannot delete group: " + oid + " contains 'hakukohteita'");
                throw new OrganisaatioDeleteHakukohteitaException();
            }

            // Merkitään ryhmä poistetuksi
            parent = organisaatioDAO.markRemoved(oid);
        }
        else {
            // Poistettavalla organisaatiolla ei saa olla alkavia koulutuksia
            if (organisaatioTarjonta.alkaviaKoulutuksia(org.getOid())) {
                LOG.warn("Cannot delete organisaatio: " + oid + " contains 'koulutuksia'");
                throw new OrganisaatioDeleteKoulutuksiaException();
            }

            // Merkitään organisaatio poistetuksi
            parent = organisaatioDAO.markRemoved(oid);
        }

        return parent;
    }
}
