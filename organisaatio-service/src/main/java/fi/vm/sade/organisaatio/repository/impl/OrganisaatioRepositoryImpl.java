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

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.business.exception.OrganisaatioCrudException;
import fi.vm.sade.organisaatio.business.exception.OrganisaatioNotFoundException;
import fi.vm.sade.organisaatio.dto.ChildOidsCriteria;
import fi.vm.sade.organisaatio.dto.mapping.OrganisaatioNimiModelMapper;
import fi.vm.sade.organisaatio.dto.mapping.RyhmaCriteriaDto;
import fi.vm.sade.organisaatio.dto.v3.OrganisaatioRDTOV3;
import fi.vm.sade.organisaatio.model.*;
import fi.vm.sade.organisaatio.repository.OrganisaatioRepositoryCustom;
import fi.vm.sade.organisaatio.service.converter.v3.OrganisaatioToOrganisaatioRDTOV3ProjectionFactory;
import fi.vm.sade.organisaatio.service.search.SearchCriteria;
import org.hibernate.jpa.QueryHints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.types.dsl.Expressions.allOf;
import static com.querydsl.core.types.dsl.Expressions.anyOf;
import static fi.vm.sade.organisaatio.service.util.OptionalUtil.ifPresentOrElse;
import static fi.vm.sade.organisaatio.service.util.PredicateUtil.not;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * @author tommiha
 * @author mlyly
 */
@Repository
public class OrganisaatioRepositoryImpl implements OrganisaatioRepositoryCustom {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${root.organisaatio.oid}")
    private String ophOid;

    @Autowired
    EntityManager em;

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    private OrganisaatioNimiModelMapper organisaatioNimiModelMapper;

    @Value("${root.organisaatio.oid}")
    private String rootOrganisaatioOid;

    private static final String URI_WITH_VERSION_REG_EXP = "^.*#[0-9]+$";

    @Override
    public Collection<Organisaatio> findBy(SearchCriteria criteria) {
        QOrganisaatio qOrganisaatio = QOrganisaatio.organisaatio;
        StringPath qOrganisaatiotyyppi = Expressions.stringPath("tyyppi");
        QMonikielinenTeksti qNimi = new QMonikielinenTeksti("nimi");
        StringPath qNimiArvo = Expressions.stringPath("nimiArvo");
        StringPath qKieli = Expressions.stringPath("kieli");
        StringPath qParentOid = Expressions.stringPath("parentOid");

        JPAQuery<Organisaatio> query = new JPAQuery<>(em)
                .from(qOrganisaatio)
                .join(qOrganisaatio.tyypit, qOrganisaatiotyyppi).fetchJoin()
                .leftJoin(qOrganisaatio.nimi, qNimi).fetchJoin()
                .leftJoin(qNimi.values, qNimiArvo).fetchJoin()
                .leftJoin(qOrganisaatio.kielet, qKieli).fetchJoin()
                .leftJoin(qOrganisaatio.parentOids, qParentOid).fetchJoin()
                .select(qOrganisaatio);

        Optional.ofNullable(getStatusPredicate(criteria, qOrganisaatio)).ifPresent(query::where);

        Optional.ofNullable(criteria.getPoistettu()).ifPresent(poistettu
                -> query.where(qOrganisaatio.organisaatioPoistettu.eq(poistettu)));

        Optional.ofNullable(criteria.getYritysmuoto()).filter(not(Collection::isEmpty)).ifPresent(yritysmuodot
                -> query.where(qOrganisaatio.yritysmuoto.in(yritysmuodot)));

        Optional.ofNullable(criteria.getPiilotettu()).ifPresent(piilotettu
                -> query.where(qOrganisaatio.piilotettu.eq(piilotettu)));

        Optional.ofNullable(criteria.getKunta()).filter(not(Collection::isEmpty)).ifPresent(kunnat
                -> query.where(qOrganisaatio.muutKotipaikatUris.any().in(kunnat).or(qOrganisaatio.kotipaikka.in(kunnat))));

        ifPresentOrElse(Optional.ofNullable(criteria.getOrganisaatioTyyppi()).filter(not(Collection::isEmpty)), organisaatiotyypit -> {
            QOrganisaatio qOrganisaatio1 = new QOrganisaatio("organisaatio1");
            StringPath qOrganisaatiotyyppi1 = Expressions.stringPath("organisaatiotyyppi1");
            query.where(qOrganisaatio.in(JPAExpressions.selectFrom(qOrganisaatio1)
                    .join(qOrganisaatio1.tyypit, qOrganisaatiotyyppi1)
                    .where(qOrganisaatiotyyppi1.in(organisaatiotyypit))));
        }, () -> query.where(qOrganisaatiotyyppi.notIn("Ryhma")));

        Optional.ofNullable(criteria.getOppilaitosTyyppi()).filter(not(Collection::isEmpty)).ifPresent(oppilaitostyypit -> {
            BooleanBuilder predicate = oppilaitostyypit.stream()
                    .map(oppilaitostyyppi -> qOrganisaatio.oppilaitosTyyppi.like(oppilaitostyyppi.replace("*", "%")))
                    .reduce(new BooleanBuilder(), BooleanBuilder::or, BooleanBuilder::or);
            query.where(predicate);
        });

        Optional.ofNullable(criteria.getKieli()).filter(not(Collection::isEmpty)).ifPresent(kielet
                -> query.where(qKieli.in(kielet)));

        Optional.ofNullable(criteria.getOidRestrictionList()).filter(not(Collection::isEmpty)).ifPresent(oids -> {
            BooleanBuilder parentOidPathPredicate = oids.stream()
                    .map(qOrganisaatio.parentOids::contains)
                    .reduce(new BooleanBuilder(), BooleanBuilder::or, BooleanBuilder::or);
            query.where(qOrganisaatio.oid.in(oids).or(parentOidPathPredicate));
        });

        Optional.ofNullable(criteria.getSearchStr()).ifPresent(searchStr -> query.where(anyOf(
                qOrganisaatio.nimihaku.containsIgnoreCase(searchStr),
                qOrganisaatio.oid.contains(searchStr),
                qOrganisaatio.ytunnus.contains(searchStr),
                qOrganisaatio.oppilaitosKoodi.contains(searchStr)
        )));

        Optional.ofNullable(criteria.getOid()).filter(not(Collection::isEmpty)).ifPresent(oids
                -> query.where(qOrganisaatio.oid.in(oids)));

        Optional.ofNullable(criteria.getParentOids()).filter(not(Collection::isEmpty)).ifPresent(parentOids
                -> query.where(qOrganisaatio.parentOids.any().in(parentOids)));

        query.where(qOrganisaatio.oid.notEqualsIgnoreCase(rootOrganisaatioOid));
        return query.fetch();
    }

