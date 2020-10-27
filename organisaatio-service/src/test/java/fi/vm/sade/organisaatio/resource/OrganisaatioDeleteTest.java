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
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Primary;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import fi.vm.sade.organisaatio.SecurityAwareTestBase;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.business.impl.OrganisaatioTarjonta;
import fi.vm.sade.organisaatio.resource.dto.OrganisaatioRDTO;
import fi.vm.sade.organisaatio.util.OrganisaatioRDTOTestUtil;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.kubek2k.springockito.annotations.ReplaceWithMock; // TODO replacement ??
import org.kubek2k.springockito.annotations.SpringockitoContextLoader;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

@RunWith(SpringRunner.class)
@Transactional
@ComponentScan(basePackages = "fi.vm.sade.organisaatio")
@SpringBootTest
@AutoConfigureTestDatabase
public class OrganisaatioDeleteTest {

    private OrganisaatioRDTO a, ab, abc, ad;

    @TestConfiguration
    public static class TestConfig {
        @Bean
        @Primary
        public OrganisaatioTarjonta tarjontaMock(){
            OrganisaatioTarjonta tarjonta = Mockito.mock(OrganisaatioTarjonta.class);
            return tarjonta;
        }

    }
    @Autowired
    OrganisaatioTarjonta tarjontaMock;

    @Autowired
    OrganisaatioResource res;

    private static final Logger LOG = LoggerFactory.getLogger(OrganisaatioDeleteTest.class);


    @Before
    public void setUp() {
    }

    @Test
    @WithMockUser(roles = {"APP_ORGANISAATIOHALLINTA", "APP_ORGANISAATIOHALLINTA_CRUD_1.2.246.562.24.00000000001", "APP_ORGANISAATIOHALLINTA_CRUD"})
    public void testParentDelete() throws Exception {
        LOG.info("testParentDelete()...");


        //      A
        //     / \
        //   AB  AD (koulutuksia)
        //   /
        // ABC

        a   = createOrganisaatio("A", null);
        ab  = createOrganisaatio("AB", a);
        abc = createOrganisaatio("ABC", ab);
        ad  = createOrganisaatio("AD", a);

        //MockitoAnnotations.initMocks(this);

        // Mock toteutukset alkavien koulutusten pyynnöille
        when(tarjontaMock.alkaviaKoulutuksia(ab.getOid())).thenReturn(false);
        when(tarjontaMock.alkaviaKoulutuksia(ad.getOid())).thenReturn(true);
        when(tarjontaMock.alkaviaKoulutuksia(abc.getOid())).thenReturn(false);

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
    @WithMockUser(roles = {"APP_ORGANISAATIOHALLINTA", "APP_ORGANISAATIOHALLINTA_CRUD_1.2.246.562.24.00000000001", "APP_ORGANISAATIOHALLINTA_CRUD"})
    public void testAlkaviaKoulutuksiaDelete() throws Exception {
        LOG.info("testAlkaviaKoulutuksiaDelete()...");

        //      A
        //     / \
        //   AB  AD (koulutuksia)
        //   /
        // ABC

        a   = createOrganisaatio("A", null);
        ab  = createOrganisaatio("AB", a);
        abc = createOrganisaatio("ABC", ab);
        ad  = createOrganisaatio("AD", a);

        //MockitoAnnotations.initMocks(this);

        // Mock toteutukset alkavien koulutusten pyynnöille
        when(tarjontaMock.alkaviaKoulutuksia(ab.getOid())).thenReturn(false);
        when(tarjontaMock.alkaviaKoulutuksia(ad.getOid())).thenReturn(true);
        when(tarjontaMock.alkaviaKoulutuksia(abc.getOid())).thenReturn(false);

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
    @WithMockUser(roles = {"APP_ORGANISAATIOHALLINTA", "APP_ORGANISAATIOHALLINTA_CRUD_1.2.246.562.24.00000000001", "APP_ORGANISAATIOHALLINTA_CRUD"})
    public void testSuccessfulDelete() throws Exception {
        LOG.info("testSuccessfulDelete()...");


        //      A
        //     / \
        //   AB  AD (koulutuksia)
        //   /
        // ABC

        a   = createOrganisaatio("A", null);
        ab  = createOrganisaatio("AB", a);
        abc = createOrganisaatio("ABC", ab);
        ad  = createOrganisaatio("AD", a);

        //MockitoAnnotations.initMocks(this);

        // Mock toteutukset alkavien koulutusten pyynnöille
        when(tarjontaMock.alkaviaKoulutuksia(ab.getOid())).thenReturn(false);
        when(tarjontaMock.alkaviaKoulutuksia(ad.getOid())).thenReturn(true);
        when(tarjontaMock.alkaviaKoulutuksia(abc.getOid())).thenReturn(false);

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
