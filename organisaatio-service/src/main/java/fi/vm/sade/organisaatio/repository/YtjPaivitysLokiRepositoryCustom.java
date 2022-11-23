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

import fi.vm.sade.organisaatio.model.YtjPaivitysLoki;

import java.util.Date;
import java.util.List;

public interface YtjPaivitysLokiRepositoryCustom {

    /**
     * Haetaan YTJ-massapäivityksen statukset annetulta aikaväliltä
     *
     * @param alkupvm alkupäivämäärä
     * @param loppupvm alkupäivämäärä
     * @return Annetun aikavälit massapäivitystilanteet listana
     */
    List<YtjPaivitysLoki> findByDateRange(Date alkupvm, Date loppupvm);

    List<YtjPaivitysLoki> findLatest(int limit);
}
