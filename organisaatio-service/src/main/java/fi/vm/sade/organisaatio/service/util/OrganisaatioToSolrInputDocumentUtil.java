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

package fi.vm.sade.organisaatio.service.util;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.model.OrganisaatioNimi;
import fi.vm.sade.organisaatio.service.search.SolrOrgFields;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;

/**
 * Add Organisaatio And Organisaatio Nimihistoria to SolrInputDocument so that it can be indexed.
 */
public abstract class OrganisaatioToSolrInputDocumentUtil extends SolrOrgFields {
    private static Logger LOG = LoggerFactory.getLogger("OrganisaatioToSolrInputDocumentUtil");

    public static SolrInputDocument apply(Organisaatio org) {
        Preconditions.checkNotNull(org);
        SolrInputDocument doc = new SolrInputDocument();
        add(doc, ALKUPVM, org.getAlkuPvm());
        add(doc, LAKKAUTUSPVM, org.getLakkautusPvm());
        add(doc, NIMIEN, org.getNimi().getString("en"));
        add(doc, NIMIFI, org.getNimi().getString("fi"));
        add(doc, NIMISV, org.getNimi().getString("sv"));

        // Lisätään organisaation nimihistoria hakuun
        addNimiHistoria(doc, NIMISEARCH, org.getNimet());

        // Haku mahdollista myös y-tunnuksella
        if (org.getYtunnus() != null) {
            add(doc, NIMISEARCH, org.getYtunnus());
        }

        // Haku mahdollista myös oppilaitoskoodilla
        if (org.getOppilaitosKoodi() != null) {
            add(doc, NIMISEARCH, org.getOppilaitosKoodi());
        }

        add(doc, OID, org.getOid());
        add(doc, OPPILAITOSKOODI, org.getOppilaitosKoodi());
        add(doc, ALIORGANISAATIOIDEN_LKM, org.getChildCount(new Date()));
        for (String tyyppi : org.getTyypit()) {
            add(doc, ORGANISAATIOTYYPPI, OrganisaatioTyyppi.fromKoodiValue(tyyppi).value());
        }
        for (String kieli : org.getKielet()) {
            add(doc, KIELI, kieli);
        }
        add(doc, DOMAINNIMI, org.getDomainNimi());
        add(doc, OPPILAITOSTYYPPI, org.getOppilaitosTyyppi());
        add(doc, TOIMIPISTEKOODI, org.getToimipisteKoodi());

        final Organisaatio parent = org.getParent();
        add(doc, PARENTOID, parent != null ? parent.getOid() : null);
        add(doc, YTUNNUS, org.getYtunnus());
        add(doc, VIRASTOTUNNUS, org.getVirastoTunnus());
        add(doc, KUNTA, org.getKotipaikka());
        do {
            add(doc, PATH, org.getOid());
            org = org.getParent();
        } while (org != null);
        return doc;
    }

    /**
     * Add field if koodiValue is not null
     */
    private static void add(SolrInputDocument doc, String fieldName, Object value) {
        if (value != null) {
            doc.addField(fieldName, value);
        }
    }

    private static void addNimiHistoria(SolrInputDocument doc, String fieldName, List<OrganisaatioNimi> nimet) {
        if (nimet.isEmpty()) {
            LOG.warn("Nimihistoriassa ei nimiä!");
        }
        for (OrganisaatioNimi nimi : nimet) {
            LOG.debug("Nimihistoria " + fieldName + ": " + Joiner.on(", ").join(nimi.getNimi().getValues().values()));

            // Lisätään nimi kaikilla kielillä
            for (String nimiValue : nimi.getNimi().getValues().values()) {
                add(doc, fieldName, nimiValue);
            }
        }
    }
}
