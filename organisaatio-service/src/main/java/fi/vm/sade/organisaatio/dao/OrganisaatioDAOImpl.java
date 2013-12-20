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
package fi.vm.sade.organisaatio.dao;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.mysema.query.BooleanBuilder;
import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.expr.BooleanExpression;
import fi.vm.sade.generic.dao.AbstractJpaDAOImpl;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.model.*;
import fi.vm.sade.organisaatio.model.dto.OrgPerustieto;
import fi.vm.sade.organisaatio.model.dto.OrgStructure;
import fi.vm.sade.organisaatio.model.dto.QOrgPerustieto;
import fi.vm.sade.organisaatio.model.dto.QOrgStructure;
import fi.vm.sade.organisaatio.service.OrganisaatioCrudException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.*;
import org.hibernate.criterion.LikeExpression;

/**
 * @author tommiha
 * @author mlyly
 */
@Repository
public class OrganisaatioDAOImpl extends AbstractJpaDAOImpl<Organisaatio, Long> {

    @Value("${root.organisaatio.oid}")
    private String ophOid;
    protected final Logger log = LoggerFactory.getLogger(getClass());
    @Autowired
    OrganisaatioSuhdeDAOImpl organisaatioSuhdeDAO;

    /**
     * Find the children of given parent organisation.
     *
     * @param parentId
     * @return
     */
    public List<Organisaatio> findChildren(Long parentId) {
        log.debug("findChildren({})", parentId);

        QOrganisaatio qOrganisaatio = QOrganisaatio.organisaatio;

        BooleanExpression whereExpression = qOrganisaatio.parentIdPath.endsWith("|" + parentId + "|");

        return new JPAQuery(getEntityManager())
                .from(qOrganisaatio)
                .where(whereExpression)
                .distinct()
                .list(qOrganisaatio);
    }

    public int countChildren(String parentOid) {
        return (Integer) getEntityManager().createQuery("FROM " + Organisaatio.class.getName() + " WHERE parentOid=? AND organisaatioPoistettu IS NOT NULL")
                .setParameter(1, parentOid)
                .getSingleResult();
    }

    /**
     * Find childers for given Organisation with OID.
     *
     * @param parentOid
     * @param myosPoistetut if true return also "removed" orgs
     * @return
     */
    public List<Organisaatio> findChildren(String parentOid, boolean myosPoistetut, boolean myosLakkautetut) {
        log.debug("findChildren({})", parentOid);

        Organisaatio parent = this.findByOid(parentOid);
        List<Organisaatio> result = new ArrayList<Organisaatio>();
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
        log.debug("findByDomainNimi");
        return findBy("domainNimi", domainNimi);
    }

    public boolean isYtunnusAvailable(String ytunnus) {
        return ((Number) getEntityManager()
                .createQuery("SELECT COUNT(*) FROM " + Organisaatio.class.getName() + " WHERE ytunnus=? AND organisaatiopoistettu IS NOT NULL")
                .setParameter(1, ytunnus.trim())
                .getSingleResult()).intValue() == 0;
    }

    public List<Organisaatio> findOrganisaatioByNimiLike(String organisaatioNimi, int firstResult, int maxResults) {
        log.debug("findOrganisaatioByNimiLike()");
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
        log.debug("findBySearchCriteria()");

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

        List<OrgPerustieto> organisaatiot = new JPAQuery(getEntityManager())
                .from(qOrganisaatio)
                .where(whereExpression)
                .distinct()
                        //.orderBy(qOrganisaatio1.nimihaku.asc())
                .limit(maxResults + 1)
                .list(new QOrgPerustieto(qOrganisaatio.oid, qOrganisaatio.version, qOrganisaatio.alkuPvm, qOrganisaatio.lakkautusPvm, qOrganisaatio.nimi, qOrganisaatio.ytunnus, qOrganisaatio.oppilaitosKoodi, qOrganisaatio.parentOidPath, qOrganisaatio.organisaatiotyypitStr));

        log.debug("Query took {} ms", System.currentTimeMillis() - qstarted);

        organisaatiot = retrieveParentsAndChildren(organisaatiot, new TreeSet<String>(oids), suunnitellut, lakkautetut);

        return organisaatiot;
    }

