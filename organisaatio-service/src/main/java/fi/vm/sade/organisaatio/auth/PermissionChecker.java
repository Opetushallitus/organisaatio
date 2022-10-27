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

import com.google.common.base.Objects;
import fi.vm.sade.organisaatio.business.exception.NotAuthorizedException;
import fi.vm.sade.organisaatio.dto.v4.OrganisaatioRDTOV4;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.repository.OrganisaatioRepository;
import fi.vm.sade.organisaatio.service.converter.MonikielinenTekstiTyyppiToEntityFunction;
import fi.vm.sade.organisaatio.service.util.OrganisaatioUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Map;


/**
 * Encapsulate most of the auth check logic done at server here.
 */
@Component
@Transactional(readOnly = true)
public class PermissionChecker {
    private final Logger LOG = LoggerFactory.getLogger(getClass());

    @Autowired
    private OrganisaatioRepository organisaatioRepository;

    @Autowired
    private OrganisaatioPermissionServiceImpl permissionService;

    private final MonikielinenTekstiTyyppiToEntityFunction mkt2entity = new MonikielinenTekstiTyyppiToEntityFunction();

    public void checkRemoveOrganisation(String oid) {
        final OrganisaatioContext authContext = OrganisaatioContext.get(organisaatioRepository.findFirstByOid(oid));
        checkPermission(permissionService.userCanDeleteOrganisation(authContext));
    }

    public void checkUpdateOrganisationName(String oid) {
        Organisaatio current = organisaatioRepository.findFirstByOid(oid);
        final OrganisaatioContext authContext = OrganisaatioContext.get(current);
        checkPermission(permissionService.userCanEditName(authContext));
    }

    public void checkSaveOrganisation(OrganisaatioRDTOV4 organisaatio, boolean update) {
        final OrganisaatioContext authContext = OrganisaatioContext.get(organisaatio);
        checkSaveOrganisation(authContext, update, organisaatio.getOid(), organisaatio.getNimi(),
                organisaatio.getAlkuPvm(), organisaatio.getLakkautusPvm());
    }

    public void checkUpdateOrganisation(String oid) {
        final OrganisaatioContext authContext = OrganisaatioContext.get(oid);
        checkPermission(permissionService.userCanUpdateOrganisation(authContext));
    }

    private void checkSaveOrganisation(OrganisaatioContext authContext, boolean update,
                                       String oid, Map<String, String> nimi,
                                       Date alkuPvm, Date lakkautusPvm) {
        if (update) {
            final Organisaatio current = organisaatioRepository.findFirstByOid(oid);

            if (!Objects.equal(current.getNimi().getValues(), nimi)) {
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
            checkPermission(permissionService.userCanCreateOrganisation(authContext));
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

    public void checkReadOrganisation(String oid) {
        Organisaatio organisaatio = organisaatioRepository.findFirstByOid(oid);

        if(organisaatio == null){
            return;
        }
        checkPermission(!organisaatio.isPiilotettu() || permissionService.userCanReadOrganisation(organisaatio.getOid()));
    }

    public boolean isReadAccessToAll() {
         return permissionService.isReadAccessToAll();
    }
}
