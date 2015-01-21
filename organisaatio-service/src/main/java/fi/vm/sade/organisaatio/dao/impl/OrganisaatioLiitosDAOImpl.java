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

package fi.vm.sade.organisaatio.dao.impl;

import fi.vm.sade.generic.dao.AbstractJpaDAOImpl;
import fi.vm.sade.organisaatio.model.Organisaatio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.mysema.query.jpa.impl.JPAQuery;
import fi.vm.sade.organisaatio.dao.OrganisaatioDAO;
import fi.vm.sade.organisaatio.dao.OrganisaatioLiitosDAO;
import fi.vm.sade.organisaatio.model.OrganisaatioLiitos;
import fi.vm.sade.organisaatio.model.QOrganisaatioLiitos;

import java.util.Date;
import java.util.List;

/**
 * @author simok
 */
@Repository
public class OrganisaatioLiitosDAOImpl extends AbstractJpaDAOImpl<OrganisaatioLiitos, Long> implements OrganisaatioLiitosDAO {

    private static final Logger LOG = LoggerFactory.getLogger(OrganisaatioLiitosDAOImpl.class);

    @Autowired(required = true)
    OrganisaatioDAO organisaatioDAO;

    private void logRelation(String message, OrganisaatioLiitos relation) {
        if (relation == null) {
            LOG.info("  {} - NULL", message);
        } else {
            LOG.info("  {} --> pId={}, cId={}, aPvm={}",
                    new Object[]{message, relation.getOrganisaatio().getId(), relation.getKohde().getId(),
                            relation.getAlkuPvm()});
        }
    }

    @Override
    public OrganisaatioLiitos addLiitos(Long organisaatioId, Long kohdeId, Date startingFrom) {
        LOG.info("addLiitos({}, {}, {})", new Object[]{organisaatioId, kohdeId, startingFrom});

        if (organisaatioId == null || kohdeId == null) {
            throw new IllegalArgumentException();
        }
        if (startingFrom == null) {
            startingFrom = new Date();
        }

        //
        // Create the new relation
        //
        Organisaatio organisaatio = organisaatioDAO.read(organisaatioId);
        Organisaatio kohde        = organisaatioDAO.read(kohdeId);

        OrganisaatioLiitos liitosRelation = new OrganisaatioLiitos();
        liitosRelation.setAlkuPvm(startingFrom);
        liitosRelation.setOrganisaatio(organisaatio);
        liitosRelation.setKohde(kohde);

        liitosRelation = insert(liitosRelation);

        logRelation("  luotiin uusi liitos: ", liitosRelation);

        return liitosRelation;
    }

    @Override
    public List<OrganisaatioLiitos> findLiitokset(Long kohdeId) {
        if (kohdeId == null) {
            throw new IllegalArgumentException("kohdeId == null");
        }

        LOG.info("findLiitokset({})", kohdeId);

        QOrganisaatioLiitos qLiitos = QOrganisaatioLiitos.organisaatioLiitos;

        List<OrganisaatioLiitos> liitokset = new JPAQuery(getEntityManager()).from(qLiitos)
                .where(qLiitos.kohde.id.eq(kohdeId))
                .orderBy(qLiitos.alkuPvm.desc())
                .list(qLiitos);

        return liitokset;
    }

    @Override
    public List<OrganisaatioLiitos> findLiittynyt(Long organisaatioId) {
        if (organisaatioId == null) {
            throw new IllegalArgumentException("organisaatioId == null");
        }

        LOG.info("findLiittynyt({})", organisaatioId);

        QOrganisaatioLiitos qLiitos = QOrganisaatioLiitos.organisaatioLiitos;

        List<OrganisaatioLiitos> liitokset = new JPAQuery(getEntityManager()).from(qLiitos)
                .where(qLiitos.organisaatio.id.eq(organisaatioId))
                .orderBy(qLiitos.alkuPvm.desc())
                .list(qLiitos);

        return liitokset;
    }

}