    private void appendParentOrganisation(List<OrgPerustieto> ret, String poid, boolean suunnitellut, boolean lakkautetut) {

        QOrganisaatio qOrganisaatio = new QOrganisaatio("a");
        BooleanExpression whereExpression = qOrganisaatio.oid.eq(poid);
        if (!lakkautetut) {
            whereExpression = whereExpression.and(qOrganisaatio.organisaatioPoistettu.eq(false));
        }

        OrgPerustieto po = new JPAQuery(getEntityManager())
                .from(qOrganisaatio)
                .where(whereExpression)
                .uniqueResult(new QOrgPerustieto(qOrganisaatio.oid, qOrganisaatio.version, qOrganisaatio.alkuPvm, qOrganisaatio.lakkautusPvm, qOrganisaatio.nimi, qOrganisaatio.ytunnus, qOrganisaatio.oppilaitosKoodi, qOrganisaatio.parentOidPath, qOrganisaatio.organisaatiotyypitStr));

        if (po != null) {
            ret.add(po);
        }
    }

    private void appendChildOrganisations(List<OrgPerustieto> ret, Set<String> procOids, OrgPerustieto parent, Set<String> oids, boolean suunnitellut, boolean lakkautetut) {

        String noidPath = parent.getParentOidPath() + parent.getOid() + "|";

        QOrganisaatio qOrganisaatio = new QOrganisaatio("a");
        BooleanExpression whereExpression = qOrganisaatio.parentOidPath.startsWith(noidPath);
        if (!lakkautetut) {
            whereExpression = whereExpression.and(qOrganisaatio.organisaatioPoistettu.eq(false));
        }

        List<OrgPerustieto> pos = new JPAQuery(getEntityManager())
                .from(qOrganisaatio)
                .where(whereExpression)
                .list(new QOrgPerustieto(qOrganisaatio.oid, qOrganisaatio.version, qOrganisaatio.alkuPvm, qOrganisaatio.lakkautusPvm, qOrganisaatio.nimi, qOrganisaatio.ytunnus, qOrganisaatio.oppilaitosKoodi, qOrganisaatio.parentOidPath, qOrganisaatio.organisaatiotyypitStr));

        for (OrgPerustieto pt : pos) {
            if (procOids.add(pt.getOid())) {
                ret.add(pt);
            }
        }

    }

