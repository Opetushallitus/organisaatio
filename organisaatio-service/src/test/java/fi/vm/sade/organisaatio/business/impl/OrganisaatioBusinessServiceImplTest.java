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

package fi.vm.sade.organisaatio.business.impl;

import fi.vm.sade.organisaatio.SecurityAwareTestBase;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.business.OrganisaatioBusinessService;
import fi.vm.sade.organisaatio.dao.OrganisaatioDAO;
import fi.vm.sade.organisaatio.dto.mapping.SearchCriteriaModelMapper;
import fi.vm.sade.organisaatio.model.*;
import fi.vm.sade.organisaatio.resource.IndexerResource;
import fi.vm.sade.organisaatio.resource.dto.OrganisaatioRDTO;
import fi.vm.sade.organisaatio.service.converter.OrganisaatioToOrganisaatioRDTOConverter;
import fi.vm.sade.organisaatio.util.OrganisaatioRDTOTestUtil;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Tests for {@link fi.vm.sade.organisaatio.business.impl.OrganisaatioBusinessServiceImpl} class.
 * Created by vrouvine on 02/12/14.
 */
@ContextConfiguration(locations = {"classpath:spring/test-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles({"embedded-solr"})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class OrganisaatioBusinessServiceImplTest extends SecurityAwareTestBase {

    private final String rootOid = "1.2.246.562.24.00000000001";
    @Autowired
    private OrganisaatioDAO organisaatioDAO;
    @Autowired
    private OrganisaatioBusinessService service;
    @Autowired
    private IndexerResource indexer;
    @Autowired
    SearchCriteriaModelMapper searchCriteriaModelMapper;

    @Before
    public void setUp() {
        executeSqlScript("data/basic_organisaatio_data.sql", false);
        indexer.reBuildIndex(true);
    }

    @After
    public void tearDown() {
        executeSqlScript("data/truncate_tables.sql", false);
    }

    @Test
    public void processOrganisaatioSuhdeChangesNoNewChanges() {
        List<Organisaatio> results = service.processNewOrganisaatioSuhdeChanges();
        Assert.assertNotNull(results);
        Assert.assertTrue("Results should be empty!", results.isEmpty());
    }

    @Test
    public void processOrganisaatiosuhdeChangesOneNewChange() throws Exception {
        long parentId = 7L;
        long childId = 4L;
        String oldParentOid = "1.2.2004.1";
        String newParentOid = "1.2.2004.5";

        assertChildCountFromIndex(oldParentOid, 2);
        assertChildCountFromIndex(newParentOid, 0);

        // Make new organisaatiosuhde change
        Date time = new Date();
        jdbcTemplate.update("insert into organisaatiosuhde (id, version, suhdetyyppi, child_id, parent_id, alkupvm) values (9, 1, 'HISTORIA', ?, ?, ?)",
                new Object[] {childId, parentId, time});
        // End old organisaatiosuhde
        jdbcTemplate.update("update organisaatiosuhde set loppupvm = ? where id = ?", new Object[] {time, 3});

        Assert.assertEquals("Row count should match!", 9, countRowsInTable("organisaatiosuhde"));

        List<Organisaatio> results = service.processNewOrganisaatioSuhdeChanges();
        Assert.assertNotNull(results);
        Assert.assertEquals("Results size does not match!", 1, results.size());

        Organisaatio modified = results.get(0);
        Assert.assertEquals("Modified organisation does not match!", Long.valueOf(4), modified.getId());

        Assert.assertEquals("Parent oid path should match!", "|" + rootOid + "|" + newParentOid + "|", modified.getParentOidPath());

        String oid = "1.2.2004.4";
        Organisaatio org = organisaatioDAO.findByOid(oid);
        Assert.assertEquals("Parent oid path should match for oid: " + oid, modified.getParentOidPath() + modified.getOid() + "|", org.getParentOidPath());

        oid = "1.2.2005.4";
        Organisaatio org2 = organisaatioDAO.findByOid(oid);
        Assert.assertEquals("Parent oid path should match for oid: " + oid, modified.getParentOidPath() + modified.getOid() + "|", org2.getParentOidPath());

        oid = "1.2.2005.5";
        org2 = organisaatioDAO.findByOid(oid);
        Assert.assertEquals("Parent oid path should match for oid: " + oid, org.getParentOidPath() + org.getOid() + "|", org2.getParentOidPath());

        checkParent(modified, "1.2.2004.4");
        checkParent(modified, "1.2.2005.4");
        checkParent(org, "1.2.2005.5");
        checkParentOidPath(org, "1.2.2005.5");

        assertChildCountFromIndex(oldParentOid, 1);
        assertChildCountFromIndex(newParentOid, 1);
    }


    private Organisaatio checkParentOidPath(Organisaatio parent, String oid) {
        Organisaatio org = organisaatioDAO.findByOid(oid);
        Assert.assertEquals("Parent oid path should match for oid: " + oid, parent.getParentOidPath() + parent.getOid() + "|", org.getParentOidPath());
        return org;
    }

    private Organisaatio checkParent(Organisaatio parent, String oid) {
        Organisaatio org = organisaatioDAO.findByOid(oid);
        String pop = org.getParentOidPath();
        String[] list = pop.split("[|]+");
        String parentOid = list[list.length-1];
        Organisaatio orgParent = organisaatioDAO.findByOid(parentOid);
        Assert.assertEquals("Parent oid should match for oid: " + oid, parent.getOid(), orgParent.getOid());
        return org;
    }

    @Test
    public void editingRemovedIsNotAllowed() {
        OrganisaatioRDTO model = new OrganisaatioRDTO();
        String removedOid = "1.2.2004.4";
        model.setOid(removedOid);
        jdbcTemplate.update("update organisaatio set organisaatiopoistettu = TRUE where oid = ?", removedOid);
        try {
            service.save(model, true);
            Assert.fail("should throw ValidationException");
        } catch (Throwable e) {
            Assert.assertNotNull(e.getMessage());
            Assert.assertTrue(e.getMessage().equals("validation.Organisaatio.poistettu"));
        }
    }

    @Test
    public void saveNewAndUpdateOrganisation() {
        OrganisaatioToOrganisaatioRDTOConverter organisaatioToOrganisaatioRDTOConverter = new OrganisaatioToOrganisaatioRDTOConverter();
        OrganisaatioRDTO model = OrganisaatioRDTOTestUtil.createOrganisaatio("orgrandomnimijsdflsfsf", OrganisaatioTyyppi.MUU_ORGANISAATIO.value(), "65432.1", rootOid);
        model.setKayntiosoite(null);
        model.setYhteystiedot(null);
        model.setKieletUris(null);
        model.setAlkuPvm(new Date());
        OrganisaatioResult organisaatioResult = service.save(model, false);
        Assert.assertEquals("65432.1", organisaatioResult.getOrganisaatio().getOid());

        model = organisaatioToOrganisaatioRDTOConverter.convert(organisaatioResult.getOrganisaatio());
        organisaatioDAO.getJpaEntityManager().detach(organisaatioResult.getOrganisaatio());
        model.setYTunnus("4567891-0");
        organisaatioResult = service.save(model, true);
        Assert.assertEquals("4567891-0", organisaatioResult.getOrganisaatio().getYtunnus());

    }

}