    private static com.querydsl.core.types.Predicate getStatusPredicate(SearchCriteria criteria, QOrganisaatio qOrganisaatio) {
        // Ei aktiivisia, suunniteltuja eikä lakkautettuja - tätä ei pitäisi tapahtua
        Date now = new Date();
        if (!criteria.getAktiiviset() && !criteria.getSuunnitellut() && !criteria.getLakkautetut()) {
            return allOf(
                    qOrganisaatio.alkuPvm.before(now),
                    qOrganisaatio.alkuPvm.after(now),
                    qOrganisaatio.lakkautusPvm.before(now)
            );
        }

        // Aktiiviset, Suunnitellut, Lakkautetut
        if (criteria.getAktiiviset() && criteria.getSuunnitellut() && criteria.getLakkautetut()) {
            // Ei päivämääräfiltteröintiä
            return null;
        }

        // Suunnitellut, Lakkautetut
        if (!criteria.getAktiiviset() && criteria.getSuunnitellut() && criteria.getLakkautetut()) {
            // Alkupvm tulevaisuudessa tai lakkautuspvm menneisyydessä
            return anyOf(qOrganisaatio.alkuPvm.after(now), qOrganisaatio.lakkautusPvm.before(now));
        }

        // Lakkautetut
        if (!criteria.getAktiiviset() && !criteria.getSuunnitellut() && criteria.getLakkautetut()) {
            // Haetaan mukaan kaikki lakkautetut - lakkautuspäivämäärä menneisyydessä
            return qOrganisaatio.lakkautusPvm.before(now);
        }

        BooleanBuilder booleanBuilder = new BooleanBuilder();

        // Alkupäivämäärän käsittely -otetaanko mukaan suunnitellut vai filtteröidäänkö ne ulos
        if (criteria.getSuunnitellut() && !criteria.getAktiiviset()) {
            // Filtteröidään pois aktiiviset - joiden alkupvm menneisyydessä
            booleanBuilder.and(qOrganisaatio.alkuPvm.after(now));
        } else if (!criteria.getSuunnitellut() && criteria.getAktiiviset()) {
            // Filtteröidään pois suunnitellut - joiden alkupvm tulevaisuudessa
            booleanBuilder.and(anyOf(qOrganisaatio.alkuPvm.isNull(), qOrganisaatio.alkuPvm.loe(now)));
        }

        // Loppupäivämäärän käsittely - otetaanko mukaan lakkautetut vai filtteröidäänkö ne ulos
        if (!criteria.getLakkautetut()) {
            // Filteröidään pois lakkautetut.
            booleanBuilder.and(anyOf(qOrganisaatio.lakkautusPvm.isNull(), qOrganisaatio.lakkautusPvm.goe(now)));
        }

        return booleanBuilder;
    }

    @Override
    public Map<String, Long> countActiveChildrenByOid(Date now) {
        QOrganisaatio qParent = new QOrganisaatio("parent");
        QOrganisaatioSuhde qOrganisaatioSuhde = QOrganisaatioSuhde.organisaatioSuhde;
        QOrganisaatio qChild = new QOrganisaatio("child");

        JPAQuery<Object> query = new JPAQuery<>(em)
                .from(qParent)
                .join(qParent.childSuhteet, qOrganisaatioSuhde)
                .join(qOrganisaatioSuhde.child, qChild)
                .where(qOrganisaatioSuhde.suhdeTyyppi.ne(OrganisaatioSuhde.OrganisaatioSuhdeTyyppi.LIITOS))
                .where(anyOf(qOrganisaatioSuhde.loppuPvm.isNull(), qOrganisaatioSuhde.loppuPvm.after(now)))
                .where(qChild.organisaatioPoistettu.isFalse())
                .where(anyOf(qChild.lakkautusPvm.isNull(), qChild.lakkautusPvm.after(now)));

        return query.groupBy(qParent.oid).transform(groupBy(qParent.oid).as(qChild.count()));
    }

