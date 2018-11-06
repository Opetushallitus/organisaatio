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

package fi.vm.sade.organisaatio.business;

import fi.vm.sade.organisaatio.api.DateParam;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.dto.ChildOidsCriteria;
import fi.vm.sade.organisaatio.dto.v3.OrganisaatioRDTOV3;
import fi.vm.sade.organisaatio.dto.v4.OrganisaatioRDTOV4;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.model.OrganisaatioSuhde;
import fi.vm.sade.organisaatio.resource.dto.RyhmaCriteriaDtoV3;

import java.util.Collection;
import java.util.Date;
import java.util.List;

public interface OrganisaatioFindBusinessService {

    /**
     * Hakee v4 rajapinnan mukaiset organisaatiotiedot oidien perusteella
     * @param oids Organisaatioden oidit
     * @return V4 rajapinnan käyttämä organisaatiolista
     */
    List<OrganisaatioRDTOV4> findByOidsV4(Collection<String> oids);

    /**
     * Wrappaa findById(String id) mutta palauttaa v4 apin tuloksen
     * @param id findById(String id) vastaava hakuavain
     * @param includeImage Haetaanko kuvat
     * @return Organisaatio dto muodossa OrganisaatioRDTOV4
     */
    OrganisaatioRDTOV4 findByIdV4(String id, boolean includeImage);

    /**
     * Hakee organisaation ID:n perusteella ja palauttaa tämän lapsiorganisaatiot.
     * @param id Käyttää findById(String id)
     * @param includeImage Haetaanko kuvat
     * @return Organisaation aliorganisaatiot muodossa OrganisaatioRDTOV4
     */
    List<OrganisaatioRDTOV4> findChildrenById(String id, boolean includeImage);

    /**
     * Idllä haku (Id voi olla oid, y-tunnus, virastotunnus, oppilaitoskoodi, toimipistekoodi)
     *
     * Search order
     *  1. OID
     *  2. Y-TUNNUS
     *  3. VIRASTOTUNNUS
     *  4. OPPILAITOSKOODI
     *  5. TOIMIPISTEKOODI
     *
     * @param id Id, joka voi olla: oid, y-tunnus, virastotunnus, oppilaitoskoodi tai toimipistekoodi
     *
     * @return Organisaatio
     */
    public Organisaatio findById(String id);

    public List<OrganisaatioRDTOV3> findByOids(Collection<String> oids);

    /**
     * @param kieliList kielirajaus kielivalikoima-koodiston koodiUreja: ["kielivalikoima_en", "kielivalikoima_sv"]
     * @param kuntaList kuntarajaus kunta-koodiston koodiUreja: ["kunta_407", "kunta_604"]
     * @param oppilaitostyyppiList oppilaitostyyppirajaus oppilaitostyyppi-koodiston koodiUreja: ["oppilaitostyyppi_19", "oppilaitostyyppi_91"]
     * @param vuosiluokkaList vuosiluokkarajaus vuosiluokat-koodiston koodiUreja: ["vuosiluokat_1","vuosiluokat_2"]
     * @param ytunnusList y-tunnusrajaus: ["0147510-4", "0203797-4"]
     * @param oidList
     * @param limit
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
     * Haetaan kannasta kaikki ryhmät.
     * @param criteria hakukriteerit
     * @return Ryhmät
     */
    public List<Organisaatio> findGroups(RyhmaCriteriaDtoV3 criteria);

    /**
     * Haetaan kannasta organiasaatioiden oidit listana.
     * @param searchTerms
     * @param count Lukumäärä
     * @param startIndex Aloitusindeksi
     * @param type Haettava organisaatiotyyppi
     * @return Organisaatioiden oid:t
     */
    public List<String> findOidsBy(String searchTerms, int count, int startIndex, OrganisaatioTyyppi type);

    /**
     * Haetaan kannasta kaikki organisaatioliitokset.
     *
     * @param date
     * @return Organisaatioiden liitokset.
     */
    public List<OrganisaatioSuhde> findLiitokset(Date date);

    Collection<String> findChildOidsRecursive(ChildOidsCriteria criteria);

    /**
     * Hakee muuttuneet organisaatiot
     * @param lastModifiedSince Päivämäärä jonka jälkeen muuttuneita haetaan
     * @param includeImage Haetaanko kuvat
     * @return Muuttuneet organisaatiot muodossa OrganisaatioRDTOV4
     */
    List<OrganisaatioRDTOV4> haeMuutetut(DateParam lastModifiedSince, boolean includeImage);
}
