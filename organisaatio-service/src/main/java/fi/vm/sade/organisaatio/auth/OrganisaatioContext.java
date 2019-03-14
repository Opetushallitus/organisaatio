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

import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.api.search.OrganisaatioPerustieto;
import fi.vm.sade.organisaatio.dto.v3.OrganisaatioRDTOV3;
import fi.vm.sade.organisaatio.dto.v4.OrganisaatioRDTOV4;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.resource.dto.OrganisaatioRDTO;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class OrganisaatioContext {
    private OrganisaatioRDTO rdto;
    private OrganisaatioRDTOV3 rdtov3;
    private OrganisaatioRDTOV4 rdtov4;
    private OrganisaatioPerustieto perus;
    private final String orgOid;
    private final String parentOrgOid;
    private final Set<OrganisaatioTyyppi> orgTypes;

    private static Set<OrganisaatioTyyppi> getTyypitFromStrings(Set<String> tyypitStrs) {
        Set<OrganisaatioTyyppi> tyypit = new HashSet<>();
        for (String tyyppiStr : tyypitStrs) {
            tyypit.add(OrganisaatioTyyppi.fromValue(tyyppiStr));
        }
        return tyypit;
    }

    private static Set<OrganisaatioTyyppi> getTyypitFromKoodiStrings(Set<String> tyypitStrs) {
        Set<OrganisaatioTyyppi> tyypit = new HashSet<>();
        for (String tyyppiStr : tyypitStrs) {
            tyypit.add(OrganisaatioTyyppi.fromKoodiValue(tyyppiStr));
        }
        return tyypit;
    }

    public String getOrgOid() {
        return orgOid;
    }

    public String getParentOrgOid() {
        return parentOrgOid;
    }

    public Set<OrganisaatioTyyppi> getOrgTypes() {
        return Collections.unmodifiableSet(orgTypes);
    }

    public boolean isRyhma() {
        return orgTypes.size() == 1 && orgTypes.contains(OrganisaatioTyyppi.RYHMA);
    }

    private String getNimi() {
        if (rdto != null)
            return rdto.getNimi().values().iterator().next();
        if (rdtov3 != null)
            return rdtov3.getNimi().values().iterator().next();
        if (rdtov4 != null)
            return rdtov4.getNimi().values().iterator().next();
        return (perus != null) ? perus.getNimi("fi") : null;
    }

    private OrganisaatioContext(OrganisaatioRDTO org) {
        this.orgOid = org != null ? org.getOid() : null;
        this.parentOrgOid = org != null ? org.getParentOid() : null;
        this.orgTypes = new HashSet<>(org != null ? getTyypitFromStrings(org.getTyypit()) : Collections.emptySet());
        this.rdto = org;
    }

    private OrganisaatioContext(OrganisaatioRDTOV3 org) {
        this.orgOid = org != null ? org.getOid() : null;
        this.parentOrgOid = org != null ? org.getParentOid() : null;
        this.orgTypes = new HashSet<>(org != null ? getTyypitFromStrings(org.getTyypit()) : Collections.emptySet());
        this.rdtov3 = org;
    }

    private OrganisaatioContext(OrganisaatioRDTOV4 org) {
        this.orgOid = org != null ? org.getOid() : null;
        this.parentOrgOid = org != null ? org.getParentOid() : null;
        this.orgTypes = new HashSet<>(org != null ? getTyypitFromKoodiStrings(org.getTyypit()) : Collections.emptySet());
        this.rdtov4 = org;
    }

    private OrganisaatioContext(String orgOid) {
        this.orgOid = orgOid;
        this.parentOrgOid = null;
        this.orgTypes = new HashSet<>();
    }

    private OrganisaatioContext(OrganisaatioPerustieto org) {
        this.orgOid = org != null ? org.getOid() : null;
        this.parentOrgOid = org != null ? org.getParentOid() : null;
        this.orgTypes = new HashSet<>(org != null ? org.getOrganisaatiotyypit() : Collections.emptySet());
        this.perus = org;
    }

    private OrganisaatioContext(Organisaatio org) {
        this.orgOid = org != null ? org.getOid() : null;
        this.parentOrgOid = org != null ? org.getParentOid().orElse(null) : null;
        this.orgTypes = new HashSet<>(org != null ? getTyypitFromKoodiStrings(org.getTyypit()) : Collections.emptySet());
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

    public static OrganisaatioContext get(OrganisaatioRDTOV3 organisaatio) {
        return new OrganisaatioContext(organisaatio);
    }

    public static OrganisaatioContext get(OrganisaatioRDTOV4 organisaatio) {
        return new OrganisaatioContext(organisaatio);
    }

    public static OrganisaatioContext get(Organisaatio organisaatio) {
        return new OrganisaatioContext(organisaatio);
    }

    @Override
    public String toString() {
        return "org:" + getNimi() + " oid:" + orgOid + ", orgTypes:" + orgTypes;
    }
}
