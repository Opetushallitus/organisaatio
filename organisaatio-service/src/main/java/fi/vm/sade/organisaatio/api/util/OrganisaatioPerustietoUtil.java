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


package fi.vm.sade.organisaatio.api.util;

import fi.vm.sade.organisaatio.api.search.OrganisaatioPerustieto;
import fi.vm.sade.organisaatio.dto.v4.OrganisaatioPerustietoV4;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 * @author simok
 */
public abstract class OrganisaatioPerustietoUtil {
    /**
     * Luo puumaisen organisaatiohierarkian. 
     * 
     * @param organisaatiot List organisaatioista, joista muodostetaan puu
     * @return Lista juuritason organisaatioista (lapset asetettu niiden alle)
     */
    public static Set<OrganisaatioPerustieto> createHierarchy(
            final List<OrganisaatioPerustieto> organisaatiot) {

        Map<String, OrganisaatioPerustieto> oidToOrgMap = organisaatiot.stream().collect(
                Collectors.toMap(OrganisaatioPerustieto::getOid, Function.identity()));

        //ORganisaatiot joilla eil ole isää:
        Set<OrganisaatioPerustieto> rootOrgs = new HashSet<>();

        for (OrganisaatioPerustieto curOrg : organisaatiot) {
            final String parentOid = curOrg.getParentOid();
            final OrganisaatioPerustieto parentOrg = oidToOrgMap.get(parentOid);
            if (parentOrg != null) {
                parentOrg.getChildren().add(curOrg);
            } else {
                rootOrgs.add(curOrg);
            }
        }

        return rootOrgs;
    }
    /**
     * Tarkistaa onko organisaatio passivoitu eli lakkautettu
     * @param o Organisaatio
     * @return true jos organisaatio on passiivinen, false muuten
     */
    public static boolean isPassive(OrganisaatioPerustieto o) {
        return o.getLakkautusPvm() != null && o.getLakkautusPvm().before(new Date());
    }

    /**
     * Tarkistaa onko organisaatio suunniteltu, eli alkupvm tulevaisuudessa
     * @param o Organisaatio
     * @return true jos organisaatio on suunniteltu, false muuten
     */
    public static boolean isSuunniteltu(OrganisaatioPerustieto o) {
        return o.getAlkuPvm() != null && o.getAlkuPvm().after(new Date());
    }

    /**
     * Tarkistaa onko organisaatio passivoitu eli lakkautettu
     * @param o Organisaatio
     * @return true jos organisaatio on passiivinen, false muuten
     */
    public static boolean isPassive(OrganisaatioPerustietoV4 o) {
        return o.getLakkautusPvm() != null && o.getLakkautusPvm().before(new Date());
    }

    /**
     * Tarkistaa onko organisaatio suunniteltu, eli alkupvm tulevaisuudessa
     * @param o Organisaatio
     * @return true jos organisaatio on suunniteltu, false muuten
     */
    public static boolean isSuunniteltu(OrganisaatioPerustietoV4 o) {
        return o.getAlkuPvm() != null && o.getAlkuPvm().after(new Date());
    }
}
