/*
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
package fi.vm.sade.organisaatio.service.search;

import java.util.Date;

import org.apache.solr.common.SolrInputDocument;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;

import fi.vm.sade.organisaatio.model.Organisaatio;

/**
 * Convert {@link Organisaatio} to {@link SolrInputDocument} so that it can be
 * indexed.
 */
public class OrganisaatioToSolrInputDocumentFunction extends SolrOrgFields implements
        Function<Organisaatio, SolrInputDocument> {

    @Override
    public SolrInputDocument apply(Organisaatio org) {
        Preconditions.checkNotNull(org);
        SolrInputDocument doc = new SolrInputDocument();
        add(doc, ALKUPVM, org.getAlkuPvm());
        add(doc, LAKKAUTUSPVM, org.getLakkautusPvm());
        add(doc, NIMIEN, org.getNimi().getString("en"));
        add(doc, NIMISEARCH, org.getNimi().getString("en"));
        add(doc, NIMIFI, org.getNimi().getString("fi"));
        add(doc, NIMISEARCH, org.getNimi().getString("fi"));
        add(doc, NIMISV, org.getNimi().getString("sv"));
        add(doc, NIMISEARCH, org.getNimi().getString("sv"));
        if (org.getYtunnus() != null) { //allow searching by y-tunnus
            add(doc, NIMISEARCH, org.getYtunnus());
        }
        if (org.getOppilaitosKoodi() != null) { //allow searching by tk-koodi (oppilaitoskoodi)
            add(doc, NIMISEARCH, org.getOppilaitosKoodi());
        }
        add(doc, OID, org.getOid());
        add(doc, OPPILAITOSKOODI, org.getOppilaitosKoodi());
        add(doc, ALIORGANISAATIOIDEN_LKM, org.getChildCount(null,new Date()));
        for (String tyyppi : org.getTyypit()) {
            add(doc, ORGANISAATIOTYYPPI, tyyppi);
        }
        add(doc, DOMAINNIMI, org.getDomainNimi());
        add(doc, OPPILAITOSTYYPPI, org.getOppilaitosTyyppi());

        final Organisaatio parent = org.getParent();
        add(doc, PARENTOID, parent != null ? parent.getOid() : null);
        add(doc, YTUNNUS, org.getYtunnus());
        add(doc, KUNTA, org.getKotipaikka());
        do {
            add(doc, PATH, org.getOid());
            org = org.getParent();
        } while (org != null);
        return doc;
    }

    /**
     * Add field if value is not null
     * 
     * @param doc
     * @param nimifi
     * @param string
     */
    private void add(SolrInputDocument doc, String fieldName, Object value) {
        if (value != null) {
            doc.addField(fieldName, value);
        }
    }
}
