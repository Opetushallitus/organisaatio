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

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import fi.vm.sade.generic.dao.AbstractJpaDAOImpl;
import fi.vm.sade.organisaatio.dao.OrganisaatioDAO;
import fi.vm.sade.organisaatio.dao.OrganisaatioSuhdeDAO;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.model.OrganisaatioSuhde;
import fi.vm.sade.organisaatio.model.QOrganisaatio;
import fi.vm.sade.organisaatio.model.QOrganisaatioSuhde;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author mlyly
 */
@Repository
public class OrganisaatioSuhdeDAOImpl extends AbstractJpaDAOImpl<OrganisaatioSuhde, Long> implements OrganisaatioSuhdeDAO {

    private static final Logger LOG = LoggerFactory.getLogger(OrganisaatioSuhdeDAOImpl.class);

    @Autowired(required = true)
    OrganisaatioDAO organisaatioDAO;

    // USE CASES:
    //
    // TIME 1. New child, B for A
    // data:
    // A B 1 - *
    //
    // TIME 2. New child, C for A
    // data:
    // A B 1 -
    // A C 2 - *
    //
    // TIME 3. New child, E for D
    // data:
    // A B 1 -
    // A C 2 -
    // D E 3 - *
    //
    // TIME 4. Remove child E
    // data:
    // A B 1 -
    // A C 2 -
    // D E 3 4 *
    //
    // TIME 5. Move B to be child of C  (remove child B, add child B to C)
    // data:
    // A B 1 5 *
    // A C 2 -
    // D E 3 4
    // C B 5 - *
    //

    /**
     * @param childId
     * @param atTime  null == now
     * @return parent relation at given time - there can be only one.
     */
    @Override
    public OrganisaatioSuhde findParentTo(Long childId, Date atTime) {
        if (childId == null) {
            throw new IllegalArgumentException("childId cannot be null");
        }

        // Laitetaan tälle päivälle oikea kellonaika, löytyy tänä päivänä luodut organisaatiot
        Date currentTimeStamp = new Date();
        if (atTime == null) {
            atTime = currentTimeStamp;
        }

        LOG.info("findParentTo({}, {})", childId, atTime);

        QOrganisaatioSuhde qSuhde = QOrganisaatioSuhde.organisaatioSuhde;
        QOrganisaatio qOrganisaatio = QOrganisaatio.organisaatio;

        BooleanExpression historiaExpression = qSuhde.suhdeTyyppi.eq(OrganisaatioSuhde.OrganisaatioSuhdeTyyppi.HISTORIA);
        BooleanExpression alkuExpression = qSuhde.alkuPvm.eq(atTime).or(qSuhde.alkuPvm.before(atTime));
        BooleanExpression loppuExpression = qSuhde.loppuPvm.isNull().or(qSuhde.loppuPvm.after(atTime));
        BooleanExpression expression = qSuhde.child.id.eq(childId).and(historiaExpression).and(alkuExpression).and(loppuExpression);

        List<OrganisaatioSuhde> suhteet = new JPAQuery<>(getEntityManager()).from(qSuhde)
                .join(qSuhde.parent, qOrganisaatio).fetchJoin()
                .where(expression.and(qOrganisaatio.organisaatioPoistettu.isFalse()))
                .orderBy(qSuhde.alkuPvm.desc())
                .select(qSuhde)
                .fetch();

        if (suhteet != null && !suhteet.isEmpty()) {
            return suhteet.get(0);
        }
        return null;
    }

    /**
     * @param parentId
     * @param atTime   null == now
     * @return list of child relations at given time
     */
    @Override
    public List<OrganisaatioSuhde> findChildrenTo(Long parentId, Date atTime) {
        if (parentId == null) {
            throw new IllegalArgumentException("parentId == null");
        }

        // Laitetaan tälle päivälle oikea kellonaika, löytyy tänä päivänä luodut organisaatiot
        Date currentTimeStamp = new Date();
        if (atTime == null) {
            atTime = currentTimeStamp;
        }

        LOG.info("findChildrenTo({}, {})", parentId, atTime);

        QOrganisaatio qOrganisaatio = QOrganisaatio.organisaatio;
        QOrganisaatioSuhde qSuhde = QOrganisaatioSuhde.organisaatioSuhde;

        BooleanExpression historiaExpression = qSuhde.suhdeTyyppi.eq(OrganisaatioSuhde.OrganisaatioSuhdeTyyppi.HISTORIA);
        BooleanExpression alkuExpression = qSuhde.alkuPvm.eq(atTime).or(qSuhde.alkuPvm.before(atTime));
        BooleanExpression loppuExpression = qSuhde.loppuPvm.isNull().or(qSuhde.loppuPvm.after(atTime));
        BooleanExpression expression = qSuhde.parent.id.eq(parentId).and(historiaExpression).and(alkuExpression).and(loppuExpression);

        List<OrganisaatioSuhde> suhteet = new JPAQuery<>(getEntityManager()).from(qSuhde)
                .join(qSuhde.child, qOrganisaatio).fetchJoin()
                .where(expression.and(qOrganisaatio.organisaatioPoistettu.isFalse()))
                .orderBy(qSuhde.alkuPvm.desc())
                .select(qSuhde)
                .fetch();

        List<Long> foundChildIds = new ArrayList<>();
        List<OrganisaatioSuhde> result = new ArrayList<>();
        for (OrganisaatioSuhde curSuhde : suhteet) {
            Organisaatio parent = findParentTo(curSuhde.getChild().getId(), atTime).getParent();
            if (!(foundChildIds.contains(curSuhde.getChild().getId()))
                    && Objects.equals(parent.getId(), parentId)) {
                foundChildIds.add(curSuhde.getChild().getId());
                result.add(curSuhde);
            }
        }

        return result;
    }

