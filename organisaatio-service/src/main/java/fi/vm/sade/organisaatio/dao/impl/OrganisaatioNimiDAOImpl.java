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
import fi.vm.sade.organisaatio.model.QOrganisaatioNimi;
import java.util.Date;
import java.util.List;
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
    public OrganisaatioNimi addNimi(Long organisaatioId, MonikielinenTeksti nimi, Date alkuPvm, String paivittaja) {
        LOG.info("addNimi({}, {}, {})", new Object[]{organisaatioId, alkuPvm, nimi.getValues()});

        if (organisaatioId == null) {
            throw new IllegalArgumentException();
        }
        if (alkuPvm == null) {
            alkuPvm = new Date();
        }

        //
        // Luodaan uusi nimi organisaatiolle (nimihistorian entry)
        //
        Organisaatio organisaatio = organisaatioDAO.read(organisaatioId);

        OrganisaatioNimi organisaatioNimi = new OrganisaatioNimi();
        organisaatioNimi.setOrganisaatio(organisaatio);
        organisaatioNimi.setAlkuPvm(alkuPvm);
        organisaatioNimi.setNimi(nimi);
        organisaatioNimi.setPaivittaja(paivittaja);

        organisaatioNimi = insert(organisaatioNimi);

        return organisaatioNimi;
    }

    @Override
    public List<OrganisaatioNimi> findNimet(Long organisaatioId) {
        if (organisaatioId == null) {
            throw new IllegalArgumentException("organisaatioId cannot be null");
        }

        LOG.info("findNimet({})", organisaatioId);

        QOrganisaatioNimi qOrganisaatioNimi = QOrganisaatioNimi.organisaatioNimi;

        // Haetaan organisaatio
        BooleanExpression whereExpression = qOrganisaatioNimi.organisaatio.id.eq(organisaatioId);

        return new JPAQuery(getEntityManager())
                .from(qOrganisaatioNimi)
                .where(whereExpression)
                .distinct()
                .list(qOrganisaatioNimi);
    }

    @Override
    public List<OrganisaatioNimi> findNimet(String organisaatioOid) {
        Organisaatio organisaatio = organisaatioDAO.findByOid(organisaatioOid);

        return this.findNimet(organisaatio.getId());
    }

    public OrganisaatioNimi findNimi(Long organisaatioId, Date alkuPvm) {
        if (organisaatioId == null) {
            throw new IllegalArgumentException("organisaatioId cannot be null");
        }

        LOG.info("findNimi({}, {})", new Object[]{organisaatioId, alkuPvm});

        QOrganisaatioNimi qOrganisaatioNimi = QOrganisaatioNimi.organisaatioNimi;

        // Otetaan hakuun mukaan organisaatio
        BooleanExpression whereExpression = qOrganisaatioNimi.organisaatio.id.eq(organisaatioId);

        // Otetaan hakuun mukaan alkuPvm
        BooleanExpression alkuPvmExpr = qOrganisaatioNimi.organisaatio.alkuPvm.eq(alkuPvm);

        whereExpression = whereExpression.and(alkuPvmExpr);

        List<OrganisaatioNimi> organisaatioNimet = new JPAQuery(getEntityManager())
                .from(qOrganisaatioNimi)
                .where(whereExpression)
                .distinct()
                .list(qOrganisaatioNimi);


        if (organisaatioNimet.size() == 1) {
            return organisaatioNimet.get(0);
        }

        return null;
    }


    @Override
    public OrganisaatioNimi findNimi(String organisaatioOid, Date alkuPvm) {
        Organisaatio organisaatio = organisaatioDAO.findByOid(organisaatioOid);

        return this.findNimi(organisaatio.getId(), alkuPvm);
    }


}
