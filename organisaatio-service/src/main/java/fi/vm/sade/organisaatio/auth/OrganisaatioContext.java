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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import fi.vm.sade.organisaatio.api.model.types.OrganisaatioDTO;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.api.search.OrganisaatioPerustieto;
import fi.vm.sade.organisaatio.resource.dto.OrganisaatioRDTO;

public class OrganisaatioContext {

    private OrganisaatioDTO dto;
    private OrganisaatioRDTO rdto;
    private OrganisaatioPerustieto perus;
    private final String orgOid;
    private final Set<OrganisaatioTyyppi> orgTypes;

    public String getOrgOid() {
        return orgOid;
    }

    public Set<OrganisaatioTyyppi> getOrgTypes() {
        return Collections.unmodifiableSet(orgTypes);
    }

    private String getNimi() {
        if (dto != null)
            return dto.getNimi().getTeksti().get(0).getValue();
        if (rdto != null)
            return rdto.getNimi().values().iterator().next();
        return (perus != null) ? perus.getNimi("fi") : null;
    }

    private OrganisaatioContext(OrganisaatioRDTO org) {
        this.orgOid = org != null ? org.getOid() : null;
        this.orgTypes = new HashSet<OrganisaatioTyyppi>(org != null ? org.getTyypit() : Collections.EMPTY_SET);
        this.rdto = org;
    }

    private OrganisaatioContext(OrganisaatioDTO org) {
        this.orgOid = org != null ? org.getOid() : null;
        this.orgTypes = new HashSet<OrganisaatioTyyppi>(org != null ? org.getTyypit() : Collections.EMPTY_SET);
        this.dto = org;
    }

    private OrganisaatioContext(String orgOid) {
        this.orgOid = orgOid;
        this.orgTypes = Collections.EMPTY_SET;
    }

    private OrganisaatioContext(OrganisaatioPerustieto org) {
        this.orgOid = org != null ? org.getOid() : null;
        this.orgTypes = new HashSet<OrganisaatioTyyppi>(org != null ? org.getOrganisaatiotyypit() : Collections.EMPTY_SET);
        this.perus = org;
    }

    public static OrganisaatioContext get(OrganisaatioDTO organisaatio) {
        return new OrganisaatioContext(organisaatio);
    }

    public static OrganisaatioContext get(String oid) {
        return new OrganisaatioContext(oid);
    }

    public static OrganisaatioContext get(OrganisaatioPerustieto organisaatio) {
        return new OrganisaatioContext(organisaatio);
    }

    public static OrganisaatioContext get(OrganisaatioRDTO organisaatio) {
        return new OrganisaatioContext(organisaatio);
    }

    @Override
    public String toString() {
        return "org:" + getNimi() + " oid:" + orgOid + ", orgTypes:" + orgTypes;
    }
}
