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

import com.google.common.base.Preconditions;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.security.AbstractPermissionService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Configurable
public class OrganisaatioPermissionServiceImpl extends AbstractPermissionService {

    private static final String ROLE_RYHMA = "APP_ORGANISAATIOHALLINTA_RYHMA";
    public static final String ORGANISAATIOHALLINTA = "ORGANISAATIOHALLINTA";

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("${root.organisaatio.oid}")
    private String ophOid;

    protected OrganisaatioPermissionServiceImpl() {
        super(ORGANISAATIOHALLINTA);
    }

    public OrganisaatioPermissionServiceImpl(String ophOid) {
        super(ORGANISAATIOHALLINTA);
        this.ophOid = ophOid;
    }

    public boolean userCanReadOrganisation(String oid){
        Preconditions.checkNotNull(oid);
        return checkAccess(oid, ROLE_CRUD, ROLE_RU, ROLE_R);

    }

    public boolean userCanUpdateOrganisation(OrganisaatioContext context) {
        Preconditions.checkNotNull(context.getOrgOid());
        /*
         * https://confluence.csc.fi/display/oppija/B1%29+Muokkaa+organisaation+
         * tietoja Käyttöoikeudet huomioitava: Vain OPH:n pääkäyttäjä voi
         * muokata tyyppiä muu organisaatio olevia organisaatioita. Muut
         * virkailijat voivat muokata oman organisaatiotasonsa ja sen
         * alapuolisten organisaatioiden tietoja.
         */

        if (checkAccess(context.getOrgOid(), ROLE_CRUD, ROLE_RU)) {
            if (!context.getOrgTypes().contains(OrganisaatioTyyppi.MUU_ORGANISAATIO)
                    && !context.getOrgTypes().contains(OrganisaatioTyyppi.TYOELAMAJARJESTO)) {
                return true;
            }
        }

        // implicitly oph crud user can edit whatever, is this true?
        if (checkAccess(ophOid, ROLE_CRUD)){
            return true;
        }

        //oph ru can edit everything else but muu organisaatio (OVT-4755)
        if (checkAccess(ophOid, ROLE_RU)){
            if (!context.getOrgTypes().contains(OrganisaatioTyyppi.MUU_ORGANISAATIO)
                    && !context.getOrgTypes().contains(OrganisaatioTyyppi.TYOELAMAJARJESTO)) {
                return true;
            }
        }

        if (context.isRyhma() && userCanEditRyhma()) {
            return true;
        }

        return false;
    }

    public boolean userCanCreateOrganisation(OrganisaatioContext context) {
        /*
         * https://confluence.csc.fi/display/oppija/A2%29+Luo+organisaatio
         * Käyttöoikeudet huomioitava: Vain OPH:n pääkäyttäjä voi luoda
         * koulutustoimijan tai muun organisaation. Muut virkailija voivat luoda
         * alaorganisaatioita oman organisaatiotasonsa alle.
         */
        if (context.getOrgTypes().stream().anyMatch(type -> !userCanCreateOrganisationOfType(type))) {
            return false;
        }
        return checkAccess(context.getParentOrgOid(), ROLE_CRUD) || checkAccess(ophOid, ROLE_CRUD)
                || (context.isRyhma() && userCanCreateDeleteRyhma());
    }


    /**
     * Vain oph virkailija voi muuttaa oppilaitoksen nimeä
     * @param context
     * @return
     */
    public boolean userCanEditName(OrganisaatioContext context) {
        if(context.getOrgTypes().contains(OrganisaatioTyyppi.OPPILAITOS) || context.getOrgTypes().contains(OrganisaatioTyyppi.KOULUTUSTOIMIJA) || context.getOrgTypes().contains(OrganisaatioTyyppi.MUU_ORGANISAATIO)
                || context.getOrgTypes().contains(OrganisaatioTyyppi.VARHAISKASVATUKSEN_JARJESTAJA)
                || context.getOrgTypes().contains(OrganisaatioTyyppi.TYOELAMAJARJESTO)) {
            return checkAccess(ophOid, ROLE_CRUD, ROLE_RU);
        }
        return true;
    }

