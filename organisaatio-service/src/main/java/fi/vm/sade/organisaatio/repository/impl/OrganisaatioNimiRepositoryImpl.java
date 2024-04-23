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
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.model.OrganisaatioNimi;
import fi.vm.sade.organisaatio.repository.OrganisaatioNimiRepositoryCustom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.Date;
import java.util.List;

@Repository
public class OrganisaatioNimiRepositoryImpl implements OrganisaatioNimiRepositoryCustom {

    @Autowired
    EntityManager em;


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

        TypedQuery<OrganisaatioNimi> query = em.createNamedQuery(
                "OrganisaatioNimiDAO.findNimiByAlkupvmDate", OrganisaatioNimi.class);
        query.setParameter("id", organisaatio.getId());
        query.setParameter("date", nimi.getAlkuPvm());
        List<OrganisaatioNimi> organisaatioNimet = query.getResultList();

        if (organisaatioNimet.size() == 1) {
            return organisaatioNimet.get(0);
        } else if (organisaatioNimet.size() > 1) {
            return organisaatioNimet.stream().filter(foundNimi -> foundNimi.getNimi().getValues().equals(nimi.getNimi())).findFirst().orElse(null);
        }

        return null;
    }

    @Override
    public OrganisaatioNimi findCurrentNimi(Organisaatio organisaatio) {
        if (organisaatio == null) {
            throw new IllegalArgumentException("organisaatio cannot be null");
        }

        String s = "SELECT n FROM OrganisaatioNimi n "
                + "WHERE n.organisaatio.id = :id AND n.alkuPvm = ("
                + "     SELECT MAX (o.alkuPvm)"
                + "     FROM OrganisaatioNimi o"
                + "     WHERE o.organisaatio.id = :id AND o.alkuPvm <= :date)";

        TypedQuery<OrganisaatioNimi> q = em.createQuery(s, OrganisaatioNimi.class);
        List<OrganisaatioNimi> organisaatioNimet = q
                .setParameter("id", organisaatio.getId())
                .setParameter("date", new Date())
                .getResultList();

        if (organisaatioNimet.size() == 1) {
            return organisaatioNimet.get(0);
        }

        return null;
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
