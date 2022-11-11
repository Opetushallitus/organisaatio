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

import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.model.YhteystietoArvo;
import fi.vm.sade.organisaatio.repository.YhteystietoArvoRepositoryCustom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.Collections;
import java.util.List;

@Repository
public class YhteystietoArvoRepositoryImpl implements YhteystietoArvoRepositoryCustom {

    @Autowired
    EntityManager em;

    @SuppressWarnings("unchecked")
    @Override
    public List<YhteystietoArvo> findByOrganisaatio(Organisaatio org) {
        if (org.getId() == null) {
            return Collections.emptyList();
        }
        return em.createQuery("FROM YhteystietoArvo WHERE organisaatio=:organisaatio")
                .setParameter("organisaatio", org)
                .getResultList();
    }
}