    /**
     * Find the children of given parent organisation.
     *
     * @param parentId
     * @return
     */
    @Override
    public List<Organisaatio> findChildren(Long parentId) {
        logger.debug("findChildren({})", parentId);

        QOrganisaatio qOrganisaatio = QOrganisaatio.organisaatio;

        BooleanExpression whereExpression = qOrganisaatio.parentIdPath.endsWith("|" + parentId + "|");

        return new JPAQuery<>(em)
                .from(qOrganisaatio)
                .select(qOrganisaatio)
                .where(whereExpression)
                .distinct()
                .fetch();
    }

    /**
     * Find childers for given Organisation with OID.
     *
     * @param parentOid
     * @param myosPoistetut   if true return also "removed" orgs
     * @param myosLakkautetut
     * @return
     */
    @Override
    public List<Organisaatio> findChildren(String parentOid, boolean myosPoistetut, boolean myosLakkautetut) {
        logger.debug("findChildren({})", parentOid);

        Organisaatio parent = findByOids(List.of(parentOid), false).stream().findFirst().orElse(null);
        List<Organisaatio> result = new ArrayList<>();
        if (parent == null) {
            return result;
        }

        Date now = new Date();

        for (Organisaatio curOrg : findChildren(parent.getId())) {
            if ((myosPoistetut || !curOrg.isOrganisaatioPoistettu())
                    && (myosLakkautetut || curOrg.getLakkautusPvm() == null || curOrg.getLakkautusPvm().after(now))) {
                result.add(curOrg);
            }
        }

        return result;

    }

    /**
     *
     * @param ytunnus
     * @return
     */
    @Override
    public boolean isYtunnusAvailable(String ytunnus) {
        return ((Number) em
                .createQuery("SELECT COUNT(ytunnus) FROM Organisaatio WHERE ytunnus=:ytunnus AND organisaatioPoistettu = FALSE")
                .setParameter("ytunnus", ytunnus.trim())
                .getSingleResult()).intValue() == 0;
    }

    @Override
    public List<Organisaatio> findModifiedSince(
            boolean excludePiilotettu,
            LocalDateTime lastModifiedSince) {
        return findModifiedSince(excludePiilotettu, lastModifiedSince, Collections.emptyList(), true);
    }

    @Override
    public List<Organisaatio> findModifiedSince(
            boolean excludePiilotettu,
            LocalDateTime lastModifiedSinceLocalDate,
            List<OrganisaatioTyyppi> organizationTypes,
            boolean excludeDiscontinued) {
        Date lastModifiedSince = java.sql.Timestamp.valueOf(lastModifiedSinceLocalDate);

        logger.debug("findModifiedSince({})", lastModifiedSince);

        QOrganisaatio qOrganisaatio = QOrganisaatio.organisaatio;

        BooleanExpression whereExpression = qOrganisaatio.paivitysPvm.after(lastModifiedSince);
        if (excludePiilotettu) {
            whereExpression = whereExpression.and(qOrganisaatio.piilotettu.eq(false));
        }
        if (organizationTypes != null && !organizationTypes.isEmpty()) {
            String[] types = organizationTypes.stream()
                    .map(OrganisaatioTyyppi::koodiValue).toArray(String[]::new);
            whereExpression = whereExpression.and(
                    qOrganisaatio.tyypit.any().in(types));
        }
        if (excludeDiscontinued) {
            Date now = new Date();
            whereExpression = whereExpression.and(
                    qOrganisaatio.lakkautusPvm.isNull().or(qOrganisaatio.lakkautusPvm.after(now)));
        }

        return new JPAQuery<>(em)
                .from(qOrganisaatio)
                .where(whereExpression)
                .distinct()
                .select(qOrganisaatio)
                .fetch();
    }

    @Override
    public Collection<Organisaatio> findByTarkastusPvm(Date tarkastusPvm, LocalDate voimassaPvmLocalDate, Collection<String> oids, long limit) {
        java.sql.Date voimassaPvm = java.sql.Date.valueOf(voimassaPvmLocalDate);
        QOrganisaatio qOrganisaatio = QOrganisaatio.organisaatio;
        QOrganisaatioSahkoposti qOrganisaatioSahkoposti = QOrganisaatioSahkoposti.organisaatioSahkoposti;
        JPQLQuery<Organisaatio> sahkopostiLahetettySubQuery = JPAExpressions
                .selectDistinct(qOrganisaatioSahkoposti.organisaatio)
                .from(qOrganisaatioSahkoposti)
                .where(qOrganisaatioSahkoposti.tyyppi.eq(OrganisaatioSahkoposti.Tyyppi.VANHENTUNEET_TIEDOT))
                .where(qOrganisaatioSahkoposti.aikaleima.after(tarkastusPvm));
        return new JPAQuery<>(em)
                .from(qOrganisaatio)
                .where(qOrganisaatio.organisaatioPoistettu.isFalse())
                .where(anyOf(qOrganisaatio.alkuPvm.loe(voimassaPvm), qOrganisaatio.alkuPvm.isNull()))
                .where(anyOf(qOrganisaatio.lakkautusPvm.after(voimassaPvm), qOrganisaatio.lakkautusPvm.isNull()))
                .where(qOrganisaatio.oid.in(oids))
                .where(anyOf(qOrganisaatio.tarkastusPvm.before(tarkastusPvm), qOrganisaatio.tarkastusPvm.isNull()))
                .where(qOrganisaatio.notIn(sahkopostiLahetettySubQuery))
                .select(qOrganisaatio)
                .distinct()
                .orderBy(qOrganisaatio.tarkastusPvm.asc().nullsFirst(), qOrganisaatio.id.asc())
                .limit(limit)
                .fetch();
    }

