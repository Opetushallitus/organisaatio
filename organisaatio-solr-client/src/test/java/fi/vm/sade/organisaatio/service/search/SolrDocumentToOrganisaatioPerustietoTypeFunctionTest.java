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

import static fi.vm.sade.organisaatio.service.search.SolrOrgFields.*;   
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.HashSet;

import org.apache.solr.common.SolrDocument;
import org.junit.Test;


import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.api.search.OrganisaatioPerustieto;

public class SolrDocumentToOrganisaatioPerustietoTypeFunctionTest {

    @Test
    public void test() {
        final SolrDocumentToOrganisaatioPerustietoTypeFunction converter = new SolrDocumentToOrganisaatioPerustietoTypeFunction(new HashSet<String>());

        final SolrDocument doc = new SolrDocument();

        doc.addField(ALKUPVM, new Date(1));
        doc.addField(DOMAINNIMI, "foo.fi");
        doc.addField(KUNTA, "Espoo");
        doc.addField(LAKKAUTUSPVM, new Date(2));
        doc.addField(NIMIEN, "Espoo En");
        doc.addField(NIMIFI, "Espoo Fi");
        doc.addField(NIMISV, "Espoo Sv");
        doc.addField(OID, "1.2.3.4.5.6.7");
        doc.addField(OPPILAITOSKOODI, "123456");
        doc.addField(TOIMIPISTEKOODI, "1234567");
        doc.addField(ORGANISAATIOTYYPPI, OrganisaatioTyyppi.KOULUTUSTOIMIJA.value());
        doc.addField(ORGANISAATIOTYYPPI, OrganisaatioTyyppi.OPPILAITOS.value());
        doc.addField(PARENTOID, "1.1.1.1.1.1.1");
        doc.addField(PATH, "1.2.3.4.5.6.7");
        doc.addField(PATH, "1.1.1.1.1.1.1");
        doc.addField(YTUNNUS, "1234567-8");
        final OrganisaatioPerustieto result = converter.apply(doc);
        assertEquals(doc.getFieldValue(ALKUPVM), result.getAlkuPvm());
        assertEquals(doc.getFieldValue(LAKKAUTUSPVM), result.getLakkautusPvm());
        assertEquals(doc.getFieldValue(NIMIEN), result.getNimi("en"));
        assertEquals(doc.getFieldValue(NIMIFI), result.getNimi("fi"));
        assertEquals(doc.getFieldValue(NIMISV), result.getNimi("sv"));
        assertEquals(doc.getFieldValue(OID), result.getOid());
        assertEquals(doc.getFieldValue(OPPILAITOSKOODI), result.getOppilaitosKoodi());
        assertEquals(doc.getFieldValue(TOIMIPISTEKOODI), result.getToimipistekoodi());
        assertEquals(doc.getFieldValue(PARENTOID), result.getParentOid());
        assertEquals(doc.getFieldValues(ORGANISAATIOTYYPPI).size(), result.getOrganisaatiotyypit().size());
        assertTrue(doc.getFieldValues(ORGANISAATIOTYYPPI).contains(OrganisaatioTyyppi.KOULUTUSTOIMIJA.value()));
        assertTrue(doc.getFieldValues(ORGANISAATIOTYYPPI).contains(OrganisaatioTyyppi.OPPILAITOS.value()));
        assertEquals(doc.getFieldValue(PARENTOID), result.getParentOid());
        assertEquals(2, doc.getFieldValues(PATH).size());
        assertEquals(doc.getFieldValue(YTUNNUS), result.getYtunnus());
    
    }

}