    private List<OrgPerustieto> retrieveParentsAndChildren(List<OrgPerustieto> baseResult, Set<String> oids, boolean suunnitellut, boolean lakkautetut) {
        Set<String> procOids = new TreeSet<String>();
        procOids.add(ophOid);
        List<OrgPerustieto> ret = new ArrayList<OrgPerustieto>();

        Set<String> ppoids = new TreeSet<String>();

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

    public List<OrgPerustieto> findBySearchCriteriaExact(String orgTyyppi,
                                                         String oppilaitosTyyppi,
                                                         String kunta,
                                                         String searchStr,
                                                         boolean suunnitellut,
                                                         boolean lakkautetut,
                                                         int maxResults,
                                                         List<String> oids) {
        log.debug("findBySearchCriteria()");

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

        List<OrgPerustieto> organisaatiot = new JPAQuery(getEntityManager())
                .from(qOrganisaatio)
                .where(whereExpression)
                .distinct()
                .limit(maxResults + 1)
                .list(new QOrgPerustieto(qOrganisaatio.oid, qOrganisaatio.version, qOrganisaatio.alkuPvm, qOrganisaatio.lakkautusPvm,
                        qOrganisaatio.nimi, qOrganisaatio.ytunnus, qOrganisaatio.oppilaitosKoodi, qOrganisaatio.parentOidPath,
                        qOrganisaatio.organisaatiotyypitStr));
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
        log.debug("getStringExpression()");
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
        log.debug("getVoimassaoloExpression()");
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

    public Organisaatio findByOid(String oid) {
        log.debug("findByOid({})", oid);


        oid = oid != null ? oid.trim() : null;
        try {
            List<Organisaatio> organisaatios = findBy("oid", oid);
            if (organisaatios.size() == 1) {
                return organisaatios.get(0);
            }
        } catch (Exception ex) {
            log.info(ex.getMessage());
        }
        return null;
    }

    public List<Organisaatio> findDescendantsByOidList(List<String> oidList, int maxResults) {
        log.debug("findByOidList({}, {})", oidList, maxResults);

        // first drop nulls from oidList
        List<String> oidListFiltered = new ArrayList<String>();
        for (String oid : oidList) {
            if (oid != null) {
                oidListFiltered.add(oid);
            }
        }

        QOrganisaatio qOrganisaatio = QOrganisaatio.organisaatio;
        List<Organisaatio> result = new ArrayList<Organisaatio>();

        for (String curOid : oidListFiltered) {
            result.addAll(new JPAQuery(getEntityManager()).from(qOrganisaatio)
                    .where((qOrganisaatio.oid.eq(curOid).or(qOrganisaatio.parentOidPath.like("%|" + curOid + "|%")))
                            .and(qOrganisaatio.organisaatioPoistettu.isFalse()))
                    .distinct()
                    .orderBy(qOrganisaatio.nimihaku.asc())
                    .list(qOrganisaatio));
        }

        return result;

    }

    public List<OrgPerustieto> findDescendantsBasicByOidList(List<String> oidList, int maxResults) {
        log.debug("findByOidList({}, {})", oidList, maxResults);

        // first drop nulls from oidList
        List<String> oidListFiltered = new ArrayList<String>();
        for (String oid : oidList) {
            if (oid != null) {
                oidListFiltered.add(oid);
            }
        }

        QOrganisaatio qOrganisaatio = QOrganisaatio.organisaatio;
        List<OrgPerustieto> result = new ArrayList<OrgPerustieto>();

        for (String curOid : oidListFiltered) {
            result.addAll(new JPAQuery(getEntityManager()).from(qOrganisaatio)
                    .where((qOrganisaatio.oid.eq(curOid).or(qOrganisaatio.parentOidPath.like("%|" + curOid + "|%")))
                            .and(qOrganisaatio.organisaatioPoistettu.isFalse()))
                    .distinct()
                            //.orderBy(qOrganisaatio.nimihaku.asc())
                    .list(new QOrgPerustieto(qOrganisaatio.oid, qOrganisaatio.version, qOrganisaatio.alkuPvm, qOrganisaatio.lakkautusPvm,
                            qOrganisaatio.nimi, qOrganisaatio.ytunnus, qOrganisaatio.oppilaitosKoodi, qOrganisaatio.parentOidPath,
                            qOrganisaatio.organisaatiotyypitStr)));
        }

        return result;

    }

    public List<OrgStructure> getOrganizationStructure(List<String> oids) {
        QOrganisaatio qOrganisaatio = QOrganisaatio.organisaatio;
        QMonikielinenTeksti nimi = QMonikielinenTeksti.monikielinenTeksti;

        JPAQuery q = new JPAQuery(getEntityManager());
        BooleanBuilder where = new BooleanBuilder();

        for (String oid : oids) {
            where.or(qOrganisaatio.parentOidPath.contains(oid));
            where.or(qOrganisaatio.oid.eq(oid));
        }

        JPAQuery query = q.from(qOrganisaatio)
                .leftJoin(qOrganisaatio.nimi, nimi)
                .leftJoin(nimi.values)
                .where(where);

        return query.distinct().list(new QOrgStructure(qOrganisaatio.oid, qOrganisaatio.parentOidPath,
                nimi, qOrganisaatio.organisaatioPoistettu, qOrganisaatio.lakkautusPvm));
    }

    public List<Organisaatio> findByOidList(List<String> oidList, int maxResults) {
        log.debug("findByOidList({}, {})", oidList, maxResults);

        // first drop nulls from oidList
        List<String> oidListFiltered = new ArrayList<String>();
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
    public Collection<String> listDescendantOids(String parentOid, boolean myosPoistetut) {
        parentOid = parentOid != null ? parentOid.trim() : null;
        if (parentOid == null) {
            return new ArrayList<String>();
        }

        String parentOidStr = "%|" + parentOid + "|%";

        QOrganisaatio qOrganisaatio = QOrganisaatio.organisaatio;


        return new JPAQuery(getEntityManager()).from(qOrganisaatio)
                .where(qOrganisaatio.parentOidPath.like(parentOidStr).and(qOrganisaatio.organisaatioPoistettu.isFalse()))
                .distinct()
                .list(qOrganisaatio.oid);
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
            return new ArrayList<Organisaatio>();
        }

        String parentOidStr = "%|" + parentOid + "|%";

        QOrganisaatio qOrganisaatio = QOrganisaatio.organisaatio;

        return new JPAQuery(getEntityManager()).from(qOrganisaatio)
                .where(qOrganisaatio.parentOidPath.like(parentOidStr).and(qOrganisaatio.organisaatioPoistettu.eq(vainPoistetut ? null : false)))
                .distinct()
                .list(qOrganisaatio);

    }

    /**
     * Mark Organisation to be removed. Can only be done if it dows not have any
     * active children.
     *
     * @param oid
     * @return parent
     */
    public Organisaatio markRemoved(String oid) {
        Organisaatio org = findByOid(oid);

        if (org == null) {
            throw new OrganisaatioCrudException("organisaatio.not.found.with.oid");
        }

        // OVT-2391, cannot remove organization if there is child organizations)
        if (!findChildren(oid, false, false).isEmpty()) {
            throw new OrganisaatioCrudException("child.orgs.found");
        }

        // Mark removed
        org.setOrganisaatioPoistettu(true);
        getEntityManager().persist(org);

        return org.getParent();
    }

    public HistoryMetadata findCurrentHistoryMetadata(Long organisaatioId, String key, String kieli) {
        return findPreviousValidHistoryMetadata(organisaatioId, key, kieli, new Date());
    }

    public HistoryMetadata findPreviousValidHistoryMetadata(Long organisaatioId, String avain, String kieli, Date atTime) {

        if (organisaatioId == null || avain == null || kieli == null || atTime == null) {
            throw new IllegalArgumentException("orgId, avain, kieli, aika has to be specified.");
        }

        StringBuilder sb = new StringBuilder();

        sb.append("SELECT a FROM HistoryMetadata a ");
        sb.append("WHERE ");
        sb.append("a.organisaatio.id = :organisaatioId AND ");
        sb.append("a.avain = :avain AND ");
        sb.append("a.kieli = :kieli AND ");
        sb.append("a.aika <= :aika ");
        sb.append("ORDER BY a.aika DESC");

        Query q = getEntityManager().createQuery(sb.toString());
        q.setParameter("organisaatioId", organisaatioId);
        q.setParameter("avain", avain);
        q.setParameter("kieli", kieli);
        q.setParameter("aika", atTime);
        q.setMaxResults(1);

        HistoryMetadata result = (HistoryMetadata) q.getSingleResult();
        return result;
    }

    public void addHistoriaMetadata(Long organisaatioId, String avain, String kieli, Date atTime, String arvo) {

        Organisaatio organisaatio = read(organisaatioId);

        HistoryMetadata hmd = new HistoryMetadata();
        hmd.setOrganisaatio(organisaatio);
        hmd.setAvain(avain);
        hmd.setKieli(kieli);
        hmd.setAika(atTime);
        hmd.setArvo(arvo);

        getEntityManager().persist(hmd);
    }

    /**
     * Return parent org oids to org, optimized for the auth use.
     *
     * @return
     */
    public List<String> findParentOidsTo(String oid) {
        log.debug("findParentOidsTo({})", oid);
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
     * @return
     */
    public List<Organisaatio> findParentsTo(String oid) {
        log.debug("findParentOidsTo({})", oid);
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
     * @param searchTerms
     * @param count
     * @param startIndex
     * @param lastModifiedBefore
     * @param lastModifiedSince
     * @parem type Organisation type
     * @return
     */
    public List<String> findOidsBy(String searchTerms, int count, int startIndex, Date lastModifiedBefore, Date lastModifiedSince, OrganisaatioTyyppi type) {

        log.debug("findOidsBy({}, {}, {}, {}, {}, {})", new Object[] {searchTerms, count, startIndex, lastModifiedBefore, lastModifiedSince, type});

        QOrganisaatio org = QOrganisaatio.organisaatio;
        BooleanExpression whereExpr = null;

        // Select by Org tyyppi
        if (type != null) {
            // whereExpr = org.tyypit.contains(type.value());
            whereExpr = org.organisaatiotyypitStr.like("%" + type.value() + "%");
        }

// TODO org + lastmodified?
//        if (lastModifiedBefore != null) {
//            whereExpr = and(whereExpr, org.updated.before(lastModifiedBefore));
//        }
//
//        if (lastModifiedSince != null) {
//            whereExpr = and(whereExpr, org.updated.after(lastModifiedSince));
//        }


        JPAQuery q = new JPAQuery(getEntityManager());
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

        log.debug("  q = {}", q);

        return q.list(org.oid);
    }

    public Organisaatio findByYTunnus(String oid) {
        log.debug("findByYtunnus({})", oid);
        QOrganisaatio org = QOrganisaatio.organisaatio;
        return new JPAQuery(getEntityManager()).from(org).where(org.ytunnus.eq(oid)).singleResult(org);
    }

    public Organisaatio findByVirastoTunnus(String oid) {
        log.debug("findByVirastotunnus({})", oid);
        QOrganisaatio org = QOrganisaatio.organisaatio;
        return new JPAQuery(getEntityManager()).from(org).where(org.virastoTunnus.eq(oid)).singleResult(org);
    }

    public Organisaatio findByOppilaitoskoodi(String oid) {
        log.debug("findByOppilaitoskoodi({})", oid);
        QOrganisaatio org = QOrganisaatio.organisaatio;
        return new JPAQuery(getEntityManager()).from(org).where(org.oppilaitosKoodi.eq(oid)).singleResult(org);
    }

    public Organisaatio findByToimipistekoodi(String oid) {
        log.debug("findByToimipisteKoodi({})", oid);
        QOrganisaatio org = QOrganisaatio.organisaatio;
        return new JPAQuery(getEntityManager()).from(org).where(org.toimipisteKoodi.eq(oid)).singleResult(org);
    }
}