    private BooleanExpression getVoimassaoloExpression(boolean suunnitellut, boolean lakkautetut, QOrganisaatio qOrganisaatio) {
        logger.debug("getVoimassaoloExpression()");
        BooleanExpression voimassaoloExpr = null;

        Date currentDate = Calendar.getInstance().getTime();
        BooleanExpression alkuPvmLoe = qOrganisaatio.alkuPvm.loe(currentDate).or(qOrganisaatio.alkuPvm.isNull());
        BooleanExpression lakkautusPvmGoe = qOrganisaatio.lakkautusPvm.goe(currentDate).or(qOrganisaatio.lakkautusPvm.isNull());

        if (!suunnitellut && !lakkautetut) {
            voimassaoloExpr = alkuPvmLoe.and(lakkautusPvmGoe);
        } else if (suunnitellut && !lakkautetut) {
            voimassaoloExpr = lakkautusPvmGoe;
        } else if (lakkautetut && !suunnitellut) {
            voimassaoloExpr = alkuPvmLoe;
        }

        return voimassaoloExpr;
    }

    @Override
    public List<OrganisaatioRDTOV3> findByOids(Collection<String> oids) {
        logger.debug("findByOids(Number of OIDs = {})", oids.size());
        QOrganisaatio org = QOrganisaatio.organisaatio;

        return new JPAQuery<>(em)
                .from(org)
                .where(org.oid.in(oids)
                        .and(org.organisaatioPoistettu.isFalse()))
                .select(new OrganisaatioToOrganisaatioRDTOV3ProjectionFactory(org, this.organisaatioNimiModelMapper))
                .fetch();
    }

    private List<Organisaatio> findByOids(Collection<String> oids, boolean excludePoistettu) {
        return findByOids(oids, excludePoistettu, true);
    }

    @Override
    public List<Organisaatio> findByOids(Collection<String> oids, boolean excludePoistettu, boolean excludePiilotettu) {
        logger.debug("findByOids(Number of OIDs = {})", oids.size());
        QOrganisaatio org = QOrganisaatio.organisaatio;
        QOrganisaatioMetaData metaData = QOrganisaatioMetaData.organisaatioMetaData;
        QMonikielinenTeksti metadatanimi = new QMonikielinenTeksti("metadatanimi");
        QMonikielinenTeksti currentnimi = new QMonikielinenTeksti("currentnimi");
        QVarhaiskasvatuksenToimipaikkaTiedot qVarhaiskasvatuksenToimipaikkaTiedot = QVarhaiskasvatuksenToimipaikkaTiedot.varhaiskasvatuksenToimipaikkaTiedot;
        QMonikielinenTeksti kuvaus2 = new QMonikielinenTeksti("kuvaus2");
        QMonikielinenTeksti hakutoimistoEctsEmailmkt = new QMonikielinenTeksti("hakutoimistoEctsEmailmkt");
        QMonikielinenTeksti hakutoimistoEctsNimimkt = new QMonikielinenTeksti("hakutoimistoEctsNimimkt");
        QMonikielinenTeksti hakutoimistoEctsTehtavanimikemkt = new QMonikielinenTeksti("hakutoimistoEctsTehtavanimikemkt");
        QMonikielinenTeksti hakutoimistoEctsPuhelinmkt = new QMonikielinenTeksti("hakutoimistoEctsPuhelinmkt");
        QMonikielinenTeksti hakutoimistoNimi = new QMonikielinenTeksti("hakutoimistoNimi");

        JPAQuery<Organisaatio> jpaQuery = new JPAQuery<Organisaatio>(em)
                .select(org)
                .distinct()
                .from(org)
                .leftJoin(org.kuvaus2, kuvaus2).fetchJoin()
                .innerJoin(org.nimi, currentnimi).fetchJoin()
                .leftJoin(org.kielet).fetchJoin()
                .leftJoin(org.metadata, metaData).fetchJoin()
                .leftJoin(metaData.nimi, metadatanimi).fetchJoin()
                .leftJoin(metaData.hakutoimistoEctsEmailmkt, hakutoimistoEctsEmailmkt).fetchJoin()
                .leftJoin(metaData.hakutoimistoEctsNimimkt, hakutoimistoEctsNimimkt).fetchJoin()
                .leftJoin(metaData.hakutoimistoEctsTehtavanimikemkt, hakutoimistoEctsTehtavanimikemkt).fetchJoin()
                .leftJoin(metaData.hakutoimistoEctsPuhelinmkt, hakutoimistoEctsPuhelinmkt).fetchJoin()
                .leftJoin(metaData.hakutoimistoNimi, hakutoimistoNimi).fetchJoin()
                .leftJoin(org.varhaiskasvatuksenToimipaikkaTiedot, qVarhaiskasvatuksenToimipaikkaTiedot).fetchJoin()
                .where(org.oid.in(oids));
        if (excludePoistettu) {
            jpaQuery.where(org.organisaatioPoistettu.isFalse());
        }
        if (excludePiilotettu) {
            jpaQuery.where(org.piilotettu.isFalse());
        }
        return jpaQuery.fetch();
    }

