/*
* Copyright (c) 2014 The Finnish Board of Education - Opetushallitus
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

package fi.vm.sade.organisaatio.repository;

import fi.vm.sade.organisaatio.model.MonikielinenTeksti;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.model.OrganisaatioNimi;

import java.util.Date;
import java.util.List;

/**
 *
 * @author simok
 */
public interface OrganisaatioNimiRepositoryCustom {

    /**
     * Luodaan uusi nimi organisaatiolle annetuilla tiedoilla.
     *
     * @param organisaatio
     * @param nimi
     * @param alkuPvm
     * @param paivittaja
     * @return Luotu OrganisaatioNimi
     */
    OrganisaatioNimi addNimi(Organisaatio organisaatio, MonikielinenTeksti nimi, Date alkuPvm, String paivittaja);

    /**
     * Haetaan annetun organisaation nimet
     *
     * @param organisaatio
     * @return Annetun organisaation nimihistoria listana
     */
    List<OrganisaatioNimi> findNimet(Organisaatio organisaatio);

    /**
     * Haetaan annetun organisaation nimet
     *
     * @param organisaatioOid
     * @return Annetun organisaation nimihistoria listana
     */
    List<OrganisaatioNimi> findNimet(String organisaatioOid);

    /**
     * Haetaan annetun organisaation nimi, annetulla nimen voimassaolon alkupäivämäärällä.
     *
     * @param organisaatio
     * @param alkuPvm
     * @return
     */
    public OrganisaatioNimi findNimi(Organisaatio organisaatio, Date alkuPvm);

    /**
     * Haetaan annetun organisaation (oid) nimi, annetulla nimen voimassaolon alkupäivämäärällä.
     *
     * @param organisaatioOid
     * @param alkuPvm
     * @return
     */
    public OrganisaatioNimi findNimi(String organisaatioOid, Date alkuPvm);

    /**
     * Haetaan annetun organisaation nykyinen nimi.
     *
     * @param organisaatio
     * @return
     */
    public OrganisaatioNimi findCurrentNimi(Organisaatio organisaatio);

    /**
     * Haetaan annetun organisaation (oid) nykyinen nimi.
     *
     * @param organisaatioOid
     * @return
     */
    public OrganisaatioNimi findCurrentNimi(String organisaatioOid);

    /**
     * Haetaan organisaatiot, joiden nimi ei ole sama kuin nimihistorian current nimi.
     *
     * @return
     */
    public List<Organisaatio> findNimiNotCurrentOrganisaatiot();

}