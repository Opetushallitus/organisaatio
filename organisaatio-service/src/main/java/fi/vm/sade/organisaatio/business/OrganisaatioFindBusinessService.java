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

import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.model.OrganisaatioSuhde;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 *
 * @author simok
 */
public interface OrganisaatioFindBusinessService {

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

    public List<Organisaatio> findByOids(Collection<String> oids);

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
     * @return Ryhmät
     */
    public List<Organisaatio> findGroups();

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

}
