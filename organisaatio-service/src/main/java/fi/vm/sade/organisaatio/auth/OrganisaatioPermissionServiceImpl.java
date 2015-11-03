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

import fi.vm.sade.generic.service.AbstractPermissionService;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.api.search.OrganisaatioPerustieto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;

@Component
@Configurable
public class OrganisaatioPermissionServiceImpl extends AbstractPermissionService {

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
    // XXX case työelämäjärjestö
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
            if (!context.getOrgTypes().contains(OrganisaatioTyyppi.MUU_ORGANISAATIO) && !context.getOrgTypes().contains(OrganisaatioTyyppi.TYOELAMAJARJESTO)) {
                return true;
            }
        }

        // implicitly oph crud user can edit whatever, is this true?
        if (checkAccess(ophOid, ROLE_CRUD)){
            return true;
        }

        //oph ru can edit everything else but muu organisaatio (OVT-4755)
        if (checkAccess(ophOid, ROLE_RU)){
            if (!context.getOrgTypes().contains(OrganisaatioTyyppi.MUU_ORGANISAATIO) && !context.getOrgTypes().contains(OrganisaatioTyyppi.TYOELAMAJARJESTO)) {
                return true;
            }
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
        if (checkAccess(context.getOrgOid(), ROLE_CRUD)) {
            if (context.getOrgTypes().contains(
                    OrganisaatioTyyppi.KOULUTUSTOIMIJA)) {
                //organisaatio jonka alle luodaan on tyyppiä koulutustoimija, tarkistetaan saako käyttäjä luoda oppilaitoksen
                if (!userCanCreateOrganisationOfType(OrganisaatioTyyppi.OPPILAITOS)) {
                    return false;
                }
            }
            return true;
        }

        // implicitly oph user can create whatever, is this true?

        return checkAccess(ophOid, ROLE_CRUD);
    }


    /**
     * Vain oph virkailija voi muuttaa oppilaitoksen nimeä
     * @param context
     * @return
     */
    public boolean userCanEditName(OrganisaatioContext context) {
        if(context.getOrgTypes().contains(OrganisaatioTyyppi.OPPILAITOS) || context.getOrgTypes().contains(OrganisaatioTyyppi.KOULUTUSTOIMIJA) || context.getOrgTypes().contains(OrganisaatioTyyppi.MUU_ORGANISAATIO)
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
        case TYOELAMAJARJESTO:
            //only oph
            return checkAccess(ophOid, ROLE_CRUD);
        case OPPISOPIMUSTOIMIPISTE:
        case TOIMIPISTE:
            return checkAccess(new String[]{ROLE_CRUD}) || checkAccess(ophOid, ROLE_CRUD);

        default:
            log.error("Unhandled or type:" + tyyppi + " returning false!");
            break;
        }

        return false;
    }


    public boolean userCanDeleteOrganisation(OrganisaatioContext context) {
        /*
         * Käyttöoikeudet huomioitava: Vain OPH:n pääkäyttäjä voi poistaa
         * koulutuksen järjestäjän tai muun organisaation. Muut virkailijat
         * voivat poistaa oman organisaatiotasonsa alapuolisia organisaatioita.
         */

        //OVT-4508
//        if (checkAccess(context.getOrgOid(), ROLE_CRUD)) {
//            if (!context.getOrgTypes().contains(OrganisaatioTyyppi.MUU_ORGANISAATIO)
//                    && !context.getOrgTypes().contains(OrganisaatioTyyppi.KOULUTUSTOIMIJA) && !context) {
//                return true;
//            }
//        }

        // implicitly oph user can delete whatever, is this true?
        return checkAccess(ophOid, ROLE_CRUD);
    }

    public boolean userCanUpdateYTJ() {
        // TODO only OPH user can update??
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
     * Is user allowed to create "root" organisation.
     * @return
     */
    public boolean userCanCreateRootOrganisation() {
        OrganisaatioPerustieto root = new OrganisaatioPerustieto();
        root.setOid(ophOid);
        root.setNimi("fi", "ROOT");
        OrganisaatioContext rootContext = OrganisaatioContext.get(root);
        return userCanCreateOrganisation(rootContext);
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

    //TODO there's no spec about who can edit yhteystiedot
    public boolean userCanEditYhteystietojenTyypit() {
        return checkAccess(ophOid, ROLE_RU, ROLE_CRUD);
    }

    //TODO there's no spec about who can edit yhteystiedot
    public boolean userCanDeleteYhteystietojenTyyppi() {
        return checkAccess(ophOid, ROLE_CRUD);
    }

    public boolean userCanEditOppilaitostyyppi() {
        return userCanCreateRootOrganisation();
    }

    public boolean userCanEditOlkoodi() {
        return userCanCreateRootOrganisation();
    }
}
