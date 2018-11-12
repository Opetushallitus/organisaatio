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
package fi.vm.sade.organisaatio.dao.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import fi.vm.sade.generic.dao.AbstractJpaDAOImpl;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.business.exception.OrganisaatioCrudException;
import fi.vm.sade.organisaatio.dao.OrganisaatioDAO;
import fi.vm.sade.organisaatio.dao.OrganisaatioSuhdeDAO;
import fi.vm.sade.organisaatio.dto.ChildOidsCriteria;
import fi.vm.sade.organisaatio.dto.mapping.OrganisaatioNimiModelMapper;
import fi.vm.sade.organisaatio.dto.mapping.RyhmaCriteriaDto;
import fi.vm.sade.organisaatio.dto.v3.OrganisaatioRDTOV3;
import fi.vm.sade.organisaatio.model.*;
import fi.vm.sade.organisaatio.model.dto.OrgPerustieto;
import fi.vm.sade.organisaatio.model.dto.OrgStructure;
import fi.vm.sade.organisaatio.model.dto.QOrgPerustieto;
import fi.vm.sade.organisaatio.model.dto.QOrgStructure;
import fi.vm.sade.organisaatio.service.converter.v3.OrganisaatioToOrganisaatioRDTOV3ProjectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.OptimisticLockException;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.querydsl.core.types.dsl.Expressions.anyOf;
import static java.util.stream.Collectors.toSet;

/**
 * @author tommiha
 * @author mlyly
 */
@Repository
public class OrganisaatioDAOImpl extends AbstractJpaDAOImpl<Organisaatio, Long> implements OrganisaatioDAO {

    protected final Logger LOG = LoggerFactory.getLogger(getClass());

    @Value("${root.organisaatio.oid}")
    private String ophOid;

    @Autowired
    OrganisaatioSuhdeDAO organisaatioSuhdeDAO;

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    private NativeQueryHelper nativeQueryHelper;

    @Autowired
    private OrganisaatioNimiModelMapper organisaatioNimiModelMapper;

    private static final String uriWithVersionRegExp = "^.*#[0-9]+$";

