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
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.model.OrganisaatioResult;
import fi.vm.sade.organisaatio.model.Osoite;
import fi.vm.sade.organisaatio.resource.IndexerResource;
import fi.vm.sade.organisaatio.resource.dto.OrganisaatioRDTO;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
        simpleJdbcTemplate.update("insert into organisaatiosuhde (id, version, suhdetyyppi, child_id, parent_id, alkupvm) values (9, 1, 'HISTORIA', ?, ?, ?)",
                childId, parentId, time);
//        jdbcTemplate.update("insert into organisaatiosuhde (id, version, suhdetyyppi, child_id, parent_id, alkupvm) values (9, 1, 'HISTORIA', ?, ?, ?)",
//                new Object[] {childId, parentId, time});
        // End old organisaatiosuhde
//        jdbcTemplate.update("update organisaatiosuhde set loppupvm = ? where id = ?", new Object[] {time, 3});
        simpleJdbcTemplate.update("update organisaatiosuhde set loppupvm = ? where id = ?", time, 3);

        Assert.assertEquals("Row count should match!", 9, countRowsInTable("organisaatiosuhde"));

        List<Organisaatio> results = service.processNewOrganisaatioSuhdeChanges();
        Assert.assertNotNull(results);
        Assert.assertEquals("Results size does not match!", 1, results.size());

        Organisaatio modified = results.get(0);
        Assert.assertEquals("Modified organisation does not match!", Long.valueOf(4), modified.getId());

        Assert.assertEquals("Parent oid path should match!", "|" + rootOid + "|" + newParentOid + "|", modified.getParentOidPath());

        Organisaatio org = checkParentOidPath(modified, "1.2.2004.4");
        checkParentOidPath(modified, "1.2.2005.4");
        checkParentOidPath(org, "1.2.2005.5");

        checkParent(modified, "1.2.2004.4");
        checkParent(modified, "1.2.2005.4");
        checkParent(org, "1.2.2005.5");


        assertChildCountFromIndex(oldParentOid, 1);
        assertChildCountFromIndex(newParentOid, 1);
    }

    @Test
    public void editingRemovedIsNotAllowed() {
        OrganisaatioRDTO model = new OrganisaatioRDTO();
        String removedOid = "1.2.2004.4";
        model.setOid(removedOid);
        simpleJdbcTemplate.update("update organisaatio set organisaatiopoistettu = TRUE where oid = ?", removedOid);
        OrganisaatioResult organisaatioResult;
        try {
            organisaatioResult = service.save(model, true, true);
            Assert.fail("should throw ValidationException");
        } catch (Throwable e) {
            Assert.assertNotNull(e.getMessage());
            Assert.assertTrue(e.getMessage().equals("validation.Organisaatio.poistettu"));
        }
    }

    private Organisaatio checkParentOidPath(Organisaatio parent, String oid) {
        Organisaatio org = organisaatioDAO.findByOid(oid);
        Assert.assertEquals("Parent oid path should match for oid: " + oid, parent.getParentOidPath() + parent.getOid() + "|", org.getParentOidPath());
        return org;
    }

    private Organisaatio checkParent(Organisaatio parent, String oid) {
        Organisaatio org = organisaatioDAO.findByOid(oid);
        Assert.assertEquals("Parent oid should match for oid: " + oid, parent.getOid(), org.getParent().getOid());
        return org;
    }

    @Test
    public void addNewNameFromYTJToNameHistoryTest() {
        // TODO make a test case with several names in the name history and new name from YTJ
        // and see that everything goes correctly
        Assert.assertTrue(true);
    }

    @Test
    public void updateYTJDataTest() {
        int updatedOrganisations;
        updatedOrganisations = service.updateYTJData();
        // verify that the database is updated properly
        List<String> oidList = new ArrayList<>();
        List<Organisaatio> organisaatioList;
        oidList.addAll(organisaatioDAO.findOidsBy(true, 10000, 0, OrganisaatioTyyppi.KOULUTUSTOIMIJA));
        oidList.addAll(organisaatioDAO.findOidsBy(true, 10000, 0, OrganisaatioTyyppi.TYOELAMAJARJESTO));
        oidList.addAll(organisaatioDAO.findOidsBy(true, 10000, 0, OrganisaatioTyyppi.MUU_ORGANISAATIO));
        organisaatioList = organisaatioDAO.findByOidList(oidList, 10000);
        //TODO: mock oid gen?
        // What should be put to nimi alkupvm updated from YTJ?
        Assert.assertEquals(3, updatedOrganisations);
        Assert.assertEquals(3, organisaatioList.size());

        // Case: Has sv name, gets new fi name from YTJ
        Assert.assertEquals(1, organisaatioList.get(0).getNimet().size());
        Assert.assertEquals("Helsingin yliopistomuseon säätiö", organisaatioList.get(0).getNimet().get(0).getNimi().getString("fi"));
        Assert.assertEquals("node231 foo bar", organisaatioList.get(0).getNimet().get(0).getNimi().getString("sv"));
        Assert.assertEquals("Mannerheimintie 2", organisaatioList.get(0).getPostiosoite().getOsoite());
        Assert.assertEquals("Tie 1", ((Osoite)organisaatioList.get(0).getYhteystiedot().get(1)).getOsoite());
        Assert.assertEquals("oppilaitoksenopetuskieli_1#1", organisaatioList.get(0).getKielet().get(0));

        // Case: Has fi and sv name, gets fi updated from YTJ
        Assert.assertEquals(2, organisaatioList.get(1).getNimet().size());
        Assert.assertEquals("Katva Consulting", organisaatioList.get(1).getNimet().get(0).getNimi().getString("fi"));
        Assert.assertEquals("root test utbildningsoperatör", organisaatioList.get(1).getNimet().get(0).getNimi().getString("sv"));
        Assert.assertEquals("root test koulutustoimija", organisaatioList.get(1).getNimet().get(1).getNimi().getString("fi"));
        Assert.assertEquals("Ygankuja 1", organisaatioList.get(1).getPostiosoite().getOsoite());
        Assert.assertEquals("oppilaitoksenopetuskieli_1#1", organisaatioList.get(1).getKielet().get(0));
        Assert.assertNotEquals(organisaatioList.get(1).getNimet().get(0).getNimi(), organisaatioList.get(1).getNimet().get(1).getNimi());

        // Case: Has fi name, gets new sv name from YTJ
        Assert.assertEquals(1, organisaatioList.get(2).getNimet().size());
        Assert.assertEquals("Ruotsalainen koulutustoimija", organisaatioList.get(2).getNimet().get(0).getNimi().getString("sv"));
        Assert.assertEquals("root2 test2 koulutustoimija2", organisaatioList.get(2).getNimet().get(0).getNimi().getString("fi"));
        Assert.assertEquals("Svenska gatan 1", organisaatioList.get(2).getPostiosoite().getOsoite());
        Assert.assertEquals("oppilaitoksenopetuskieli_2#1", organisaatioList.get(2).getKielet().get(0));
        Assert.assertEquals(2, organisaatioList.get(2).getNimet().get(0).getNimi().getValues().size());
    }

}
