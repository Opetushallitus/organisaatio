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

import static fi.vm.sade.organisaatio.service.search.SolrOrgFields.ALKUPVM;
import static fi.vm.sade.organisaatio.service.search.SolrOrgFields.LAKKAUTUSPVM;
import static fi.vm.sade.organisaatio.service.search.SolrOrgFields.NIMIEN;
import static fi.vm.sade.organisaatio.service.search.SolrOrgFields.NIMIFI;
import static fi.vm.sade.organisaatio.service.search.SolrOrgFields.NIMISV;
import static fi.vm.sade.organisaatio.service.search.SolrOrgFields.OID;
import static fi.vm.sade.organisaatio.service.search.SolrOrgFields.OPPILAITOSKOODI;
import static fi.vm.sade.organisaatio.service.search.SolrOrgFields.ORGANISAATIOTYYPPI;
import static fi.vm.sade.organisaatio.service.search.SolrOrgFields.PARENTOID;
import static fi.vm.sade.organisaatio.service.search.SolrOrgFields.PATH;
import static fi.vm.sade.organisaatio.service.search.SolrOrgFields.YTUNNUS;
import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.apache.solr.common.SolrInputDocument;
import org.junit.Test;

import com.google.common.collect.Lists;

import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.model.MonikielinenTeksti;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.model.OrganisaatioSuhde;

public class OrganisaatioToSolrInputDocumentFunctionTest {

    @Test
    public void test() {
        final OrganisaatioToSolrInputDocumentFunction converter = new OrganisaatioToSolrInputDocumentFunction();
        Organisaatio org = new Organisaatio();
        
        org.setAlkuPvm(new Date(1));
        org.setLakkautusPvm(new Date());
        org.setDomainNimi("domain.fi");
        org.setKotipaikka("Espoo");
        MonikielinenTeksti nimi = new MonikielinenTeksti();
        nimi.addString("fi", "Espoo fi");
        nimi.addString("en", "Espoo en");
        nimi.addString("sv", "Espoo sv");
        org.setNimi(nimi);
        org.setOid("1.2.3.4.5.6.7");
        org.setOppilaitosKoodi("123456");
        final OrganisaatioSuhde suhde = new OrganisaatioSuhde();
        final Organisaatio parent = new Organisaatio();
        suhde.setParent(parent);
        suhde.setChild(org);
        suhde.setAlkuPvm(new Date());
        org.getParentSuhteet().add(suhde);
        org.setParentSuhteet(Lists.newArrayList(suhde));
        parent.setOid("1.1.1.1.1.1");
        org.setTyypit(Lists.newArrayList(OrganisaatioTyyppi.KOULUTUSTOIMIJA.value()));
        org.setYtunnus("123456-7");

        SolrInputDocument doc = converter.apply(org);
        
        assertEquals(org.getAlkuPvm(), doc.getFieldValue(ALKUPVM));
        assertEquals(org.getLakkautusPvm(), doc.getFieldValue(LAKKAUTUSPVM));
        assertEquals(org.getNimi().getString("en"), doc.getFieldValue(NIMIEN));
        assertEquals(org.getNimi().getString("fi"), doc.getFieldValue(NIMIFI));
        assertEquals(org.getNimi().getString("sv"), doc.getFieldValue(NIMISV));
        assertEquals(org.getOid(), doc.getFieldValue(OID));
        assertEquals(org.getOppilaitosKoodi(), doc.getFieldValue(OPPILAITOSKOODI));
        assertEquals(org.getParentSuhteet().get(0).getParent().getOid(), doc.getFieldValue(PARENTOID));
        assertEquals(org.getTyypit().size(), doc.getFieldValues(ORGANISAATIOTYYPPI).size());
        assertEquals(2, doc.getFieldValues(PATH).size());
        assertEquals(org.getYtunnus(), doc.getFieldValue(YTUNNUS));
    }

}
