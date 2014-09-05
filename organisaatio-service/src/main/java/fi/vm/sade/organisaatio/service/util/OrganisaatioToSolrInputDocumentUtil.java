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

import fi.vm.sade.organisaatio.service.search.SolrOrgFields;
import java.util.Date;

import org.apache.solr.common.SolrInputDocument;

import com.google.common.base.Preconditions;

import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.model.OrganisaatioNimi;
import java.util.List;
import org.apache.commons.lang.time.DateUtils;

/**
 * Add Organisaatio And Organisaatio Nimihistoria to SolrInputDocument so that it can be indexed.
 */
public abstract class OrganisaatioToSolrInputDocumentUtil extends SolrOrgFields {
    public static SolrInputDocument apply(Organisaatio org, List<OrganisaatioNimi> nimet) {
        Preconditions.checkNotNull(org);
        SolrInputDocument doc = new SolrInputDocument();
        add(doc, ALKUPVM, org.getAlkuPvm());
        add(doc, LAKKAUTUSPVM, org.getLakkautusPvm());
        add(doc, NIMIEN, org.getNimi().getString("en"));
        add(doc, NIMIFI, org.getNimi().getString("fi"));
        add(doc, NIMISV, org.getNimi().getString("sv"));

        // Lisätään organisaation nimihistoria hakuun
        addNimiHistoria(doc, NIMISEARCH, nimet);

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
        add(doc, ALIORGANISAATIOIDEN_LKM, org.getChildCount(null,new Date()));
        for (String tyyppi : org.getTyypit()) {
            add(doc, ORGANISAATIOTYYPPI, tyyppi);
        }
        for (String kieli : org.getKielet()) {
            add(doc, KIELI, kieli);
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
     */
    private static void add(SolrInputDocument doc, String fieldName, Object value) {
        if (value != null) {
            doc.addField(fieldName, value);
        }
    }

    private static void addNimiHistoria(SolrInputDocument doc, String fieldName, List<OrganisaatioNimi> nimet) {
        for (OrganisaatioNimi nimi : nimet) {
            Date today = new Date();
            if (nimi.getAlkuPvm() != null) {
                // Lisätään nimi kaikilla kielillä, jos nimen voimassaolopäivämäärä on tänään tai aiemmin
                if (nimi.getAlkuPvm().before(today) || DateUtils.isSameDay(nimi.getAlkuPvm(), today)) {
                    for (String nimiValue : nimi.getNimi().getValues().values())
                        add(doc, fieldName, nimiValue);
                }
            }
        }
    }
}
