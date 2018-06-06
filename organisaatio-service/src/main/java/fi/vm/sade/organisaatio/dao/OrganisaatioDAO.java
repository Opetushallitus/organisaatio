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
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.dto.v3.OrganisaatioRDTOV3;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.model.dto.OrgPerustieto;
import fi.vm.sade.organisaatio.model.dto.OrgStructure;
import fi.vm.sade.organisaatio.dto.mapping.RyhmaCriteriaDto;
import fi.vm.sade.organisaatio.service.search.SearchCriteria;

import javax.persistence.EntityManager;
import javax.persistence.OptimisticLockException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *
 * @author simok
 */
public interface OrganisaatioDAO extends JpaDAO<Organisaatio, Long> {

    /**
     * Palauttaa hakukriteerien mukaiset organisaatiot (ei ryhmiä).
     *
     * @param criteria hakukriteerit
     * @param now nykyhetki
     * @return organisaatiot
     */
    List<Organisaatio> findBy(SearchCriteria criteria, Date now);

    /**
     * Palauttaa organisaatioiden aktiivisten aliorganisaatioiden lukumäärät.
     *
     * @param oids organisaation oid
     * @param now nykyhetki
     * @return aliorganisaatioiden lukumäärät
     */
    Map<String, Long> countActiveChildrenByOid(Collection<String> oids, Date now);

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
    List<Organisaatio> findBySearchCriteria(
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
    Organisaatio findByOid(String oid);

    List<OrganisaatioRDTOV3> findByOids(Collection<String> oids);

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
     * @param criteria hakukriteerit
     * @return
     */
    List<Organisaatio> findGroups(RyhmaCriteriaDto criteria);

    /**
     * Tarkistetaan onko annetulle ytunnukselle olemassa jo aktiivinen organisaatio.
     *
     * @param ytunnus
     * @return
     */
    boolean isYtunnusAvailable(String ytunnus);

    /**
     * Merkitään organisaatio poistetuksi ja palautetaan organisaation parent.
     * Organisaation voi poistaa vain, jos sillä ei ole lapsiorganisaatioita.
     *
     * @param oid Poistettavan organisaation oid
     * @return Poistettavan organisaation parent
     */
    Organisaatio markRemoved(String oid);

    /**
     * Finds list of oids with given query params.
     *
     * @param requireYtunnus
     * @param count
     * @param startIndex
     * @param type
     * @return
     */
    List<String> findOidsBy(Boolean requireYtunnus, int count, int startIndex, OrganisaatioTyyppi type);

    Organisaatio findByVirastoTunnus(String oid);

    Organisaatio findByYTunnus(String oid);

    Organisaatio findByOppilaitoskoodi(String oid);

    Organisaatio findByToimipistekoodi(String oid);

    List<OrgStructure> getOrganizationStructure(List<String> oids);

    /**
     * Find childers for given Organisation with OID.
     *
     * @param parentOid
     * @param myosPoistetut if true return also "removed" orgs
     * @param myosLakkautetut
     * @return
     */
    List<Organisaatio> findChildren(String parentOid, boolean myosPoistetut, boolean myosLakkautetut);

    List<OrgPerustieto> findBySearchCriteriaExact(String orgTyyppi, String oppilaitosTyyppi, String kunta, String searchStr, boolean suunnitellut, boolean lakkautetut, int maxResults, List<String> oids);

    List<Organisaatio> findDescendantsByOidList(List<String> oidList, int maxResults);

    /**
     * Return OID list of all organizations.
     *
     * @param myosPoistetut
     * @return
     */
    Collection<String> findAllOids(boolean myosPoistetut);

    /**
     * List OIDs of descendants for a given parent OID.
     *
     * @param parentOid
     * @param myosPoistetut
     * @return
     */
    Collection<String> listDescendantOids(String parentOid, boolean myosPoistetut);
    /***
     * Palauttaa annetun päivän jälkeen muuttuneet organisaatiot
     *
     * @param lastModifiedSince päivämäärä
     * @return
     */
    List<Organisaatio> findModifiedSince(Date lastModifiedSince);

    /**
     * Implementation of merge without flush, let hibernate decide when
     * @param org
     * @throws OptimisticLockException
     */
    void updateOrg(Organisaatio org) throws OptimisticLockException;

    void flush();

    EntityManager getJpaEntityManager();

}