    @Override
    public List<Organisaatio> findByOidList(List<String> oidList, int maxResults) {
        logger.debug("findByOidList({}, {})", oidList, maxResults);

        // first drop nulls from oidList
        List<String> oidListFiltered = new ArrayList<>();
        for (String oid : oidList) {
            if (oid != null) {
                oidListFiltered.add(oid);
            }
        }

        // perform query
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Organisaatio> query = cb.createQuery(Organisaatio.class);
        Root<Organisaatio> organisaatio = query.from(Organisaatio.class);
        query.orderBy(cb.asc(organisaatio.get("nimihaku")));
        Predicate where = cb.in(organisaatio.get("oid")).value(oidListFiltered);
        query.where(where);
        return em.createQuery(query).setMaxResults(maxResults).getResultList();

    }

    /**
     * Mark Organisation to be removed. Can only be done if it dows not have any
     * active children.
     *
     * @param oid
     * @return parent
     */
    @Override
    public Organisaatio markRemoved(String oid) {
        Organisaatio org = findByOids(List.of(oid), false).stream().findFirst().orElse(null);

        if (org == null) {
            throw new OrganisaatioCrudException("organisaatio.not.found.with.oid");
        }

        // OVT-2391, cannot remove organization if there is child organizations)
        if (!findChildren(oid, false, false).isEmpty()) {
            throw new OrganisaatioCrudException("organisaatio.child.orgs.found");
        }

        // Mark removed
        org.setOrganisaatioPoistettu(true);
        em.persist(org);

        return org.getParent();
    }

    /**
     * Return parent org oids to org, optimized for the auth use.
     *
     * @param oid
     * @return
     */

    public List<String> findParentOidsTo(String oid) {
        logger.debug("findParentOidsTo({})", oid);
        Preconditions.checkNotNull(oid);

        Organisaatio org = findByOids(List.of(oid), false).stream().findFirst()
                .orElseThrow(() -> new OrganisaatioNotFoundException(oid));
        return Stream.concat(Stream.of(oid), org.getParentOids().stream()).collect(
                Collectors.collectingAndThen(toList(), oids -> {
                    Collections.reverse(oids);
                    return oids;
                })
        );
    }

    /**
     * Return parent org oids to org, optimized for the auth use.
     * <p>
     * Parents are returned in "root first" order.
     * <pre>
     * Example: a (b c (f g)) (d e)
     * findParentsTo(g) -> a c
     * </pre>
     *
     * @param oid
     * @return
     */
    @Override
    public List<Organisaatio> findParentsTo(String oid) {
        logger.debug("findParentOidsTo({})", oid);
        Preconditions.checkNotNull(oid);
        List<Organisaatio> parents = Lists.newArrayList();

        Organisaatio org = findByOids(List.of(oid), false).stream().findFirst().orElse(null);

        while (org != null) {
            parents.add(org);
            org = org.getParent();
        }
        Collections.reverse(parents);
        return parents;
    }

    /**
     * Finds list of oids with given query params.
     *
     * @param requireYtunnus
     * @param count
     * @param startIndex
     * @param type
     * @return
     */
    @Override
    public List<String> findOidsBy(Boolean requireYtunnus, int count, int startIndex, OrganisaatioTyyppi type) {

        logger.debug("findOidsBy({}, {}, {}, {})", requireYtunnus, count, startIndex, type);

        QOrganisaatio org = QOrganisaatio.organisaatio;
        BooleanBuilder whereExpr = new BooleanBuilder();
        whereExpr.and(org.organisaatioPoistettu.isFalse());
        whereExpr.and(org.piilotettu.isFalse());
        // Select by Org tyyppi
        if (type != null) {
            whereExpr.and(org.tyypit.contains(type.koodiValue()));
        }
        if (requireYtunnus) {
            whereExpr.and(org.ytunnus.isNotNull());
        }

        JPAQuery<String> q = new JPAQuery<>(em);
        q = q.from(org);

        q = q.where(whereExpr);

        if (count > 0) {
            q = q.limit(count);
        }

        if (startIndex > 0) {
            q.offset(startIndex);
        }

        logger.debug("  q = {}", q);

        return q.select(org.oid).fetch();
    }

    /**
     * Haetaan organisaatiota Y-Tunnuksen perusteella.
     *
     * @param oid
     * @return
     */
    @Override
    public Organisaatio findByYTunnus(String oid) {
        logger.debug("findByYtunnus({})", oid);
        QOrganisaatio org = QOrganisaatio.organisaatio;
        return new JPAQuery<>(em).from(org).where(org.ytunnus.eq(oid).and(org.organisaatioPoistettu.isFalse())).select(org).fetchFirst();
    }

    /**
     * Haetaan organisaatiota virastotunnuksen perusteella.
     *
     * @param oid
     * @return
     */
    @Override
    public Organisaatio findByVirastoTunnus(String oid) {
        logger.debug("findByVirastotunnus({})", oid);
        QOrganisaatio org = QOrganisaatio.organisaatio;
        return new JPAQuery<>(em).from(org).where(org.virastoTunnus.eq(oid).and(org.organisaatioPoistettu.isFalse())).select(org).fetchFirst();
    }

    /**
     * Haetaan organisaatiota oppilaitoskoodin perusteella.
     *
     * @param oid
     * @return
     */
    @Override
    public Organisaatio findByOppilaitoskoodi(String oid) {
        logger.debug("findByOppilaitoskoodi({})", oid);
        QOrganisaatio org = QOrganisaatio.organisaatio;
        return new JPAQuery<>(em).from(org).where(org.oppilaitosKoodi.eq(oid).and(org.organisaatioPoistettu.isFalse())).select(org).fetchFirst();
    }

