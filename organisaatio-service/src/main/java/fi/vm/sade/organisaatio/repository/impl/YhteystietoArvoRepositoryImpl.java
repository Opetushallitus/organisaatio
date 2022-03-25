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
import fi.vm.sade.organisaatio.model.YhteystietojenTyyppi;
import fi.vm.sade.organisaatio.repository.YhteystietoArvoRepositoryCustom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
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

    @Override
    public YhteystietoArvo findByOrganisaatioAndNimi(String organisaatioOid, String nimi) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<YhteystietoArvo> query = cb.createQuery(YhteystietoArvo.class);

        Root<YhteystietoArvo> root = query.from(YhteystietoArvo.class);
        query.select(root);

        Predicate organisaatioEquals = cb.equal(root.get("organisaatio").get("oid"), organisaatioOid);
        Predicate nameEquals = cb.equal(root.get("kentta").get("nimi"), nimi);

        Predicate whereClause = cb.and(organisaatioEquals, nameEquals);
        query.where(whereClause);

        return em.createQuery(query).getSingleResult();

    }

    /**
     * Returns yhteystietoarvos for a given yhteystietojen tyyppi
     *
     * @param yhteystietojenTyyppi the yhteystietojen tyyppi given
     * @return the yhteystietoarvo objects matching the given yhteystietojen tyyppi
     */
    @Override
    public List<YhteystietoArvo> findByYhteystietojenTyyppi(
            YhteystietojenTyyppi yhteystietojenTyyppi) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<YhteystietoArvo> query = cb.createQuery(YhteystietoArvo.class);

        Root<YhteystietoArvo> root = query.from(YhteystietoArvo.class);
        query.select(root);

        Predicate yhteystietojenTyyppiEquals = cb.equal(root.get("kentta").get("yhteystietojenTyyppi").get("oid"), yhteystietojenTyyppi.getOid());
        query.where(yhteystietojenTyyppiEquals);

        return em.createQuery(query).getResultList();
    }

}
