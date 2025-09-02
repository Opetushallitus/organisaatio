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

import com.google.common.collect.Maps;

import fi.vm.sade.organisaatio.business.exception.OrganisaatioDateException;
import fi.vm.sade.organisaatio.business.exception.OrganisaatioHierarchyException;
import fi.vm.sade.organisaatio.business.exception.OrganisaatioLakkautusKoulutuksiaException;
import fi.vm.sade.organisaatio.business.exception.OrganisaatioNameHistoryNotValidException;
import fi.vm.sade.organisaatio.business.exception.YtunnusException;
import fi.vm.sade.organisaatio.dto.v2.OrganisaatioMuokkausTiedotDTO;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.model.OrganisaatioNimi;
import fi.vm.sade.organisaatio.repository.OrganisaatioRepository;
import fi.vm.sade.organisaatio.service.OrganisationHierarchyValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author simok
 */
@Component
public class OrganisaatioBusinessChecker {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    @Autowired
    private OrganisaatioRepository organisaatioRepository;

    @Autowired
    private OrganisaatioTarjonta organisaatioTarjonta;

    @Value("${root.organisaatio.oid}")
    private String rootOrganisaatioOid;

    public boolean isEmpty(String val) {
        return val == null || val.isEmpty();
    }

    /**
     * Tarkastetaan, että nimihistorian alkupäivämäärät ovat valideja.
     *
     * @param nimet
     */
    public void checkNimihistoriaAlkupvm(Collection<OrganisaatioNimi> nimet) {
        Set<LocalDate> alkuPvms = new HashSet<>();
        for (OrganisaatioNimi nimi : nimet) {
            if (nimi.getAlkuPvm() == null) {
                throw new OrganisaatioNameHistoryNotValidException();
            }
            LocalDate alkuPvm = LocalDate.from(Instant.ofEpochMilli(nimi.getAlkuPvm().getTime()).atZone(ZoneId.systemDefault()));
            if (alkuPvms.contains(alkuPvm)) {
                throw new OrganisaatioNameHistoryNotValidException();
            }
            alkuPvms.add(alkuPvm);
        }
    }

    /**
     * Check that given code has not been used.
     *
     * @param org
     * @return
     */
    public boolean checkLearningInstitutionCodeIsUniqueAndNotUsed(Organisaatio org) {
        List<Organisaatio> orgs = organisaatioRepository.findByOppilaitosKoodi(org.getOppilaitosKoodi().trim());
        if (orgs != null && orgs.size() > 0) {
            return true;
        }

        return false;
    }

    /**
     * Check that given toimipistekoodi code has not been used.
     *
     * @param toimipistekoodi
     * @return
     */
    public boolean checkToimipistekoodiIsUniqueAndNotUsed(String toimipistekoodi) {
        List<Organisaatio> orgs = organisaatioRepository.findByToimipisteKoodi(toimipistekoodi.trim());
        if (orgs != null && orgs.size() > 0) {
            // toimipistekoodi on jo olemassa
            LOG.debug("Toimipistekoodi already exists: " + toimipistekoodi);
            return false;
        }

        return true;
    }

    /**
     * This is called when new organisaatio is saved - so there cannot be any
     * existing ytunnus.
     *
     * @param ytunnus
     */
    public void checkYtunnusIsUniqueAndNotUsed(String ytunnus) {
        if (ytunnus != null && !organisaatioRepository.isYtunnusAvailable(ytunnus)) {
            throw new YtunnusException();
        }
    }

    public void checkOrganisaatioHierarchy(Organisaatio organisaatio, String parentOid) {
        LOG.debug("checkOrganisaatioHierarchy()");

        final OrganisationHierarchyValidator validator = new OrganisationHierarchyValidator(rootOrganisaatioOid);
        Organisaatio parentOrg = (parentOid != null) ? this.organisaatioRepository.findFirstByOid(parentOid) : null;
        if (validator.apply(Maps.immutableEntry(parentOrg, organisaatio))) {
            //check children
            if (organisaatio.getId() != null) { // we can have children only if we are already saved
                List<Organisaatio> children = organisaatioRepository.findChildren(organisaatio.getId());
                for (Organisaatio child : children) {
                    if (!validator.apply(Maps.immutableEntry(organisaatio, child))) {
                        throw new OrganisaatioHierarchyException("invalid_child." + child.getOid());
                    }
                }
            }
        } else {
            throw new OrganisaatioHierarchyException();
        }
    }

    public void checkParentChildHierarchy(Organisaatio organisaatio, Organisaatio parentOrg) {
        LOG.debug("checkParentChildHierarchy()");

        final OrganisationHierarchyValidator validator = new OrganisationHierarchyValidator(rootOrganisaatioOid);

        if (validator.apply(Maps.immutableEntry(parentOrg, organisaatio)) == false) {
            throw new OrganisaatioHierarchyException();
        }
    }

    public void checkLakkautusAlkavatKoulutukset(Organisaatio entity) {
        if (organisaatioTarjonta.alkaviaKoulutuksia(entity.getOid(), entity.getLakkautusPvm())) {
            throw new OrganisaatioLakkautusKoulutuksiaException();
        }
    }

    public void checkPvmConstraints(Organisaatio organisaatio, Map<String, OrganisaatioMuokkausTiedotDTO> muokkausTiedot) {
        checkPvmConstraints(organisaatio, muokkausTiedot, null);
    }

    private void checkPvmConstraints(Organisaatio organisaatio, Map<String, OrganisaatioMuokkausTiedotDTO> muokkausTiedot, Instant maxPvm) {
        var actualStart = organisaatio.getAlkuPvm() != null ? (new Date(organisaatio.getAlkuPvm().getTime())).toInstant() : null;
        var actualEnd = organisaatio.getLakkautusPvm() != null ? (new Date(organisaatio.getLakkautusPvm().getTime())).toInstant() : null;
        OrganisaatioMuokkausTiedotDTO ownData = muokkausTiedot.get(organisaatio.getOid());
        if (ownData != null) {
            actualStart = ownData.getAlkuPvm() != null ? (new Date(ownData.getAlkuPvm().getTime())).toInstant() : actualStart;
            actualEnd = ownData.getLoppuPvm() != null ? (new Date(ownData.getLoppuPvm().getTime())).toInstant() : actualEnd;
        }

        if (actualStart != null && actualEnd != null && actualStart.isAfter(actualEnd)) {
            String virhe = String.format("oid: %s: käytetty alkuPvm (%s) > käytetty loppuPvm (%s)", organisaatio.getOid(), actualStart, actualEnd);
            LOG.warn(virhe);
            throw new OrganisaatioDateException();
        }
        if (actualEnd != null && maxPvm != null && actualEnd.isAfter(maxPvm)) {
            String virhe = String.format("oid: %s: käytetty loppuPvm (%s) > max päivämäärä (%s)", organisaatio.getOid(), actualEnd, maxPvm);
            LOG.warn(virhe);
            throw new OrganisaatioDateException();
        }

        for (Organisaatio child : organisaatio.getChildren(true)) {
            checkPvmConstraints(child, muokkausTiedot, actualEnd);
        }
    }
}