    /**
     * If child has a "current" parent, this actually "moves" child under another parent.
     *
     * @param parentId
     * @param childId
     * @param startingFrom null == now
     * @return created relation
     */
    @Override
    public OrganisaatioSuhde addChild(Long parentId, Long childId, Date startingFrom, String opetuspisteenJarjNro) {
        LOG.info("addChild({}, {}, {})", new Object[]{parentId, childId, startingFrom});

        if (parentId == null || childId == null) {
            throw new IllegalArgumentException();
        }
        if (startingFrom == null) {
            startingFrom = new Date();
        }

        //
        // Create the new relation
        //
        Organisaatio parent = organisaatioDAO.read(parentId);
        Organisaatio child = organisaatioDAO.read(childId);

        OrganisaatioSuhde childRelation = new OrganisaatioSuhde();
        childRelation.setAlkuPvm(startingFrom);
        childRelation.setLoppuPvm(null);
        childRelation.setChild(child);
        childRelation.setParent(parent);
        childRelation.setOpetuspisteenJarjNro(opetuspisteenJarjNro);

        childRelation = insert(childRelation);

        logRelation("  Created new child relation: ", childRelation);

        return childRelation;
    }

    /**
     * Updates existing parent-child relation for give parent-child.
     * If parent is null ANY valid relation for child will be dated to be ended.
     *
     * @param parentId
     * @param childId
     * @param removalDate
     */
    @Override
    public void removeChild(Long parentId, Long childId, Date removalDate) {
        LOG.info("removeChild(pId={}, cId={}, t={})", new Object[]{parentId, childId, removalDate});

        if (removalDate == null) {
            removalDate = new Date();
        }

        // Get possible existing parent relation
        OrganisaatioSuhde parentRelation = findParentTo(childId, removalDate);

        if (parentRelation != null) {
            this.remove(parentRelation);
        }
    }

    @Override
    public OrganisaatioSuhde addLiitos(Organisaatio organisaatio, Organisaatio kohde, Date startingFrom) {
        LOG.info("addLiitos({}, {}, {})", new Object[]{organisaatio, kohde, startingFrom});

        if (organisaatio == null || kohde == null) {
            throw new IllegalArgumentException();
        }
        if (startingFrom == null) {
            startingFrom = new Date();
        }

        //
        // Create the new relation
        //
        OrganisaatioSuhde liitosRelation = new OrganisaatioSuhde();
        liitosRelation.setAlkuPvm(startingFrom);
        liitosRelation.setLoppuPvm(null);
        liitosRelation.setChild(organisaatio);
        liitosRelation.setParent(kohde);
        liitosRelation.setSuhdeTyyppi(OrganisaatioSuhde.OrganisaatioSuhdeTyyppi.LIITOS);

        liitosRelation = insert(liitosRelation);

        logRelation("  Created new liitos relation: ", liitosRelation);

        return liitosRelation;
    }


    @Override
    public List<OrganisaatioSuhde> findForDay(Date day) {
        if (day == null) {
            return new ArrayList<>();
        }

        QOrganisaatioSuhde organisaatioSuhde = QOrganisaatioSuhde.organisaatioSuhde;
        JPAQuery<OrganisaatioSuhde> query = new JPAQuery<>(getEntityManager())
                .from(organisaatioSuhde)
                .where(organisaatioSuhde.alkuPvm.eq(day))
                .orderBy(organisaatioSuhde.alkuPvm.asc())
                .select(organisaatioSuhde);
        return query.fetch();
    }

    @Override
    public List<OrganisaatioSuhde> findLiitokset(Boolean piilotettu, Date date) {
        QOrganisaatioSuhde qSuhde = QOrganisaatioSuhde.organisaatioSuhde;

        BooleanExpression expression = qSuhde.suhdeTyyppi.eq(OrganisaatioSuhde.OrganisaatioSuhdeTyyppi.LIITOS);

        if (date != null) {
            expression = expression.and(qSuhde.alkuPvm.eq(date).or(qSuhde.alkuPvm.after(date)));
        }

        if(piilotettu != null){
            expression = expression.and(qSuhde.parent.piilotettu.eq(piilotettu).or(qSuhde.child.piilotettu.eq(piilotettu)));
        }

        return new JPAQuery<>(getEntityManager()).from(qSuhde)
                .where(expression)
                .orderBy(qSuhde.alkuPvm.desc())
                .select(qSuhde).fetch();
    }

    private void logRelation(String message, OrganisaatioSuhde relation) {
        if (relation == null) {
            LOG.info("  {} - NULL", message);
        } else {
            LOG.info("  {} --> pId={}, cId={}, aPvm={}, lPvm={}",
                    new Object[]{message, relation.getParent().getId(), relation.getChild().getId(),
                            relation.getAlkuPvm(), relation.getLoppuPvm()});
        }
    }
}
