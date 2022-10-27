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

package fi.vm.sade.organisaatio.repository.impl;

import fi.vm.sade.organisaatio.dto.OrganisaatioNimiDTO;
import fi.vm.sade.organisaatio.model.MonikielinenTeksti;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.model.OrganisaatioNimi;
import fi.vm.sade.organisaatio.repository.OrganisaatioNimiRepository;
import fi.vm.sade.organisaatio.repository.OrganisaatioNimiRepositoryCustom;
import fi.vm.sade.organisaatio.repository.OrganisaatioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Repository
public class OrganisaatioNimiRepositoryImpl implements OrganisaatioNimiRepositoryCustom {

    private static final Logger LOG = LoggerFactory.getLogger(OrganisaatioNimiRepositoryImpl.class);

    @Autowired(required = true)
    OrganisaatioRepository organisaatioRepository;

    @Autowired(required = true)
    OrganisaatioNimiRepository organisaatioNimiRepository;

    @Autowired
    EntityManager em;

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

        organisaatioNimi = organisaatioNimiRepository.save(organisaatioNimi);

        return organisaatioNimi;
    }

    @Override
    public List<OrganisaatioNimi> findNimet(Organisaatio organisaatio) {
        if (organisaatio == null) {
            throw new IllegalArgumentException("organisaatio cannot be null");
        }

        LOG.info("findNimet({})", organisaatio.getOid());

        return organisaatioNimiRepository.findNimet(organisaatio);
    }

    @Override
    public List<OrganisaatioNimi> findNimet(String organisaatioOid) {
        TypedQuery<OrganisaatioNimi> query = em.createNamedQuery(
                "OrganisaatioNimiDAO.findNimet", OrganisaatioNimi.class);
        query.setParameter("organisaatioOid", organisaatioOid);
        return query.getResultList();
    }

    @Override
    public OrganisaatioNimi findNimi(Organisaatio organisaatio, OrganisaatioNimiDTO nimi) {
        if (organisaatio == null) {
            throw new IllegalArgumentException("organisaatio cannot be null");
        }

        LOG.debug("findNimi({}, {})",organisaatio.getId(), nimi.getAlkuPvm());

        // Kyselyä kokeilty myös QueryDsl:llä
        // Ongelmana oli se, että päivämäärän perusteella haku ei onnistunut jos
        // organisaation id:llä rivejä oli enemmän kuin 1


        String s = "SELECT n FROM OrganisaatioNimi n "
                + "WHERE "
                + "organisaatio_id = :id "
                + " and "
                + "alkupvm = :date ";

        TypedQuery q = em.createQuery(s, OrganisaatioNimi.class);

        List<OrganisaatioNimi> organisaatioNimet = q.setParameter("id", organisaatio.getId()).setParameter("date", nimi.getAlkuPvm()).getResultList();

        LOG.debug("findNimi() result size: {} ", organisaatioNimet.size());

        if (organisaatioNimet.size() == 1) {
            return organisaatioNimet.get(0);
        } else if (organisaatioNimet.size() > 1) {
            return organisaatioNimet.stream().filter(foundNimi -> foundNimi.getNimi().getValues().equals(nimi.getNimi())).findFirst().orElse(null);
        }

        LOG.debug("findNimi({}, {}) --> OrganisaatioNimi not found", organisaatio.getId(), nimi.getAlkuPvm());

        return null;
    }

    @Override
    public OrganisaatioNimi findCurrentNimi(Organisaatio organisaatio) {
        if (organisaatio == null) {
            throw new IllegalArgumentException("organisaatio cannot be null");
        }

        LOG.info("findCurrentNimi({})", organisaatio.getId());

        String s = "SELECT n FROM OrganisaatioNimi n "
                + "WHERE "
                + "organisaatio_id = :id "
                + " AND "
                + "alkupvm = (SELECT MAX (o.alkuPvm) "
                + "FROM OrganisaatioNimi o "
                + "WHERE "
                + "organisaatio_id = :id "
                + " AND "
                + "alkupvm <= :date)";

        TypedQuery<OrganisaatioNimi> q = em.createQuery(s, OrganisaatioNimi.class);

        List<OrganisaatioNimi> organisaatioNimet = q.setParameter("id", organisaatio.getId()).setParameter("date", new Date()).getResultList();

        LOG.info("findCurrentNimi() result size: {}", organisaatioNimet.size());

        if (organisaatioNimet.size() == 1) {
            return organisaatioNimet.get(0);
        }

        LOG.info("findNimi({}) --> OrganisaatioNimi not found", organisaatio.getId());

        return null;
    }


    @Override
    public OrganisaatioNimi findCurrentNimi(String organisaatioOid) {
        Organisaatio organisaatio = organisaatioRepository.findFirstByOid(organisaatioOid);

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
     * <p>
     * Ylläoleva SQL lauseke on alla kirjoitettu HQL muotoon.
     *
     * @return
     **/
    @Override
    public List<Organisaatio> findNimiNotCurrentOrganisaatiot() {

        String s = "SELECT org FROM Organisaatio org "
                + "WHERE org.nimi != "
                + "( "
                + "SELECT org_nimi.nimi FROM OrganisaatioNimi org_nimi "
                + "WHERE org_nimi.alkuPvm = "
                + "( "
                + "SELECT MAX (org_nimi2.alkuPvm) FROM OrganisaatioNimi org_nimi2 "
                + "WHERE org_nimi.organisaatio = org_nimi2.organisaatio "
                + "AND org_nimi2.alkuPvm <= :date "
                + ") "
                + "AND org = org_nimi.organisaatio "
                + ")";

        TypedQuery<Organisaatio> q = em.createQuery(s, Organisaatio.class);

        return q.setParameter("date", new Date()).getResultList();
    }

}
