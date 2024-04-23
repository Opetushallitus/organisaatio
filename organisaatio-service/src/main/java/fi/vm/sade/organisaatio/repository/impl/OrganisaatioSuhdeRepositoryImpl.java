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

package fi.vm.sade.organisaatio.repository.impl;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.model.OrganisaatioSuhde;
import fi.vm.sade.organisaatio.model.QOrganisaatio;
import fi.vm.sade.organisaatio.model.QOrganisaatioSuhde;
import fi.vm.sade.organisaatio.repository.OrganisaatioSuhdeRepositoryCustom;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author mlyly
 */
@Slf4j
@Repository
public class OrganisaatioSuhdeRepositoryImpl extends AbstractRepository implements OrganisaatioSuhdeRepositoryCustom {

    @Autowired
    EntityManager em;
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

        log.info("findParentTo({}, {})", childId, atTime);

        QOrganisaatioSuhde qSuhde = QOrganisaatioSuhde.organisaatioSuhde;
        QOrganisaatio qOrganisaatio = QOrganisaatio.organisaatio;

        BooleanExpression historiaExpression = qSuhde.suhdeTyyppi.eq(OrganisaatioSuhde.OrganisaatioSuhdeTyyppi.HISTORIA);
        BooleanExpression alkuExpression = qSuhde.alkuPvm.eq(atTime).or(qSuhde.alkuPvm.before(atTime));
        BooleanExpression loppuExpression = qSuhde.loppuPvm.isNull().or(qSuhde.loppuPvm.after(atTime));
        BooleanExpression expression = qSuhde.child.id.eq(childId).and(historiaExpression).and(alkuExpression).and(loppuExpression);

        List<OrganisaatioSuhde> suhteet = jpa().from(qSuhde)
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

        log.info("findChildrenTo({}, {})", parentId, atTime);

        QOrganisaatio qOrganisaatio = QOrganisaatio.organisaatio;
        QOrganisaatioSuhde qSuhde = QOrganisaatioSuhde.organisaatioSuhde;

        BooleanExpression historiaExpression = qSuhde.suhdeTyyppi.eq(OrganisaatioSuhde.OrganisaatioSuhdeTyyppi.HISTORIA);
        BooleanExpression alkuExpression = qSuhde.alkuPvm.eq(atTime).or(qSuhde.alkuPvm.before(atTime));
        BooleanExpression loppuExpression = qSuhde.loppuPvm.isNull().or(qSuhde.loppuPvm.after(atTime));
        BooleanExpression expression = qSuhde.parent.id.eq(parentId).and(historiaExpression).and(alkuExpression).and(loppuExpression);

        List<OrganisaatioSuhde> suhteet = jpa().from(qSuhde)
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

    @Override
    public List<OrganisaatioSuhde> findForDay(Date day) {
        if (day == null) {
            return new ArrayList<>();
        }

        QOrganisaatioSuhde organisaatioSuhde = QOrganisaatioSuhde.organisaatioSuhde;
        JPAQuery<OrganisaatioSuhde> query = jpa()
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

        return jpa().from(qSuhde)
                .where(expression)
                .orderBy(qSuhde.alkuPvm.desc())
                .select(qSuhde).fetch();
    }

}