    /**
     * Check if user is allowed to create organisation of specified type
     * @param tyyppi
     * @return
     */
    public boolean userCanCreateOrganisationOfType(OrganisaatioTyyppi tyyppi) {
        switch (tyyppi) {
        case KOULUTUSTOIMIJA:
        case OPPILAITOS:
        case MUU_ORGANISAATIO:
            //only oph
            return checkAccess(ophOid, ROLE_CRUD);
        case VARHAISKASVATUKSEN_JARJESTAJA:
        case TYOELAMAJARJESTO:
            //only oph
            return checkAccess(ophOid, ROLE_CRUD);
        case OPPISOPIMUSTOIMIPISTE:
        case TOIMIPISTE:
        case VARHAISKASVATUKSEN_TOIMIPAIKKA:
            return checkAccess(new String[]{ROLE_CRUD}) || checkAccess(ophOid, ROLE_CRUD);
        case RYHMA:
            return userCanCreateDeleteRyhma();

        default:
            log.error("Unhandled or type:" + tyyppi + " returning false!");
            break;
        }

        return false;
    }


    public boolean userCanDeleteOrganisation() {
        return checkAccess(ophOid, ROLE_CRUD);
    }

    public boolean userCanUpdateYTJ() {
        return checkAccess(ophOid, ROLE_CRUD);
    }

    /**
     * Is user allowed to edit start and end dates.
     * OH-15: Vain OPH:N virkailija voi muuttaa koulutustoimijan ja oppilaitoksen perustamis- ja lakkauttamispäiviä
     * @param context
     * @return
     */
    public boolean userCanEditDates(final OrganisaatioContext context) {
        Preconditions.checkNotNull(context, "organisaatioContext cannot be null");
        Preconditions.checkNotNull(context.getOrgTypes(), "organisaatioContext.orgTypes cannot be null");
        Preconditions.checkArgument(context.getOrgTypes().size()>0, "organisaatioContext.orgTypes must contain atleast 1 type");

        boolean containsKoulutustoimijaOrOppilaitos =  context.getOrgTypes().contains(OrganisaatioTyyppi.KOULUTUSTOIMIJA) || context.getOrgTypes().contains(OrganisaatioTyyppi.OPPILAITOS);

        if(containsKoulutustoimijaOrOppilaitos) {
            return checkAccess(ophOid, ROLE_CRUD);
        } else {
            return userCanUpdateOrganisation(context);
        }
    }

    /**
     * Is user allowed to move organisation.
     * @param context
     * @return
     */
    public boolean userCanMoveOrganisation(OrganisaatioContext context) {
        if (checkAccess(context.getOrgOid(), ROLE_CRUD)) {
            if (!context.getOrgTypes().contains(OrganisaatioTyyppi.MUU_ORGANISAATIO)
                    && !context.getOrgTypes().contains(OrganisaatioTyyppi.KOULUTUSTOIMIJA)) {
                return true;
            }
        }

        // implicitly oph user can delete whatever, is this true?
        return checkAccess(ophOid, ROLE_CRUD);
    }

    public boolean userCanEditYhteystietojenTyypit() {
        return checkAccess(ophOid, ROLE_RU, ROLE_CRUD);
    }

    public boolean userCanDeleteYhteystietojenTyyppi() {
        return checkAccess(ophOid, ROLE_CRUD);
    }

    private boolean userCanCreateDeleteRyhma() {
        return checkAccess(new String[]{ROLE_RYHMA}) || checkAccess(ophOid, ROLE_CRUD);
    }

    private boolean userCanEditRyhma() {
        return checkAccess(new String[]{ROLE_RYHMA}) || checkAccess(ophOid, ROLE_CRUD, ROLE_RU);
    }

    public boolean isReadAccessToAll() {
        return userCanReadOrganisation(ophOid);
    }
}
