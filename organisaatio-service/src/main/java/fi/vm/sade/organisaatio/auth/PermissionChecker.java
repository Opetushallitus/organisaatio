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
package fi.vm.sade.organisaatio.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.google.common.base.Objects;

import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.model.MonikielinenTeksti;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.resource.dto.OrganisaatioRDTO;
import fi.vm.sade.organisaatio.business.exception.NotAuthorizedException;
import fi.vm.sade.organisaatio.dao.OrganisaatioDAO;
import fi.vm.sade.organisaatio.dto.v3.OrganisaatioRDTOV3;
import fi.vm.sade.organisaatio.service.converter.MonikielinenTekstiTyyppiToEntityFunction;
import fi.vm.sade.organisaatio.service.util.OrganisaatioUtil;
import java.util.Date;

import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Encapsulate most of the auth check logic done at server here.
 */
@Component
public class PermissionChecker {
    private final Logger LOG = LoggerFactory.getLogger(getClass());

    @Autowired
    private OrganisaatioDAO organisaatioDAO;

    @Autowired
    private OrganisaatioPermissionServiceImpl permissionService;

    private final MonikielinenTekstiTyyppiToEntityFunction mkt2entity = new MonikielinenTekstiTyyppiToEntityFunction();

    private boolean checkCRUDRyhma(OrganisaatioContext authContext) {
        Set<OrganisaatioTyyppi> tyypit = authContext.getOrgTypes();
        if (tyypit.size() == 1 && tyypit.contains(OrganisaatioTyyppi.RYHMA)) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            for (GrantedAuthority ga : auth.getAuthorities()) {
                if (ga.getAuthority().startsWith("ROLE_APP_ORGANISAATIOHALLINTA_RYHMA_")) {
                    return true;
                }
            }
        }
        return false;
    }

    public void checkRemoveOrganisation(String oid) {
        final OrganisaatioContext authContext = OrganisaatioContext.get(organisaatioDAO.findByOid(oid));
        if (checkCRUDRyhma(authContext)) {
            return;
        }
        checkPermission(permissionService.userCanDeleteOrganisation(authContext));
    }

    private MonikielinenTeksti convertMapToMonikielinenTeksti(Map<String, String> m) {
        MonikielinenTeksti mt = new MonikielinenTeksti();
        for (Map.Entry<String, String> e : m.entrySet()) {
            mt.addString(e.getKey(), e.getValue());
        }
        return mt;
    }

    public void checkUpdateOrganisationName(String oid) {
        final OrganisaatioContext authContext = OrganisaatioContext.get(oid);

        checkPermission(permissionService.userCanEditName(authContext));
    }

    public void checkSaveOrganisation(OrganisaatioRDTO organisaatio, boolean update) {
        final OrganisaatioContext authContext = OrganisaatioContext.get(organisaatio);
        checkSaveOrganisation(authContext, update, organisaatio.getOid(), organisaatio.getNimi(),
                organisaatio.getAlkuPvm(), organisaatio.getLakkautusPvm(), organisaatio.getParentOid());
    }

    public void checkSaveOrganisation(OrganisaatioRDTOV3 organisaatio, boolean update) {
        final OrganisaatioContext authContext = OrganisaatioContext.get(organisaatio);
        checkSaveOrganisation(authContext, update, organisaatio.getOid(), organisaatio.getNimi(),
                organisaatio.getAlkuPvm(), organisaatio.getLakkautusPvm(), organisaatio.getParentOid());
    }

    private void checkSaveOrganisation(OrganisaatioContext authContext, boolean update,
            String oid, Map<String, String> nimi,
            Date alkuPvm, Date lakkautusPvm, String parentOid) {
        if (checkCRUDRyhma(authContext)) {
            return;
        }

        if (update) {
            final Organisaatio current = organisaatioDAO.findByOid(oid);

            if (!Objects.equal(current.getNimi(), convertMapToMonikielinenTeksti(nimi))) {
                LOG.info("Nimi muuttunut");

                // name changed
                checkPermission(permissionService.userCanEditName(authContext));
            }
            if (OrganisaatioUtil.isSameDay(alkuPvm, current.getAlkuPvm()) == false) {
                LOG.info("Alkupäivämäärä muuttunut: " +
                        current.getAlkuPvm() + " -> " + alkuPvm);

                // date(s) changed
                checkPermission(permissionService.userCanEditDates(authContext));
            }
            if (OrganisaatioUtil.isSameDay(lakkautusPvm, current.getLakkautusPvm()) == false) {
                LOG.info("Lakkautuspäivämäärä muuttunut: " +
                        current.getLakkautusPvm() + " -> " + lakkautusPvm);

                // date(s) changed
                checkPermission(permissionService.userCanEditDates(authContext));
            }
            checkPermission(permissionService.userCanUpdateOrganisation(authContext));
        } else {
            checkPermission(permissionService.userCanCreateOrganisation(OrganisaatioContext.get(parentOid)));
        }
    }

    private void checkPermission(boolean result) {
        if (!result) {
            throw new NotAuthorizedException("no.permission");
        }
    }

    public void checkEditYhteystietojentyyppi() {
        checkPermission(permissionService.userCanEditYhteystietojenTyypit());
    }

}
