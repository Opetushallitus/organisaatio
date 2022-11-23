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
package fi.vm.sade.organisaatio.repository.impl;

import fi.vm.sade.organisaatio.model.YtjPaivitysLoki;
import fi.vm.sade.organisaatio.repository.YtjPaivitysLokiRepositoryCustom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.Date;
import java.util.List;

@Repository
public class YtjPaivitysLokiRepositoryImpl implements YtjPaivitysLokiRepositoryCustom {

    @Autowired
    EntityManager em;

    @Override
    public List<YtjPaivitysLoki> findByDateRange(Date alkupvm, Date loppupvm) {
        Query query = em.createQuery("SELECT loki FROM YtjPaivitysLoki loki WHERE loki.paivitysaika BETWEEN :alkupvm AND :loppupvm");
        query.setParameter("alkupvm", alkupvm);
        query.setParameter("loppupvm", loppupvm);
        return query.getResultList();
    }

    @Override
    public List<YtjPaivitysLoki> findLatest(int limit) {
        Query query = em.createQuery("SELECT loki FROM YtjPaivitysLoki loki ORDER BY loki.paivitysaika DESC");
        query.setFirstResult(0);
        query.setMaxResults(limit);
        return query.getResultList();
    }
}