    /**
     * Haetaan organisaatiota toimipistekoodin perusteella.
     *
     * @param oid
     * @return
     */
    @Override
    public Organisaatio findByToimipistekoodi(String oid) {
        logger.debug("findByToimipisteKoodi({})", oid);
        QOrganisaatio org = QOrganisaatio.organisaatio;
        return new JPAQuery<>(em).from(org).where(org.toimipisteKoodi.eq(oid).and(org.organisaatioPoistettu.isFalse())).select(org).fetchFirst();
    }

    @Override
    public Set<Organisaatio> findBySearchCriteria(
            Set<String> kieliList,
            Set<String> kuntaList,
            Set<String> oppilaitostyyppiList,
            Set<String> vuosiluokkaList,
            Set<String> ytunnusList,
            Set<String> oidList,
            int limit) {

        logger.debug("findBySearchCriteria()");

        QOrganisaatio org = QOrganisaatio.organisaatio;

        // Ei oteta mukaan oph organisaatiota
        BooleanExpression whereExpression = org.oid.ne(ophOid);

        // Ei oteta mukaan poistettuja organisaatioita
        whereExpression = whereExpression.and(org.organisaatioPoistettu.isFalse());

        // Ei oteta mukaan suunniteltuja ja lakkautettuja organisaatioita
        BooleanExpression voimassaOloExpr = getVoimassaoloExpression(false, false, org);
        whereExpression = (voimassaOloExpr != null) ? whereExpression.and(voimassaOloExpr) : whereExpression;

        // Otetaan mukaan vain organisaatiot, joiden kieli on annetussa kielilistassa
        BooleanExpression kieliExpr = getKieliExpression(org, kieliList);
        whereExpression = (kieliExpr != null) ? whereExpression.and(kieliExpr) : whereExpression;

        // Otetaan mukaan vain organisaatiot, joiden kotikunta on annetussa kuntalistassa
        BooleanExpression kuntaExpr = getKuntaExpression(org, kuntaList);
        whereExpression = (kuntaExpr != null) ? whereExpression.and(kuntaExpr) : whereExpression;

        // Otetaan mukaan vain organisaatiot, joiden oppilaitostyyppi on annetussa oppilaitostyyppilistassa
        BooleanExpression oppilaitostyyppiExpr = getOppilaitostyyppiExpression(org, oppilaitostyyppiList);
        whereExpression = (oppilaitostyyppiExpr != null) ? whereExpression.and(oppilaitostyyppiExpr) : whereExpression;

        // Otetaan mukaan vain organisaatiot, joiden y-tunnus on annetussa ytunnuslistassa
        BooleanExpression ytunnusExpr = getYtunnusExpression(org, ytunnusList);
        whereExpression = (ytunnusExpr != null) ? whereExpression.and(ytunnusExpr) : whereExpression;

        // Otetaan mukaan vain organisaatiot, joiden vuosiluokissa esiintyy jokin annetusta vuosiluokkalistasta
        BooleanExpression vuosiluokkaExpr = getVuosiluokkaExpression(org, vuosiluokkaList);
        whereExpression = (vuosiluokkaExpr != null) ? whereExpression.and(vuosiluokkaExpr) : whereExpression;

        // Otetaan mukaan vain organisaatiot, joiden oid  esiintyy annetussa oidlistassa
        BooleanExpression oidExpr = getOidExpression(org, oidList);
        whereExpression = (oidExpr != null) ? whereExpression.and(oidExpr) : whereExpression;

        long qstarted = System.currentTimeMillis();

        List<Organisaatio> organisaatiot = new JPAQuery<>(em)
                .from(org)
                .select(org)
                .limit(limit + 1)
                .where(whereExpression)
                //.distinct()
                .fetch();

        logger.debug("Query took {} ms", System.currentTimeMillis() - qstarted);

        return new HashSet<>(organisaatiot);
    }

    private BooleanExpression getKieliExpression(QOrganisaatio qOrganisaatio, Set<String> kieliList) {
        if (kieliList == null || kieliList.isEmpty()) {
            return null;
        }

        Iterator<String> kieli = kieliList.iterator();
        BooleanExpression kieliExpr = qOrganisaatio.kielet.contains(kieli.next());
        if (kieliList.size() > 1) {
            for (int i = 1; i < kieliList.size(); ++i) {
                kieliExpr = kieliExpr.or(qOrganisaatio.kielet.contains(kieli.next()));
            }
        }
        return kieliExpr;
    }

    private BooleanExpression getKuntaExpression(QOrganisaatio qOrganisaatio, Set<String> kuntaList) {
        if (kuntaList == null || kuntaList.isEmpty()) {
            return null;
        }

        Iterator<String> kunta = kuntaList.iterator();
        BooleanExpression kuntaExpr = qOrganisaatio.kotipaikka.eq(kunta.next());
        if (kuntaList.size() > 1) {
            for (int i = 1; i < kuntaList.size(); ++i) {
                kuntaExpr = kuntaExpr.or(qOrganisaatio.kotipaikka.eq(kunta.next()));
            }
        }
        return kuntaExpr;
    }

