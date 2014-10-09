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
package fi.vm.sade.organisaatio.integrationtest.lop;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import fi.vm.sade.organisaatio.model.MonikielinenTeksti;
import fi.vm.sade.organisaatio.model.NamedMonikielinenTeksti;
import fi.vm.sade.organisaatio.model.OrganisaatioMetaData;
import fi.vm.sade.tarjoaja.service.KoulutustarjoajaPublicServiceImpl;
import org.junit.Before;
import org.powermock.reflect.Whitebox;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.model.Yhteystieto;
import fi.vm.sade.tarjoaja.service.GenericFault;
import fi.vm.sade.tarjoaja.service.types.FindByOrganizationOidRequestType;
import fi.vm.sade.tarjoaja.service.types.FindByOrganizationOidResponseType;
import fi.vm.sade.tarjoaja.service.types.MetatietoAvainTyyppi;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author mlyly
 */
@ContextConfiguration(locations = {
    "classpath:spring/test-context.xml"
})
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
public class LopTest {

    private static final Logger LOG = LoggerFactory.getLogger(LopTest.class);
    @PersistenceContext
    private EntityManager em;
    private static final String OID = "organisatio.oid.123456";
    private static final String LOP_NIMI_EN1 = "name en1";
    private static final String LOP_NIMI_EN2 = "name en2";
    private static final String LOP_NIMI_FI1 = "nimi fi1";
    private static final String LOP_NIMI_FI2 = "nimi fi2";
    private static final String LOP_KOODI1 = "1234567890";
    private static final String LOP_KOODI2 = "0000000000";
    private static final String LOP_VALUE_EN1 = "value en1";
    private static final String LOP_VALUE_FI1 = "value fi1";
    private static final String LOP_VALUE_SV1 = "value sv1";
    private static final String LOP_VALUE_EN2 = "value en2";
    private static final String LOP_VALUE_FI2 = "value fi2";
    private static final String LOP_VALUE_SV2 = "value sv2";
    private static final String KEY1 = MetatietoAvainTyyppi.OPPIMISYMPARISTOT.toString();
    private static final String KEY2 = MetatietoAvainTyyppi.TERVEYDENHUOLTO.toString();
    private KoulutustarjoajaPublicServiceImpl service = new KoulutustarjoajaPublicServiceImpl();
    private OrganisaatioMetaData lop1;

    @Before
    public void setUp() {
        assertNotNull(em);

        Whitebox.setInternalState(service, "em", em);

        lop1 = new OrganisaatioMetaData();
        // lop.setOid(LOP_OID1);
        lop1.setNimi("en", LOP_NIMI_EN1);
        lop1.setNimi("fi", LOP_NIMI_FI1);

        lop1.setKoodi(LOP_KOODI1);
        // lop.setOrganisaatioOid(LOP_OWNER_ORG_OID1);

        lop1.setNamedValue(KEY1, "en", LOP_VALUE_EN1);
        lop1.setNamedValue(KEY1, "fi", LOP_VALUE_FI1);
        lop1.setNamedValue(KEY1, "sv", LOP_VALUE_SV1);
        lop1.setNamedValue(KEY2, "sv", LOP_VALUE_SV1);

        MonikielinenTeksti mt = new MonikielinenTeksti();
        mt.setId(111l);

        mt.addString(MetatietoAvainTyyppi.OPPIMISYMPARISTOT.value(), "metatieto");
        em.persist(mt);

        List<Yhteystieto> yhteystiedot = new ArrayList<Yhteystieto>();

        Yhteystieto y = new Yhteystieto();
        y.setId(1l);
        y.setYhteystietoOid("y-oid");

        em.persist(y);
        yhteystiedot.add(y);

        Organisaatio o = new Organisaatio();
        o.setOid(OID);
        y.setOrganisaatio(o);
        o.setYhteystiedot(yhteystiedot);
        o.setNimi(mt);
        em.persist(o);

        lop1.setOrganisation(o);
        o.setMetadata(lop1);
        em.persist(lop1);
        em.flush();
    }

    @Test
    public void testFindByOrganizationOid() throws GenericFault {
        FindByOrganizationOidRequestType t = new FindByOrganizationOidRequestType();
        t.setOid(OID);
        FindByOrganizationOidResponseType findByOrganizationOid = service.findByOrganizationOid(t);
        assertNotNull(findByOrganizationOid.getReturn());
        assertEquals("nimi", false, findByOrganizationOid.getReturn().getNimi().isEmpty());
        assertEquals("oid", OID, findByOrganizationOid.getReturn().getOrganisaatioOid());
    }

    @Test
    public void testCreateLOP() {
        LOG.info("testCreateLOP()");
        // Reload
        OrganisaatioMetaData lop = service.findById(lop1.getId());
        assertNotNull(lop);

        // Verify

        // assertTrue("oid", lop.getOid().equals(LOP_OID1));
        assertTrue("koodi", lop.getKoodi().equals(LOP_KOODI1));
        assertTrue("key 1 - en", lop.getNamedValue(KEY1, "en").equals(LOP_VALUE_EN1));
        assertTrue("key 1 - fi", lop.getNamedValue(KEY1, "fi").equals(LOP_VALUE_FI1));
        assertTrue("key 1 - sv", lop.getNamedValue(KEY1, "sv").equals(LOP_VALUE_SV1));
        assertTrue("key 2 - sv", lop.getNamedValue(KEY2, "sv").equals(LOP_VALUE_SV1));
        // assertTrue("organisaatio oid", LOP_OWNER_ORG_OID1.equals(lop.getOrganisaatioOid()));

        // Update

        // lop.setOid(LOP_OID2);
        lop.setNimi("en", LOP_NIMI_EN2);
        lop.setNimi("fi", LOP_NIMI_FI2);

        lop.setKoodi(LOP_KOODI2);
        // lop.setOrganisaatioOid(LOP_OWNER_ORG_OID2);

        lop.setNamedValue(KEY1, "en", LOP_VALUE_EN2);
        lop.setNamedValue(KEY1, "fi", LOP_VALUE_FI2);
        lop.setNamedValue(KEY1, "sv", LOP_VALUE_SV2);
        lop.setNamedValue(KEY2, "sv", LOP_VALUE_SV2);

        em.persist(lop);
        em.flush();

        // Reload

        lop = service.findById(lop.getId());
        assertNotNull(lop);

        // Verify

        // assertTrue("oid", lop.getOid().equals(LOP_OID2));
        assertTrue("koodi", lop.getKoodi().equals(LOP_KOODI2));
        assertTrue("key 1 - en", lop.getNamedValue(KEY1, "en").equals(LOP_VALUE_EN2));
        assertTrue("key 1 - fi", lop.getNamedValue(KEY1, "fi").equals(LOP_VALUE_FI2));
        assertTrue("key 1 - sv", lop.getNamedValue(KEY1, "sv").equals(LOP_VALUE_SV2));
        assertTrue("key 2 - sv", lop.getNamedValue(KEY2, "sv").equals(LOP_VALUE_SV2));
        // assertTrue("organisaatio oid", LOP_OWNER_ORG_OID2.equals(lop.getOrganisaatioOid()));
    }

    private void print(NamedMonikielinenTeksti mkt) {
        LOG.info("  NamedMonikielinenTeksti = {}", mkt);
        LOG.info("    key = {}", mkt.getKey());
        print(mkt.getValue());
    }

    private void print(MonikielinenTeksti mkt) {
        LOG.info("    MonikielinenTeksti = {}", mkt);
        if (mkt != null) {
            LOG.info("    MonikielinenTeksti.values = {}", mkt.getValues());
        }
    }
}
