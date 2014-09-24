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
package fi.vm.sade.organisaatio.dao;

import fi.vm.sade.generic.dao.JpaDAO;
import fi.vm.sade.organisaatio.model.Organisaatio;
import java.util.List;

/**
 *
 * @author simok
 */
public interface OrganisaatioDAO extends JpaDAO<Organisaatio, Long> {

    /**
     * Haetaan organisaatioita annetuilla hakukriteereillä
     *
     * @param kieliList kielirajaus kielivalikoima-koodiston koodiUreja: ["kielivalikoima_en", "kielivalikoima_sv"]
     * @param kuntaList kuntarajaus kunta-koodiston koodiUreja: ["kunta_905", "kunta_401"]
     * @param oppilaitostyyppiList oppilaitostyyppirajaus oppilaitostyyppi-koodiston koodiUreja: ["oppilaitostyyppi_19", "oppilaitostyyppi_91"]
     * @param vuosiluokkaList vuosiluokkarajaus vuosiluokat-koodiston koodiUreja: ["vuosiluokat_1","vuosiluokat_2"]
     * @param ytunnusList y-tunnusrajaus: ["0147510-4", "0203797-4"]
     * @param oidList
     * @param limit hakutuloksen määrän rajoite
     * @return
     */
    public List<Organisaatio> findBySearchCriteria(
            List<String> kieliList,
            List<String> kuntaList,
            List<String> oppilaitostyyppiList,
            List<String> vuosiluokkaList,
            List<String> ytunnusList,
            List<String> oidList,
            int limit);

    /**
     * Haetaan organisaatiota oidin perusteella.
     *
     * @param oid
     * @return
     */
    public Organisaatio findByOid(String oid);

    /**
     * Haetaan organisaatioita oidlistan perusteella.
     *
     * @param oidList
     * @param maxResults
     * @return
     */
    List<Organisaatio> findByOidList(List<String> oidList, int maxResults);

    /**
     * Return parent org oids to org, optimized for the auth use.
     *
     * Parents are returned in "root first" order.
     * <pre>
     * Example: a (b c (f g)) (d e)
     * findParentsTo(g) -> a c
     * </pre>
     *
     * @param oid
     * @return
     */
    List<Organisaatio> findParentsTo(String oid);

    /**
     * Haetaan annetun parent organisaation lapset.
     *
     * @param parentId
     * @return
     */
    List<Organisaatio> findChildren(Long parentId);

    /**
     * Palautetaan oph organisaation alla olevat Ryhmä tyyppiset organisaatiot
     *
     * @return
     */
    public List<Organisaatio> findGroups();

    /**
     * Tarkistetaan onko annetulle ytunnukselle olemassa jo aktiivinen organisaatio.
     *
     * @param ytunnus
     * @return
     */
    boolean isYtunnusAvailable(String ytunnus);

}