    private BooleanExpression getOppilaitostyyppiExpression(QOrganisaatio qOrganisaatio, Set<String> oppilaitostyyppiList) {
        if (oppilaitostyyppiList == null || oppilaitostyyppiList.isEmpty()) {
            return null;
        }
        Iterator<String> oppilaitostyyppi = oppilaitostyyppiList.iterator();
        BooleanExpression oppilaitostyyppiExpr = getUriVersionExpression(qOrganisaatio.oppilaitosTyyppi, oppilaitostyyppi.next());
        if (oppilaitostyyppiList.size() > 1) {
            for (int i = 1; i < oppilaitostyyppiList.size(); ++i) {
                oppilaitostyyppiExpr = oppilaitostyyppiExpr.or(getUriVersionExpression(qOrganisaatio.oppilaitosTyyppi, oppilaitostyyppi.next()));
            }
        }
        return oppilaitostyyppiExpr;
    }

    private BooleanExpression getYtunnusExpression(QOrganisaatio qOrganisaatio, Set<String> ytunnusList) {
        if (ytunnusList == null || ytunnusList.isEmpty()) {
            return null;
        }

        Iterator<String> ytunnus = ytunnusList.iterator();
        BooleanExpression ytunnusExpr = qOrganisaatio.ytunnus.eq(ytunnus.next());
        if (ytunnusList.size() > 1) {
            for (int i = 1; i < ytunnusList.size(); ++i) {
                ytunnusExpr = ytunnusExpr.or(qOrganisaatio.ytunnus.eq(ytunnus.next()));
            }
        }
        return ytunnusExpr;
    }

    private BooleanExpression getVuosiluokkaExpression(QOrganisaatio qOrganisaatio, Set<String> vuosiluokkaList) {
        if (vuosiluokkaList == null || vuosiluokkaList.isEmpty()) {
            return null;
        }

        Iterator<String> vuosiluokka = vuosiluokkaList.iterator();
        BooleanExpression vuosiluokkaExpr = qOrganisaatio.vuosiluokat.contains(vuosiluokka.next());
        if (vuosiluokkaList.size() > 1) {
            for (int i = 1; i < vuosiluokkaList.size(); ++i) {
                vuosiluokkaExpr = vuosiluokkaExpr.or(qOrganisaatio.vuosiluokat.contains(vuosiluokka.next()));
            }
        }
        return vuosiluokkaExpr;
    }

    private BooleanExpression getOidExpression(QOrganisaatio qOrganisaatio, Set<String> oidList) {
        if (oidList == null || oidList.isEmpty()) {
            return null;
        }

        Iterator<String> oid = oidList.iterator();
        BooleanExpression oidExpr = qOrganisaatio.oid.eq(oid.next());
        if (oidList.size() > 1) {
            for (int i = 1; i < oidList.size(); ++i) {
                oidExpr = oidExpr.or(qOrganisaatio.oid.eq(oid.next()));
            }
        }
        return oidExpr;
    }

    private BooleanExpression getUriVersionExpression(StringPath string, String criteriaUri) {
        if (criteriaUri.matches(URI_WITH_VERSION_REG_EXP)) {
            return string.eq(criteriaUri);
        }

        return string.like(criteriaUri + "#%");
    }

