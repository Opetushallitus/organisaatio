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

package fi.vm.sade.organisaatio.resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import fi.vm.sade.organisaatio.SecurityAwareTestBase;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.business.impl.OrganisaatioTarjonta;
import fi.vm.sade.organisaatio.resource.dto.OrganisaatioRDTO;
import fi.vm.sade.organisaatio.util.OrganisaatioRDTOTestUtil;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.kubek2k.springockito.annotations.ReplaceWithMock;
import org.kubek2k.springockito.annotations.SpringockitoContextLoader;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

@ContextConfiguration(loader = SpringockitoContextLoader.class, locations = { "classpath:spring/test-context.xml" })
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
public class OrganisaatioDeleteTest extends SecurityAwareTestBase {

    @Autowired
    OrganisaatioResource res;

    @ReplaceWithMock
    @Autowired
    private OrganisaatioTarjonta tarjonta;

    private static final Logger LOG = LoggerFactory.getLogger(OrganisaatioDeleteTest.class);

    private OrganisaatioRDTO a, ab, abc, ad;

    @Before
    public void setUp() {
        LOG.info("setUp()...");

        //      A
        //     / \
        //   AB  AD (koulutuksia)
        //   /
        // ABC

        a   = createOrganisaatio("A", null);
        ab  = createOrganisaatio("AB", a);
        abc = createOrganisaatio("ABC", ab);
        ad  = createOrganisaatio("AD", a);

        MockitoAnnotations.initMocks(this);

        // Mock toteutukset alkavien koulutusten pyynnöille
        when(tarjonta.alkaviaKoulutuksia(ab.getOid())).thenReturn(false);
        when(tarjonta.alkaviaKoulutuksia(ad.getOid())).thenReturn(true);
        when(tarjonta.alkaviaKoulutuksia(abc.getOid())).thenReturn(false);
    }

    @Test
    public void testParentDelete() throws Exception {
        LOG.info("testParentDelete()...");

        // Organisaatiota ei saa poistaa, jos sillä on aliorganisaatioita
        try {
            res.deleteOrganisaatio(ab.getOid());
            fail();
        } catch (OrganisaatioResourceException e) {
            // expected
            // could also check for message of exception, etc.
        }
    }

    @Test
    public void testAlkaviaKoulutuksiaDelete() throws Exception {
        LOG.info("testAlkaviaKoulutuksiaDelete()...");

        // Organisaatiota ei saa poistaa, jos sillä on alkavia koulutuksia
        try {
            res.deleteOrganisaatio(ad.getOid());
            fail();
        } catch (OrganisaatioResourceException e) {
            // expected
            // could also check for message of exception, etc.
        }
    }

    @Test
    public void testSuccessfulDelete() throws Exception {
        LOG.info("testSuccessfulDelete()...");

        // Organisaatiolla ei ole aliorganisaatioita eikä alkavia koulutuksia
        // --> saa poistaa
        res.deleteOrganisaatio(abc.getOid());
    }

    private OrganisaatioRDTO createOrganisaatio(String nimi, OrganisaatioRDTO parent) {
        LOG.info("createOrganisaatio({})", nimi);

        OrganisaatioRDTO o = OrganisaatioRDTOTestUtil.createOrganisaatio(nimi, OrganisaatioTyyppi.MUU_ORGANISAATIO.value(), parent);

        return res.newOrganisaatio(o).getOrganisaatio();
    }

}