    /**
     * Find the children of given parent organisation.
     *
     * @param parentId
     * @return
     */
    @Override
    public List<Organisaatio> findChildren(Long parentId) {
        LOG.debug("findChildren({})", parentId);

        QOrganisaatio qOrganisaatio = QOrganisaatio.organisaatio;

        BooleanExpression whereExpression = qOrganisaatio.parentIdPath.endsWith("|" + parentId + "|");

        return new JPAQuery<>(getEntityManager())
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
     * @param myosPoistetut if true return also "removed" orgs
     * @param myosLakkautetut
     * @return
     */
    @Override
    public List<Organisaatio> findChildren(String parentOid, boolean myosPoistetut, boolean myosLakkautetut) {
        LOG.debug("findChildren({})", parentOid);

        Organisaatio parent = this.findByOid(parentOid);
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

    public List<Organisaatio> findByDomainNimi(String domainNimi) {
        LOG.debug("findByDomainNimi");
        return findBy("domainNimi", domainNimi);
    }

    /**
     *
     * @param ytunnus
     * @return
     */
    @Override
    public boolean isYtunnusAvailable(String ytunnus) {
        return ((Number) getEntityManager()
                .createQuery("SELECT COUNT(*) FROM " + Organisaatio.class.getName() + " WHERE ytunnus=:ytunnus AND organisaatiopoistettu = FALSE")
                .setParameter("ytunnus", ytunnus.trim())
                .getSingleResult()).intValue() == 0;
    }

    @Override
    public List<Organisaatio> findModifiedSince(Date lastModifiedSince) {
        LOG.debug("findModifiedSince({})", lastModifiedSince);

        QOrganisaatio qOrganisaatio = QOrganisaatio.organisaatio;

        BooleanExpression whereExpression = qOrganisaatio.paivitysPvm.after(lastModifiedSince);

        return new JPAQuery<>(getEntityManager())
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
        return new JPAQuery<>(getEntityManager())
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

    public List<Organisaatio> findOrganisaatioByNimiLike(String organisaatioNimi, int firstResult, int maxResults) {
        LOG.debug("findOrganisaatioByNimiLike()");
        Query query = getEntityManager().createQuery("SELECT o FROM Organisaatio o WHERE UPPER(o.nimiFi) LIKE :orgnimi OR UPPER(o.nimiSv) LIKE :orgnimi "
                + "OR UPPER(o.nimiEn) LIKE :orgnimi");
        query.setParameter("orgnimi", "%" + organisaatioNimi.toUpperCase() + "%");
        query.setFirstResult(firstResult);
        query.setMaxResults((maxResults <= 100) ? maxResults + 1 : 101);
        return query.getResultList();
    }

    public List<OrgPerustieto> findBySearchCriteria(String orgTyyppi,
                                                    String oppilaitosTyyppi,
                                                    String kunta,
                                                    String searchStr,
                                                    boolean suunnitellut,
                                                    boolean lakkautetut,
                                                    boolean yTunnus,
                                                    boolean olKoodi,
                                                    int maxResults,
                                                    List<String> oids) {
        LOG.debug("findBySearchCriteria()");

        QOrganisaatio qOrganisaatio = new QOrganisaatio("a");

        //Not retrieving root of all organisations
        BooleanExpression whereExpression = qOrganisaatio.oid.ne(ophOid);

        //Not retrieving removed organisations
        whereExpression = whereExpression.and(qOrganisaatio.organisaatioPoistettu.isFalse());

        //Retrieving only organisations whose start and end date match the given criteria
        BooleanExpression voimassaOloExpr = getVoimassaoloExpression(suunnitellut, lakkautetut, qOrganisaatio);
        whereExpression = (voimassaOloExpr != null) ? whereExpression.and(voimassaOloExpr) : whereExpression;

        //Retrieving only organisations whose type equal the given criteria
        BooleanExpression orgTyyppiMatches = (orgTyyppi != null) ? qOrganisaatio.tyypit.contains(orgTyyppi) : null;
        whereExpression = (orgTyyppiMatches != null) ? whereExpression.and(orgTyyppiMatches) : whereExpression;

        //Retrieving only organisations that match the given search string
        BooleanExpression stringMatches = getStringExpression(searchStr, qOrganisaatio);
        whereExpression = (stringMatches != null) ? whereExpression.and(stringMatches) : whereExpression;

        //Retrieving only organisations whose oppilaitoskoodi matches the given criteria
        whereExpression = (oppilaitosTyyppi != null) ? whereExpression.and(qOrganisaatio.oppilaitosTyyppi.eq(oppilaitosTyyppi)) : whereExpression;

        //Retrieving only organisations whose home place matches the given criteria
        whereExpression = (kunta != null) ? whereExpression.and(qOrganisaatio.kotipaikka.eq(kunta)) : whereExpression;

        //Retrieving only organisations whose oid match the given list
        BooleanExpression restrictedMatches = getRestrictedMatches(qOrganisaatio, oids);
        whereExpression = (restrictedMatches != null) ? whereExpression.and(restrictedMatches) : whereExpression;

        long qstarted = System.currentTimeMillis();

        List<OrgPerustieto> organisaatiot = new JPAQuery<>(getEntityManager())
                .from(qOrganisaatio)
                .where(whereExpression)
                .distinct()
                        //.orderBy(qOrganisaatio1.nimihaku.asc())
                .limit(maxResults + 1)
                .select(new QOrgPerustieto(qOrganisaatio.oid, qOrganisaatio.version, qOrganisaatio.alkuPvm, qOrganisaatio.lakkautusPvm, qOrganisaatio.nimi, qOrganisaatio.ytunnus, qOrganisaatio.oppilaitosKoodi, qOrganisaatio.parentOidPath, qOrganisaatio.organisaatiotyypitStr))
                .fetch();

        LOG.debug("Query took {} ms", System.currentTimeMillis() - qstarted);

        organisaatiot = retrieveParentsAndChildren(organisaatiot, new TreeSet<>(oids), suunnitellut, lakkautetut);

        return organisaatiot;
    }

    public void updateOrg(Organisaatio org) throws OptimisticLockException {
        getEntityManager().merge(org);
    }

    private void appendParentOrganisation(List<OrgPerustieto> ret, String poid, boolean suunnitellut, boolean poistetut) {

        QOrganisaatio qOrganisaatio = new QOrganisaatio("a");
        BooleanExpression whereExpression = qOrganisaatio.oid.eq(poid);
        if (!poistetut) {
            whereExpression = whereExpression.and(qOrganisaatio.organisaatioPoistettu.eq(false));
        }

        OrgPerustieto po = new JPAQuery<>(getEntityManager())
                .from(qOrganisaatio)
                .where(whereExpression)
                .select(new QOrgPerustieto(qOrganisaatio.oid, qOrganisaatio.version, qOrganisaatio.alkuPvm, qOrganisaatio.lakkautusPvm, qOrganisaatio.nimi, qOrganisaatio.ytunnus, qOrganisaatio.oppilaitosKoodi, qOrganisaatio.parentOidPath, qOrganisaatio.organisaatiotyypitStr))
                .fetchFirst();

        if (po != null) {
            ret.add(po);
        }
    }

    private void appendChildOrganisations(List<OrgPerustieto> ret, Set<String> procOids, OrgPerustieto parent, Set<String> oids, boolean suunnitellut, boolean poistetut) {

        String noidPath = parent.getParentOidPath() + parent.getOid() + "|";

        QOrganisaatio qOrganisaatio = new QOrganisaatio("a");
        BooleanExpression whereExpression = qOrganisaatio.parentOidPath.startsWith(noidPath);
        if (!poistetut) {
            whereExpression = whereExpression.and(qOrganisaatio.organisaatioPoistettu.eq(false));
        }

        List<OrgPerustieto> pos = new JPAQuery<>(getEntityManager())
                .from(qOrganisaatio)
                .where(whereExpression)
                .select(new QOrgPerustieto(qOrganisaatio.oid, qOrganisaatio.version, qOrganisaatio.alkuPvm, qOrganisaatio.lakkautusPvm, qOrganisaatio.nimi, qOrganisaatio.ytunnus, qOrganisaatio.oppilaitosKoodi, qOrganisaatio.parentOidPath, qOrganisaatio.organisaatiotyypitStr))
                .fetch();

        for (OrgPerustieto pt : pos) {
            if (procOids.add(pt.getOid())) {
                ret.add(pt);
            }
        }

    }

    private List<OrgPerustieto> retrieveParentsAndChildren(List<OrgPerustieto> baseResult, Set<String> oids, boolean suunnitellut, boolean lakkautetut) {
        Set<String> procOids = new TreeSet<>();
        procOids.add(ophOid);
        List<OrgPerustieto> ret = new ArrayList<>();

        Set<String> ppoids = new TreeSet<>();

        for (OrgPerustieto opt : baseResult) {
            if (procOids.add(opt.getOid())) {
                ret.add(opt);
                appendChildOrganisations(ret, procOids, opt, oids, suunnitellut, lakkautetut);
            }
            for (String poid : opt.getParentOidPath().split("\\|")) {
                ppoids.add(poid);
            }
        }

        // poista tyhjä stringi jos sellainen on
        ppoids.remove("");

        if (!oids.isEmpty()) {
            ppoids.retainAll(oids);
        }

        for (String poid : ppoids) {
            if (procOids.add(poid)) {
                appendParentOrganisation(ret, poid, suunnitellut, lakkautetut);
            }
        }

        return ret;
    }

    /**
     *
     * @param orgTyyppi
     * @param oppilaitosTyyppi
     * @param kunta
     * @param searchStr
     * @param suunnitellut
     * @param lakkautetut
     * @param maxResults
     * @param oids
     * @return
     */
    @Override
    public List<OrgPerustieto> findBySearchCriteriaExact(String orgTyyppi,
                                                         String oppilaitosTyyppi,
                                                         String kunta,
                                                         String searchStr,
                                                         boolean suunnitellut,
                                                         boolean lakkautetut,
                                                         int maxResults,
                                                         List<String> oids) {
        LOG.debug("findBySearchCriteriaExact()");

        QOrganisaatio qOrganisaatio = QOrganisaatio.organisaatio;
        //QOrganisaatio qOrganisaatio1 = new QOrganisaatio("b");
        //DslExpression<List<String>> tyypit = qOrganisaatio1.tyypit.as("tyypit");


        //Not retrieving root of all organisations
        BooleanExpression whereExpression = qOrganisaatio.oid.ne(ophOid);

        //Not retrieving removed organisations
        whereExpression = whereExpression.and(qOrganisaatio.organisaatioPoistettu.isFalse());

        //Retrieving only organisations whose start and end date match the given criteria
        BooleanExpression voimassaOloExpr = getVoimassaoloExpression(suunnitellut, lakkautetut, qOrganisaatio);
        whereExpression = (voimassaOloExpr != null) ? whereExpression.and(voimassaOloExpr) : whereExpression;

        //Retrieving only organisations whose type equal the given criteria
        BooleanExpression orgTyyppiMatches = (orgTyyppi != null) ? qOrganisaatio.tyypit.contains(orgTyyppi) : null;
        whereExpression = (orgTyyppiMatches != null) ? whereExpression.and(orgTyyppiMatches) : whereExpression;

        //Retrieving only organisations that match the given search string
        BooleanExpression stringMatches = getStringExpression(searchStr, qOrganisaatio);
        whereExpression = (stringMatches != null) ? whereExpression.and(stringMatches) : whereExpression;

        //Retrieving only organisations whose oppilaitoskoodi matches the given criteria
        whereExpression = (oppilaitosTyyppi != null) ? whereExpression.and(qOrganisaatio.oppilaitosTyyppi.eq(oppilaitosTyyppi)) : whereExpression;

        //Retrieving only organisations whose home place matches the given criteria
        whereExpression = (kunta != null) ? whereExpression.and(qOrganisaatio.kotipaikka.eq(kunta)) : whereExpression;

        //Retrieving only organisations whose oid match the given list
        BooleanExpression restrictedMatches = getRestrictedMatches(qOrganisaatio, oids);
        whereExpression = (restrictedMatches != null) ? whereExpression.and(restrictedMatches) : whereExpression;

        List<OrgPerustieto> organisaatiot = new JPAQuery<>(getEntityManager())
                .from(qOrganisaatio)
                .where(whereExpression)
                .distinct()
                .limit(maxResults + 1)
                .select(new QOrgPerustieto(qOrganisaatio.oid, qOrganisaatio.version, qOrganisaatio.alkuPvm, qOrganisaatio.lakkautusPvm,
                        qOrganisaatio.nimi, qOrganisaatio.ytunnus, qOrganisaatio.oppilaitosKoodi, qOrganisaatio.parentOidPath,
                        qOrganisaatio.organisaatiotyypitStr))
                .fetch();
        return organisaatiot;
    }

    private BooleanExpression getRestrictedMatches(QOrganisaatio qOrganisaatio, List<String> oids) {
        if (oids == null || oids.isEmpty()) {
            return null;
        }
        BooleanExpression oidExpr = qOrganisaatio.oid.eq(oids.get(0)).or(qOrganisaatio.parentOidPath.like("%|" + oids.get(0) + "|%"));
        if (oids.size() > 1) {
            for (int i = 1; i < oids.size(); ++i) {
                oidExpr.or(qOrganisaatio.oid.eq(oids.get(i)).or(qOrganisaatio.parentOidPath.like("%|" + oids.get(i) + "|%")));
            }
        }
        return oidExpr;
    }

    private BooleanExpression getStringExpression(String searchStr, QOrganisaatio qOrganisaatio) {
        LOG.debug("getStringExpression()");
        if (searchStr == null || searchStr.isEmpty()) {
            return null;
        }
        BooleanExpression strExpr = null;

        String searchQueryStr = "%" + searchStr.toUpperCase() + "%";

        BooleanExpression ytunnusMatch = qOrganisaatio.ytunnus.isNotNull().and(qOrganisaatio.ytunnus.toUpperCase().like(searchQueryStr));
        BooleanExpression opkoodiMatch = qOrganisaatio.oppilaitosKoodi.isNotNull().and(qOrganisaatio.oppilaitosKoodi.toUpperCase().like(searchQueryStr));
        strExpr = qOrganisaatio.nimihaku.toUpperCase().like(searchQueryStr)
                .or(ytunnusMatch).or(opkoodiMatch);
        return strExpr;
    }

    private BooleanExpression getVoimassaoloExpression(boolean suunnitellut, boolean lakkautetut, QOrganisaatio qOrganisaatio) {
        LOG.debug("getVoimassaoloExpression()");
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
    public Organisaatio findByOid(String oid) {
        LOG.debug("findByOid({})", oid);


        oid = oid != null ? oid.trim() : null;
        try {
            List<Organisaatio> organisaatios = findBy("oid", oid);
            if (organisaatios.size() == 1) {
                return organisaatios.get(0);
            }
        } catch (Exception ex) {
            LOG.info(ex.getMessage());
        }
        return null;
    }

    @Override
    public List<OrganisaatioRDTOV3> findByOids(Collection<String> oids) {
        LOG.debug("findByOids(Number of OIDs = {})", oids.size());
        QOrganisaatio org = QOrganisaatio.organisaatio;

        return new JPAQuery<>(getEntityManager())
                .from(org)
                .where(org.oid.in(oids)
                        .and(org.organisaatioPoistettu.isFalse()))
                .select(new OrganisaatioToOrganisaatioRDTOV3ProjectionFactory(org, this.organisaatioNimiModelMapper))
                .fetch();
    }

    @Override
    public List<Organisaatio> findByOids(Collection<String> oids, boolean excludePoistettu) {
        LOG.debug("findByOids(Number of OIDs = {})", oids.size());
        QOrganisaatio org = QOrganisaatio.organisaatio;
        QOrganisaatioMetaData metaData = QOrganisaatioMetaData.organisaatioMetaData;
        QYhteystieto metadataYhteystieto = new QYhteystieto("metadataYhteystieto");
        QNamedMonikielinenTeksti metadataValues = QNamedMonikielinenTeksti.namedMonikielinenTeksti;
        QMonikielinenTeksti metadatavaluesvalue = new QMonikielinenTeksti("metadatavaluesvalue");
        QMonikielinenTeksti metadatavaluesname = new QMonikielinenTeksti("metadatavaluesname");
        QMonikielinenTeksti metadatanimi = new QMonikielinenTeksti("metadatanimi");
        QOrganisaatioNimi nimet = QOrganisaatioNimi.organisaatioNimi;
        QMonikielinenTeksti currentnimi = new QMonikielinenTeksti("currentnimi");
        QMonikielinenTeksti nimi = new QMonikielinenTeksti("orgnimi");
        QOrganisaatioLisatietotyyppi organisaatioLisatietotyyppi = QOrganisaatioLisatietotyyppi.organisaatioLisatietotyyppi;
        QLisatietotyyppi lisatietotyyppi = QLisatietotyyppi.lisatietotyyppi;
        QVarhaiskasvatuksenToimipaikkaTiedot qVarhaiskasvatuksenToimipaikkaTiedot = QVarhaiskasvatuksenToimipaikkaTiedot.varhaiskasvatuksenToimipaikkaTiedot;
        QYhteystietoArvo yhteystietoArvo = QYhteystietoArvo.yhteystietoArvo;
        QYhteystietoElementti kentta = QYhteystietoElementti.yhteystietoElementti;
        QYhteystietojenTyyppi yhteystietojenTyyppi = QYhteystietojenTyyppi.yhteystietojenTyyppi;
        QMonikielinenTeksti yhteystietojenTyyppiNimi = new QMonikielinenTeksti("yhteystietojenTyyppiNimi");
        QOrganisaatioSuhde parentSuhteet = QOrganisaatioSuhde.organisaatioSuhde;
        QMonikielinenTeksti kuvaus2 = new QMonikielinenTeksti("kuvaus2");
        QOrganisaatio parent = new QOrganisaatio("parent");
        QMonikielinenTeksti hakutoimistoEctsEmailmkt = new QMonikielinenTeksti("hakutoimistoEctsEmailmkt");
        QMonikielinenTeksti hakutoimistoEctsNimimkt = new QMonikielinenTeksti("hakutoimistoEctsNimimkt");
        QMonikielinenTeksti hakutoimistoEctsTehtavanimikemkt = new QMonikielinenTeksti("hakutoimistoEctsTehtavanimikemkt");
        QMonikielinenTeksti hakutoimistoEctsPuhelinmkt = new QMonikielinenTeksti("hakutoimistoEctsPuhelinmkt");
        QMonikielinenTeksti hakutoimistoNimi = new QMonikielinenTeksti("hakutoimistoNimi");
        QVarhaiskasvatuksenToimipaikkaTiedot vakaToimipaikkatiedot = QVarhaiskasvatuksenToimipaikkaTiedot.varhaiskasvatuksenToimipaikkaTiedot;

        JPAQuery<Organisaatio> jpaQuery = new JPAQuery<Organisaatio>(getEntityManager())
                .select(org)
//                .distinct()
                .from(org)
//                .leftJoin(org.vuosiluokat).fetchJoin()
//                .innerJoin(org.tyypit).fetchJoin()
//                .leftJoin(org.ryhmatyypit).fetchJoin()
//                .leftJoin(org.kayttoryhmat).fetchJoin()
                .leftJoin(org.kuvaus2, kuvaus2).fetchJoin()//.leftJoin(kuvaus2.values).fetchJoin()
                .innerJoin(org.nimi, currentnimi).fetchJoin()//.innerJoin(currentnimi.values).fetchJoin()
                .leftJoin(org.kielet).fetchJoin()
                .leftJoin(org.metadata, metaData).fetchJoin()
                .leftJoin(metaData.nimi, metadatanimi).fetchJoin()//.leftJoin(metadatanimi.values).fetchJoin()
                .leftJoin(metaData.hakutoimistoEctsEmailmkt, hakutoimistoEctsEmailmkt).fetchJoin()//.leftJoin(hakutoimistoEctsEmailmkt.values).fetchJoin()
                .leftJoin(metaData.hakutoimistoEctsNimimkt, hakutoimistoEctsNimimkt).fetchJoin()//.leftJoin(hakutoimistoEctsNimimkt.values).fetchJoin()
                .leftJoin(metaData.hakutoimistoEctsTehtavanimikemkt, hakutoimistoEctsTehtavanimikemkt).fetchJoin()//.leftJoin(hakutoimistoEctsTehtavanimikemkt.values).fetchJoin()
                .leftJoin(metaData.hakutoimistoEctsPuhelinmkt, hakutoimistoEctsPuhelinmkt)//.fetchJoin().leftJoin(hakutoimistoEctsPuhelinmkt.values).fetchJoin()
                .leftJoin(metaData.hakutoimistoNimi, hakutoimistoNimi).fetchJoin()//.leftJoin(hakutoimistoNimi.values).fetchJoin()
//                .leftJoin(metaData.values, metadataValues).fetchJoin()
//                .leftJoin(metadataValues.value, metadatavaluesvalue).fetchJoin().leftJoin(metadatavaluesvalue.values).fetchJoin()
//                .leftJoin(metaData.yhteystiedot, metadataYhteystieto).fetchJoin()
//                .leftJoin(org.nimet, nimet).fetchJoin()
//                .leftJoin(nimet.nimi, nimi).fetchJoin().leftJoin(nimi.values).fetchJoin()
//                .leftJoin(org.organisaatioLisatietotyypit, organisaatioLisatietotyyppi).fetchJoin()
//                .leftJoin(organisaatioLisatietotyyppi.lisatietotyyppi, lisatietotyyppi).fetchJoin()
                .leftJoin(org.varhaiskasvatuksenToimipaikkaTiedot, qVarhaiskasvatuksenToimipaikkaTiedot).fetchJoin()
//                .leftJoin(org.yhteystietoArvos, yhteystietoArvo).fetchJoin()
//                .leftJoin(yhteystietoArvo.kentta, kentta).fetchJoin()
//                .leftJoin(kentta.yhteystietojenTyyppi, yhteystietojenTyyppi).fetchJoin()
//                .leftJoin(yhteystietojenTyyppi.nimi, yhteystietojenTyyppiNimi).fetchJoin().leftJoin(yhteystietojenTyyppiNimi.values).fetchJoin()
//                .leftJoin(org.yhteystiedot).fetchJoin()

                .leftJoin(org.varhaiskasvatuksenToimipaikkaTiedot, vakaToimipaikkatiedot).fetchJoin()
//                .leftJoin(vakaToimipaikkatiedot.varhaiskasvatuksenJarjestamismuodot).fetchJoin()
//                .leftJoin(vakaToimipaikkatiedot.varhaiskasvatuksenKielipainotukset).fetchJoin()
//                .leftJoin(vakaToimipaikkatiedot.varhaiskasvatuksenToiminnallinenpainotukset).fetchJoin()
                // Parents. Fetching only essentials since this can have heavy impact on postgres distinct sort.
//                .leftJoin(org.parentSuhteet, parentSuhteet).fetchJoin()
//                .leftJoin(parentSuhteet.parent, parent).fetchJoin()
                .where(org.oid.in(oids));
        if (excludePoistettu) {
            jpaQuery.where(org.organisaatioPoistettu.isFalse());
        }
        List<Organisaatio> organisaatios = jpaQuery.fetch();
        Set<Long> monikielinenTekstiIds = Stream.of(
                organisaatios.stream().map(Organisaatio::getNimi),
                organisaatios.stream().map(Organisaatio::getKuvaus2),
                organisaatios.stream().map(Organisaatio::getMetadata).filter(Objects::nonNull).map(OrganisaatioMetaData::getNimi),
                organisaatios.stream().map(Organisaatio::getMetadata).filter(Objects::nonNull).map(OrganisaatioMetaData::getHakutoimistoNimi),
                organisaatios.stream().map(Organisaatio::getMetadata).filter(Objects::nonNull).map(OrganisaatioMetaData::getHakutoimistoEctsEmail),
                organisaatios.stream().map(Organisaatio::getMetadata).filter(Objects::nonNull).map(OrganisaatioMetaData::getHakutoimistoEctsNimi),
                organisaatios.stream().map(Organisaatio::getMetadata).filter(Objects::nonNull).map(OrganisaatioMetaData::getHakutoimistoEctsPuhelin),
                organisaatios.stream().map(Organisaatio::getMetadata).filter(Objects::nonNull).map(OrganisaatioMetaData::getHakutoimistoEctsTehtavanimike),
                organisaatios.stream().map(Organisaatio::getMetadata).filter(Objects::nonNull).map(OrganisaatioMetaData::getValues).filter(Objects::nonNull).flatMap(namedMonikielinenTekstis -> namedMonikielinenTekstis == null ? Stream.empty() : namedMonikielinenTekstis.stream().map(NamedMonikielinenTeksti::getValue))
        )
                .flatMap(monikielinenTekstiStream -> monikielinenTekstiStream)
                .filter(Objects::nonNull)
                .map(MonikielinenTeksti::getId)
                .collect(Collectors.toSet());
        QMonikielinenTeksti mkt = QMonikielinenTeksti.monikielinenTeksti;
        new JPAQuery<MonikielinenTeksti>(getEntityManager())
                .select(mkt)
                .from(mkt)
                .leftJoin(mkt.values).fetchJoin()
                .where(mkt.id.in(monikielinenTekstiIds))
                .fetch();
        return organisaatios;
    }

    /**
     *
     * @param oidList
     * @param maxResults
     * @return
     */
    @Override
    public List<Organisaatio> findDescendantsByOidList(List<String> oidList, int maxResults) {
        LOG.debug("findByOidList({}, {})", oidList, maxResults);

        // first drop nulls from oidList
        List<String> oidListFiltered = new ArrayList<>();
        for (String oid : oidList) {
            if (oid != null) {
                oidListFiltered.add(oid);
            }
        }

        QOrganisaatio qOrganisaatio = QOrganisaatio.organisaatio;
        List<Organisaatio> result = new ArrayList<>();

        for (String curOid : oidListFiltered) {
            result.addAll(new JPAQuery<>(getEntityManager()).from(qOrganisaatio)
                    .where((qOrganisaatio.oid.eq(curOid).or(qOrganisaatio.parentOidPath.like("%|" + curOid + "|%")))
                            .and(qOrganisaatio.organisaatioPoistettu.isFalse()))
                    .distinct()
                    .orderBy(qOrganisaatio.nimihaku.asc())
                    .select(qOrganisaatio).fetch());
        }

        return result;

    }

    public List<OrgPerustieto> findDescendantsBasicByOidList(List<String> oidList, int maxResults) {
        LOG.debug("findByOidList({}, {})", oidList, maxResults);

        // first drop nulls from oidList
        List<String> oidListFiltered = new ArrayList<>();
        for (String oid : oidList) {
            if (oid != null) {
                oidListFiltered.add(oid);
            }
        }

        QOrganisaatio qOrganisaatio = QOrganisaatio.organisaatio;
        List<OrgPerustieto> result = new ArrayList<>();

        for (String curOid : oidListFiltered) {
            result.addAll(new JPAQuery<>(getEntityManager()).from(qOrganisaatio)
                    .where((qOrganisaatio.oid.eq(curOid).or(qOrganisaatio.parentOidPath.like("%|" + curOid + "|%")))
                            .and(qOrganisaatio.organisaatioPoistettu.isFalse()))
                    .distinct()
                            //.orderBy(qOrganisaatio.nimihaku.asc())
                    .select(new QOrgPerustieto(qOrganisaatio.oid, qOrganisaatio.version, qOrganisaatio.alkuPvm, qOrganisaatio.lakkautusPvm,
                            qOrganisaatio.nimi, qOrganisaatio.ytunnus, qOrganisaatio.oppilaitosKoodi, qOrganisaatio.parentOidPath,
                            qOrganisaatio.organisaatiotyypitStr)).fetch());
        }

        return result;

    }

    /**
     * Palautateen organisaatiot, joiden oid on annetussa listassa, tai
     * joiden parenteissa esiintyy listan oid.
     *
     * @param oids
     * @return
     */
    @Override
    public List<OrgStructure> getOrganizationStructure(List<String> oids) {
        QOrganisaatio qOrganisaatio = QOrganisaatio.organisaatio;
        QMonikielinenTeksti nimi = QMonikielinenTeksti.monikielinenTeksti;

        JPAQuery<OrgStructure> q = new JPAQuery<>(getEntityManager());
        BooleanBuilder where = new BooleanBuilder();

        for (String oid : oids) {
            where.or(qOrganisaatio.parentOidPath.contains(oid));
            where.or(qOrganisaatio.oid.eq(oid));
        }

        JPAQuery<OrgStructure> query = q.from(qOrganisaatio)
                .leftJoin(qOrganisaatio.nimi, nimi)
                .leftJoin(nimi.values)
                .where(where);

        return query.distinct().select(new QOrgStructure(qOrganisaatio.oid, qOrganisaatio.parentOidPath,
                nimi, qOrganisaatio.organisaatioPoistettu, qOrganisaatio.lakkautusPvm))
                .fetch();
    }

    @Override
    public List<Organisaatio> findByOidList(List<String> oidList, int maxResults) {
        LOG.debug("findByOidList({}, {})", oidList, maxResults);

        // first drop nulls from oidList
        List<String> oidListFiltered = new ArrayList<>();
        for (String oid : oidList) {
            if (oid != null) {
                oidListFiltered.add(oid);
            }
        }

        // perform query
        EntityManager em = getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Organisaatio> query = cb.createQuery(Organisaatio.class);
        Root<Organisaatio> organisaatio = query.from(Organisaatio.class);
        query.orderBy(cb.asc(organisaatio.get("nimihaku")));
        Predicate where = cb.in(organisaatio.get("oid")).value(oidListFiltered);
        query.where(where);
        return getEntityManager().createQuery(query).setMaxResults(maxResults).getResultList();

    }

    /**
     * Return OID list of all organizations.
     *
     * @param myosPoistetut
     * @return
     */
    @Override
    public Collection<String> findAllOids(boolean myosPoistetut) {
        String q = "SELECT p.oid FROM Organisaatio p";

        if (!myosPoistetut) {
            q += " WHERE p.organisaatioPoistettu = false";
        }

        return (List<String>) getEntityManager().createQuery(q).getResultList();
    }

    /**
     * List OIDs of descendants for a given parent OID.
     *
     * @param parentOid
     * @param myosPoistetut
     * @return
     */
    @Override
    public Collection<String> listDescendantOids(String parentOid, boolean myosPoistetut) {
        parentOid = parentOid != null ? parentOid.trim() : null;
        if (parentOid == null) {
            return new ArrayList<>();
        }

        String parentOidStr = "%|" + parentOid + "|%";

        QOrganisaatio qOrganisaatio = QOrganisaatio.organisaatio;


        return new JPAQuery<>(getEntityManager()).from(qOrganisaatio)
                .where(qOrganisaatio.parentOidPath.like(parentOidStr).and(qOrganisaatio.organisaatioPoistettu.isFalse()))
                .distinct()
                .select(qOrganisaatio.oid)
                .fetch();
    }

    /**
     * List OIDs of descendants for a given parent OID.
     *
     * @param parentOid
     * @param vainPoistetut
     * @return
     */
    public List<Organisaatio> listDescendants(String parentOid, boolean vainPoistetut) {
        parentOid = parentOid != null ? parentOid.trim() : null;
        if (parentOid == null) {
            return new ArrayList<>();
        }

        String parentOidStr = "%|" + parentOid + "|%";

        QOrganisaatio qOrganisaatio = QOrganisaatio.organisaatio;

        return new JPAQuery<>(getEntityManager()).from(qOrganisaatio)
                .select(qOrganisaatio)
                .where(qOrganisaatio.parentOidPath.like(parentOidStr).and(qOrganisaatio.organisaatioPoistettu.eq(vainPoistetut)))
                .distinct()
                .fetch();

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
        Organisaatio org = findByOid(oid);

        if (org == null) {
            throw new OrganisaatioCrudException("organisaatio.not.found.with.oid");
        }

        // OVT-2391, cannot remove organization if there is child organizations)
        if (!findChildren(oid, false, false).isEmpty()) {
            throw new OrganisaatioCrudException("organisaatio.child.orgs.found");
        }

        // Mark removed
        org.setOrganisaatioPoistettu(true);
        getEntityManager().persist(org);

        return org.getParent();
    }

    /**
     * Return parent org oids to org, optimized for the auth use.
     *
     * @param oid
     * @return
     */
    public List<String> findParentOidsTo(String oid) {
        LOG.debug("findParentOidsTo({})", oid);
        Preconditions.checkNotNull(oid);
        List<String> oids = Lists.newArrayList();

        Organisaatio org = findByOid(oid);
        final String parentOidPath = org.getParentOidPath();


        QOrganisaatio qOrganisaatio = QOrganisaatio.organisaatio;
        QOrganisaatioSuhde qSuhde = QOrganisaatioSuhde.organisaatioSuhde;


        while (org != null) {
            oids.add(org.getOid());
            OrganisaatioSuhde curSuhde = organisaatioSuhdeDAO.findParentTo(org.getId(), new Date());
            org = (curSuhde != null) ? curSuhde.getParent() : null;
        }
        Collections.reverse(oids);
        return oids;
    }

    /**
     * Return parent org oids to org, optimized for the auth use.
     *
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
        LOG.debug("findParentOidsTo({})", oid);
        Preconditions.checkNotNull(oid);
        List<Organisaatio> parents = Lists.newArrayList();

        Organisaatio org = findByOid(oid);

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

        LOG.debug("findOidsBy({}, {}, {}, {}, {}, {})", new Object[] {requireYtunnus, count, startIndex, type});

        QOrganisaatio org = QOrganisaatio.organisaatio;
        BooleanBuilder whereExpr = new BooleanBuilder();
        whereExpr.and(org.organisaatioPoistettu.isFalse());
        // Select by Org tyyppi
        if (type != null) {
             whereExpr.and(org.tyypit.contains(type.koodiValue()));
//            whereExpr.and(org.organisaatiotyypitStr.like("%" + type.koodiValue() + "%"));
        }
        if(requireYtunnus) {
            whereExpr.and(org.ytunnus.isNotNull());
        }

        JPAQuery<String> q = new JPAQuery<>(getEntityManager());
        q = q.from(org);

        if (whereExpr != null) {
            q = q.where(whereExpr);
        }

        if (count > 0) {
            q = q.limit(count);
        }

        if (startIndex > 0) {
            q.offset(startIndex);
        }

        LOG.debug("  q = {}", q);

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
        LOG.debug("findByYtunnus({})", oid);
        QOrganisaatio org = QOrganisaatio.organisaatio;
        return new JPAQuery<>(getEntityManager()).from(org).where(org.ytunnus.eq(oid).and(org.organisaatioPoistettu.isFalse())).select(org).fetchFirst();
    }

    /**
     * Haetaan organisaatiota virastotunnuksen perusteella.
     *
     * @param oid
     * @return
     */
    @Override
    public Organisaatio findByVirastoTunnus(String oid) {
        LOG.debug("findByVirastotunnus({})", oid);
        QOrganisaatio org = QOrganisaatio.organisaatio;
        return new JPAQuery<>(getEntityManager()).from(org).where(org.virastoTunnus.eq(oid).and(org.organisaatioPoistettu.isFalse())).select(org).fetchFirst();
    }

    /**
     * Haetaan organisaatiota oppilaitoskoodin perusteella.
     *
     * @param oid
     * @return
     */
    @Override
    public Organisaatio findByOppilaitoskoodi(String oid) {
        LOG.debug("findByOppilaitoskoodi({})", oid);
        QOrganisaatio org = QOrganisaatio.organisaatio;
        return new JPAQuery<>(getEntityManager()).from(org).where(org.oppilaitosKoodi.eq(oid).and(org.organisaatioPoistettu.isFalse())).select(org).fetchFirst();
    }

    /**
     * Haetaan organisaatiota toimipistekoodin perusteella.
     *
     * @param oid
     * @return
     */
    @Override
    public Organisaatio findByToimipistekoodi(String oid) {
        LOG.debug("findByToimipisteKoodi({})", oid);
        QOrganisaatio org = QOrganisaatio.organisaatio;
        return new JPAQuery<>(getEntityManager()).from(org).where(org.toimipisteKoodi.eq(oid).and(org.organisaatioPoistettu.isFalse())).select(org).fetchFirst();
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

        LOG.debug("findBySearchCriteria()");

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

        List<Organisaatio> organisaatiot = new JPAQuery<>(getEntityManager())
                .from(org)
                .select(org)
                .limit(limit + 1)
                .where(whereExpression)
                //.distinct()
                .fetch();

        LOG.debug("Query took {} ms", System.currentTimeMillis() - qstarted);

        for (int i = 0; i < organisaatiot.size(); ++i) {
            LOG.debug("Organisaatio " + i + " " + organisaatiot.get(i).getNimi().getValues() +
                    " " + organisaatiot.get(i).getKotipaikka() + " " + organisaatiot.get(i).getNimihaku() +
                    " oid: " + organisaatiot.get(i).getOid() +
                    " luokat: " + organisaatiot.get(i).getVuosiluokat() +
                    " kielet: " + organisaatiot.get(i).getKielet());
        }

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

        // TODO: kotipaikka vielä kunta-koodiston uri ilman versiota --> version lisäyksen jälkeen: getUriVersionExpression()
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
        if (criteriaUri.matches(uriWithVersionRegExp)) {
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
     * AND organisaatiotyypitstr = 'Ryhma|'
     *
     *
     * Toinen tapa hakea on hakea kaikki ryhmät tyypit taulusta
     *
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
        LOG.debug("findGroups()");

        QOrganisaatio qOrganisaatio = QOrganisaatio.organisaatio;
        QMonikielinenTeksti qNimi = new QMonikielinenTeksti("nimi");
        QMonikielinenTeksti qKuvaus = new QMonikielinenTeksti("kuvaus");

        JPAQuery<Organisaatio> query = new JPAQuery<>(getEntityManager()).from(qOrganisaatio).select(qOrganisaatio)
                .join(qOrganisaatio.nimi, qNimi).fetchJoin()
                .join(qOrganisaatio.kuvaus2, qKuvaus).fetchJoin()
                .where(qOrganisaatio.organisaatiotyypitStr.eq("Ryhma|"));

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
        Optional.ofNullable(criteria.getParentOidPath()).ifPresent(parentOidPath -> {
            query.where(qOrganisaatio.parentOidPath.eq(parentOidPath));
        });
        Optional.ofNullable(criteria.getPoistettu()).ifPresent(poistettu -> {
            query.where(qOrganisaatio.organisaatioPoistettu.eq(poistettu));
        });

        return query.fetch();
    }

    @Override
    public void flush() {
        getEntityManager().flush();
    }

    @Override
    public EntityManager getJpaEntityManager() {
        return getEntityManager();
    }

    @Override
    public Collection<String> findChildOidsRecursive(ChildOidsCriteria criteria) {
        String sql = nativeQueryHelper.getSqlQueryAsString("classpath:sql/findChildOidsRecursive.sql", StandardCharsets.UTF_8);
        List<String> childOids = namedParameterJdbcTemplate.query(sql, new BeanPropertySqlParameterSource(criteria),
                (ResultSet rs, int rowNum) -> rs.getString("oid"));
        return childOids.stream().filter(childOid -> !childOid.equals(criteria.getOid())).collect(toSet());
    }

}
