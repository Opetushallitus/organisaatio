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

package fi.vm.sade.organisaatio.dao.impl;

import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.expr.BooleanExpression;
import fi.vm.sade.generic.dao.AbstractJpaDAOImpl;
import fi.vm.sade.organisaatio.dao.OrganisaatioNimiDAO;
import fi.vm.sade.organisaatio.model.MonikielinenTeksti;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.model.OrganisaatioNimi;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * @author simok
 */
@Repository
public class OrganisaatioNimiDAOImpl extends AbstractJpaDAOImpl<OrganisaatioNimi, Long> implements OrganisaatioNimiDAO {

    private static final Logger LOG = LoggerFactory.getLogger(OrganisaatioNimiDAOImpl.class);

    @Autowired(required = true)
    OrganisaatioDAOImpl organisaatioDAO;

    @Override
    public OrganisaatioNimi addNimi(Organisaatio organisaatio, MonikielinenTeksti nimi, Date alkuPvm, String paivittaja) {
        if (organisaatio == null) {
            throw new IllegalArgumentException();
        }

        LOG.info("addNimi({}, {}, {})", new Object[]{organisaatio.getOid(), alkuPvm, nimi.getValues()});

        if (alkuPvm == null) {
            alkuPvm = new Date();
        }

        //
        // Luodaan uusi nimi organisaatiolle (nimihistorian entry)
        //
        OrganisaatioNimi organisaatioNimi = new OrganisaatioNimi();
        organisaatioNimi.setOrganisaatio(organisaatio);
        organisaatioNimi.setAlkuPvm(alkuPvm);
        organisaatioNimi.setNimi(nimi);
        organisaatioNimi.setPaivittaja(paivittaja);

        organisaatioNimi = insert(organisaatioNimi);

        return organisaatioNimi;
    }

    @Override
    public List<OrganisaatioNimi> findNimet(Organisaatio organisaatio) {
        LOG.info("findNimet({})", organisaatio.getOid());

        return findBy("organisaatio", organisaatio);
    }

    @Override
    public List<OrganisaatioNimi> findNimet(String organisaatioOid) {
        Organisaatio organisaatio = organisaatioDAO.findByOid(organisaatioOid);

        return this.findNimet(organisaatio);
    }

    @Override
    public OrganisaatioNimi findNimi(Organisaatio organisaatio, Date alkuPvm) {
        if (organisaatio == null) {
            throw new IllegalArgumentException("organisaatio cannot be null");
        }

        LOG.info("findNimi({}, {})", new Object[]{organisaatio.getId(), alkuPvm});

        // Kyselyä kokeilty myös QueryDsl:llä
        // Ongelmana oli se, että päivämäärän perusteella haku ei onnistunut jos
        // organisaation id:llä rivejä oli enemmän kuin 1

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

        String s = "SELECT n FROM OrganisaatioNimi n "
                + "WHERE "
                + "organisaatio_id = " + organisaatio.getId()
                + " and "
                + "alkupvm = '" + df.format(alkuPvm) + "'";

        Query q = getEntityManager().createQuery(s);

        List<OrganisaatioNimi> organisaatioNimet = (List<OrganisaatioNimi>) q.getResultList();

        LOG.info("findNimi() result size: " + organisaatioNimet.size());

        if (organisaatioNimet.size() == 1) {
            return organisaatioNimet.get(0);
        }

        LOG.info("findNimi({}, {}) --> OrganisaatioNimi not found", new Object[]{organisaatio.getId(), alkuPvm});

        return null;
    }


    @Override
    public OrganisaatioNimi findNimi(String organisaatioOid, Date alkuPvm) {
        Organisaatio organisaatio = organisaatioDAO.findByOid(organisaatioOid);

        return this.findNimi(organisaatio, alkuPvm);
    }

    @Override
    public OrganisaatioNimi findCurrentNimi(Organisaatio organisaatio) {
        if (organisaatio == null) {
            throw new IllegalArgumentException("organisaatio cannot be null");
        }

        LOG.info("findCurrentNimi({})", new Object[]{organisaatio.getId()});

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

        String s = "SELECT n FROM OrganisaatioNimi n "
                + "WHERE "
                + "organisaatio_id = " + organisaatio.getId()
                + " AND "
                + "alkupvm = (SELECT MAX (o.alkuPvm) "
                + "FROM OrganisaatioNimi o "
                + "WHERE "
                + "organisaatio_id = " + organisaatio.getId()
                + " AND "
                + "alkupvm <= '" + df.format(new Date()) + "')";

        Query q = getEntityManager().createQuery(s);

        List<OrganisaatioNimi> organisaatioNimet = (List<OrganisaatioNimi>) q.getResultList();

        LOG.info("findCurrentNimi() result size: " + organisaatioNimet.size());

        if (organisaatioNimet.size() == 1) {
            return organisaatioNimet.get(0);
        }

        LOG.info("findNimi({}) --> OrganisaatioNimi not found", new Object[]{organisaatio.getId()});

        return null;
    }


    @Override
    public OrganisaatioNimi findCurrentNimi(String organisaatioOid) {
        Organisaatio organisaatio = organisaatioDAO.findByOid(organisaatioOid);

        return this.findCurrentNimi(organisaatio);
    }


    /**
     * Haetaan organisaatiot, joiden nimi eroaa nimihistorian current nimestä
     * -----------------------------------------------------------------------
     * SELECT org.*
     * FROM organisaatio org
     * WHERE org.nimi_mkt !=
     * (
     * SELECT org_nimi.nimi_mkt
     * FROM organisaatio_nimi org_nimi
     * WHERE org_nimi.alkupvm =
     * (
     * SELECT max(org_nimi2.alkupvm)
     * FROM organisaatio_nimi org_nimi2
     * WHERE org_nimi.organisaatio_id = org_nimi2.organisaatio_id
     * AND org_nimi2.alkupvm <= '2014-09-01'
     * )
     * AND org.id = org_nimi.organisaatio_id
     * )
     *
     * Ylläoleva SQL lauseke on alla kirjoitettu HQL muotoon.
     *
     * @return
     **/
    @Override
    public List<Organisaatio> findNimiNotCurrentOrganisaatiot() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

        String s = "SELECT org FROM Organisaatio org "
                + "WHERE org.nimi != "
                + "( "
                + "SELECT org_nimi.nimi FROM OrganisaatioNimi org_nimi "
                + "WHERE org_nimi.alkuPvm = "
                + "( "
                + "SELECT MAX (org_nimi2.alkuPvm) FROM OrganisaatioNimi org_nimi2 "
                + "WHERE org_nimi.organisaatio = org_nimi2.organisaatio "
                + "AND org_nimi2.alkuPvm <= '" + df.format(new Date()) + "' "
                + ") "
                + "AND org = org_nimi.organisaatio "
                + ")";

        Query q = getEntityManager().createQuery(s);

        List<Organisaatio> organisaatiot = (List<Organisaatio>) q.getResultList();

        return organisaatiot;
    }

}