    /**
     * Haetaan aktiiviset organisaatiot, joka ovat tyyppiä 'Ryhmä'
     * -----------------------------------------------------------
     * SELECT *
     * FROM organisaatio
     * WHERE parentoidpath = '|1.2.246.562.10.00000000001|'
     * AND organisaatiopoistettu = FALSE
     * AND id IN (SELECT organisaatio_id FROM organisaatio_tyypit WHERE tyypit = 'Ryhma')
     * <p>
     * <p>
     * Toinen tapa hakea on hakea kaikki ryhmät tyypit taulusta
     * <p>
     * SELECT org.*
     * FROM organisaatio org
     * RIGHT JOIN organisaatio_tyypit tp
     * ON org.id = tp.organisaatio_id
     * WHERE tp.tyypit = 'Ryhma'
     * AND org.organisaatiopoistettu = FALSE
     * AND org.parentoidpath = '|1.2.246.562.10.00000000001|'
     *
     * @param criteria hakukriteerit
     * @return
     **/
    @Override
    public List<Organisaatio> findGroups(RyhmaCriteriaDto criteria) {
        logger.debug("findGroups()");

        QOrganisaatio qOrganisaatio = QOrganisaatio.organisaatio;
        QMonikielinenTeksti qNimi = new QMonikielinenTeksti("nimi");
        QMonikielinenTeksti qKuvaus = new QMonikielinenTeksti("kuvaus");

        QOrganisaatio qOrganisaatiotyyppiSub = new QOrganisaatio("organisaatiotyyppiSub");
        StringPath qTyyppi = Expressions.stringPath("tyyppi");
        JPQLQuery<Organisaatio> organisaatiotyyppiQuery = JPAExpressions.selectFrom(qOrganisaatiotyyppiSub)
                .join(qOrganisaatiotyyppiSub.tyypit, qTyyppi).where(qTyyppi.eq("Ryhma"));

        JPAQuery<Organisaatio> query = new JPAQuery<>(em).from(qOrganisaatio).select(qOrganisaatio)
                .join(qOrganisaatio.nimi, qNimi).fetchJoin()
                .join(qOrganisaatio.kuvaus2, qKuvaus).fetchJoin()
                .where(qOrganisaatio.in(organisaatiotyyppiQuery));

        Optional.ofNullable(criteria.getQ()).ifPresent(q -> {
            QMonikielinenTeksti qNimiHaku = new QMonikielinenTeksti("nimiHaku");
            StringPath qNimiArvo = Expressions.stringPath("nimiArvo");
            JPQLQuery<MonikielinenTeksti> subquery = JPAExpressions.select(qNimiHaku)
                    .from(qNimiHaku)
                    .join(qNimiHaku.values, qNimiArvo)
                    .where(qNimiArvo.containsIgnoreCase(q));
            query.where(anyOf(qNimi.in(subquery), qOrganisaatio.oid.eq(q)));
        });
        Optional.ofNullable(criteria.getLakkautusPvm()).map(java.sql.Date::valueOf).ifPresent(lakkautusPvm -> {
            if (Boolean.TRUE.equals(criteria.getAktiivinen())) {
                query.where(anyOf(qOrganisaatio.lakkautusPvm.isNull(), qOrganisaatio.lakkautusPvm.goe(lakkautusPvm)));
            } else if (Boolean.FALSE.equals(criteria.getAktiivinen())) {
                query.where(qOrganisaatio.lakkautusPvm.lt(lakkautusPvm));
            } else {
                query.where(qOrganisaatio.lakkautusPvm.eq(lakkautusPvm));
            }
        });
        Optional.ofNullable(criteria.getRyhmatyyppi()).ifPresent(ryhmatyyppi -> {
            QOrganisaatio qRyhma = new QOrganisaatio("ryhmatyyppiSub");
            StringPath qRyhmatyyppi = Expressions.stringPath("ryhmatyyppi");
            JPQLQuery<Organisaatio> subquery = JPAExpressions.select(qRyhma)
                    .from(qRyhma)
                    .join(qRyhma.ryhmatyypit, qRyhmatyyppi)
                    .where(qRyhmatyyppi.eq(ryhmatyyppi));
            query.where(qOrganisaatio.in(subquery));
        });
        Optional.ofNullable(criteria.getKayttoryhma()).ifPresent(kayttoryhma -> {
            QOrganisaatio qRyhma = new QOrganisaatio("kayttoryhmaSub");
            StringPath qKayttoryhma = Expressions.stringPath("kayttoryhma");
            JPQLQuery<Organisaatio> subquery = JPAExpressions.select(qRyhma)
                    .from(qRyhma)
                    .join(qRyhma.kayttoryhmat, qKayttoryhma)
                    .where(qKayttoryhma.eq(kayttoryhma));
            query.where(qOrganisaatio.in(subquery));
        });
        Optional.ofNullable(criteria.getParentOid()).ifPresent(parentOid -> {
            query.where(qOrganisaatio.parentOids.get(0).eq(parentOid));
        });
        Optional.ofNullable(criteria.getPoistettu()).ifPresent(poistettu -> {
            query.where(qOrganisaatio.organisaatioPoistettu.eq(poistettu));
        });

        return query.fetch();
    }

    @Override
    public List<Organisaatio> findByAncestorOid(String oid) {
        String sql =
                "select distinct o from Organisaatio o " +
                        "join fetch o.parentOids p " +
                        "join fetch o.tyypit t " +
                        "join fetch o.nimi n " +
                        "join fetch n.values nv " +
                        "left join fetch o.kielet k " +
                        "where t <> 'Ryhma' and o.organisaatioPoistettu <> true and o in (select x from Organisaatio x join x.parentOids xp where xp = :oid)";
        TypedQuery<Organisaatio> query = em
                .createQuery(sql, Organisaatio.class)
                .setHint(QueryHints.HINT_PASS_DISTINCT_THROUGH, false)
                .setParameter("oid", oid);
        return query.getResultList();
    }

    @Override
    public EntityManager getJpaEntityManager() {
        return em;
    }

    @Override
    public Collection<String> findChildOidsRecursive(ChildOidsCriteria criteria) {
        String sql = "WITH RECURSIVE aliorganisaatiot AS ( " +
                "    SELECT id, oid " +
                "    FROM organisaatio o " +
                "    WHERE oid = :oid " +
                "UNION " +
                "    SELECT o.id, o.oid " +
                "    FROM organisaatio o " +
                "    JOIN organisaatiosuhde os ON os.child_id = o.id " +
                "    JOIN aliorganisaatiot ao ON ao.id = os.parent_id " +
                "    WHERE os.suhdetyyppi <> 'LIITOS' " +
                "    AND (os.loppupvm IS NULL OR os.loppupvm > :paivamaara) " +
                "    AND o.organisaatiopoistettu = FALSE " +
                "    AND ( " +
                "        (:aktiiviset = TRUE AND (o.alkupvm IS NULL OR o.alkupvm <= :paivamaara) " +
                "                            AND (o.lakkautuspvm IS NULL OR o.lakkautuspvm > :paivamaara)) " +
                "        OR " +
                "        (:suunnitellut = TRUE AND (o.alkupvm > :paivamaara)) " +
                "        OR " +
                "        (:lakkautetut = TRUE AND (o.lakkautuspvm <= :paivamaara)) " +
                "    ) " +
                ") " +
                "SELECT oid FROM aliorganisaatiot ";
        List<String> childOids = namedParameterJdbcTemplate.query(sql, new BeanPropertySqlParameterSource(criteria),
                (ResultSet rs, int rowNum) -> rs.getString("oid"));
        return childOids.stream().filter(childOid -> !childOid.equals(criteria.getOid())).collect(toSet());
    }

}
