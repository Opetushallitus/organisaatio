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
import fi.vm.sade.organisaatio.business.OrganisaatioFindBusinessService;
import fi.vm.sade.organisaatio.business.exception.OrganisaatioNameHistoryNotValidException;
import fi.vm.sade.organisaatio.dao.OrganisaatioDAO;
import fi.vm.sade.organisaatio.dto.mapping.SearchCriteriaModelMapper;
import fi.vm.sade.organisaatio.model.*;
import fi.vm.sade.organisaatio.resource.IndexerResource;
import fi.vm.sade.organisaatio.resource.dto.OrganisaatioRDTO;
import fi.vm.sade.organisaatio.service.converter.OrganisaatioToOrganisaatioRDTOConverter;
import fi.vm.sade.organisaatio.util.OrganisaatioRDTOTestUtil;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneOffset;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.data.MapEntry.entry;

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
    @Autowired
    private OrganisaatioFindBusinessService organisaatioFindBusinessService;

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

    @Test
    public void updateOppilaitosShouldUpdateToimipisteNames() {
        OrganisaatioRDTO koulutustoimija = OrganisaatioRDTOTestUtil.createOrganisaatio("koulutustoimija1", OrganisaatioTyyppi.KOULUTUSTOIMIJA.value(), rootOid);
        OrganisaatioResult koulutustoimijaResult1 = service.save(koulutustoimija, false);
        String koulutustoimijaOid = koulutustoimijaResult1.getOrganisaatio().getOid();
        OrganisaatioRDTO oppilaitos = OrganisaatioRDTOTestUtil.createOrganisaatio("oppilaitos1 (fi)", OrganisaatioTyyppi.OPPILAITOS.value(), koulutustoimijaOid);
        OrganisaatioResult oppilaitosResult1 = service.save(oppilaitos, false);
        String oppilaitos1oid = oppilaitosResult1.getOrganisaatio().getOid();
        OrganisaatioRDTO toimipiste = OrganisaatioRDTOTestUtil.createOrganisaatio("oppilaitos1 (fi), toimipiste1 (fi)", OrganisaatioTyyppi.TOIMIPISTE.value(), oppilaitos1oid);
        toimipiste.getNimi().put("en", "oppilaitos1-päivitetty (en), toimipiste1 (en)");
        toimipiste.getNimi().put("sv", "toimipiste1 (sv)");
        OrganisaatioResult toimipisteResult1 = service.save(toimipiste, false);

        oppilaitos.getNimi().put("fi", "oppilaitos1-päivitetty (fi)");
        oppilaitos.getNimi().put("en", "oppilaitos1-päivitetty (en)");
        oppilaitos.getNimi().put("dk", "oppilaitos1-päivitetty (dk)");
        service.save(oppilaitos, true);

        Organisaatio organisaatio = organisaatioFindBusinessService.findById(toimipisteResult1.getOrganisaatio().getOid());
        Map<String, String> nimet = organisaatio.getNimi().getValues();
        assertThat(nimet).containsOnly(
                entry("fi", "oppilaitos1-päivitetty (fi), toimipiste1 (fi)"),
                entry("en", "oppilaitos1-päivitetty (en), toimipiste1 (en)"),
                entry("sv", "toimipiste1 (sv)")
        );
    }

    @Test
    public void saveToimipisteShouldGenerateOpetuspisteenJarjNroAndToimipistekoodiWhenEmpty() {
        OrganisaatioRDTO koulutustoimija1 = OrganisaatioRDTOTestUtil.createOrganisaatio("koulutustoimija1", OrganisaatioTyyppi.KOULUTUSTOIMIJA.value(), rootOid);
        OrganisaatioResult koulutustoimijaResult1 = service.save(koulutustoimija1, false);
        String koulutustoimija1Oid = koulutustoimijaResult1.getOrganisaatio().getOid();
        OrganisaatioRDTO oppilaitos1 = OrganisaatioRDTOTestUtil.createOrganisaatio("oppilaitos1", OrganisaatioTyyppi.OPPILAITOS.value(), koulutustoimija1Oid);
        oppilaitos1.setOppilaitosKoodi("oppilaitoskoodi");
        OrganisaatioResult oppilaitosResult1 = service.save(oppilaitos1, false);
        String oppilaitos1oid = oppilaitosResult1.getOrganisaatio().getOid();

        OrganisaatioRDTO toimipiste1 = OrganisaatioRDTOTestUtil.createOrganisaatio("oppilaitos1, toimipiste1", OrganisaatioTyyppi.TOIMIPISTE.value(), oppilaitos1oid);
        OrganisaatioResult toimipisteResult1 = service.save(toimipiste1, false);
        assertThat(toimipisteResult1.getOrganisaatio().getOpetuspisteenJarjNro()).isEqualTo("01");
        assertThat(toimipisteResult1.getOrganisaatio().getToimipisteKoodi()).isEqualTo("oppilaitoskoodi01");

        jdbcTemplate.update("update organisaatio set opetuspisteenjarjnro = null, toimipistekoodi = null where oid = ?", toimipisteResult1.getOrganisaatio().getOid());
        toimipisteResult1 = service.save(toimipiste1, true);
        assertThat(toimipisteResult1.getOrganisaatio().getOpetuspisteenJarjNro()).isEqualTo("02");
        assertThat(toimipisteResult1.getOrganisaatio().getToimipisteKoodi()).isEqualTo("oppilaitoskoodi02");
    }

    @Test
    public void saveToimipisteShouldMaintainToimipistekoodiWhenOpetuspisteenJarjNroEmpty() {
        OrganisaatioRDTO koulutustoimija1 = OrganisaatioRDTOTestUtil.createOrganisaatio("koulutustoimija1", OrganisaatioTyyppi.KOULUTUSTOIMIJA.value(), rootOid);
        OrganisaatioResult koulutustoimijaResult1 = service.save(koulutustoimija1, false);
        String koulutustoimija1Oid = koulutustoimijaResult1.getOrganisaatio().getOid();
        OrganisaatioRDTO oppilaitos1 = OrganisaatioRDTOTestUtil.createOrganisaatio("oppilaitos1", OrganisaatioTyyppi.OPPILAITOS.value(), koulutustoimija1Oid);
        oppilaitos1.setOppilaitosKoodi("oppilaitoskoodi");
        OrganisaatioResult oppilaitosResult1 = service.save(oppilaitos1, false);
        String oppilaitos1oid = oppilaitosResult1.getOrganisaatio().getOid();

        OrganisaatioRDTO toimipiste1 = OrganisaatioRDTOTestUtil.createOrganisaatio("oppilaitos1, toimipiste1", OrganisaatioTyyppi.TOIMIPISTE.value(), oppilaitos1oid);
        OrganisaatioResult toimipisteResult1 = service.save(toimipiste1, false);
        assertThat(toimipisteResult1.getOrganisaatio().getOpetuspisteenJarjNro()).isEqualTo("01");
        assertThat(toimipisteResult1.getOrganisaatio().getToimipisteKoodi()).isEqualTo("oppilaitoskoodi01");

        jdbcTemplate.update("update organisaatio set opetuspisteenjarjnro = null where oid = ?", toimipisteResult1.getOrganisaatio().getOid());
        toimipisteResult1 = service.save(toimipiste1, true);
        assertThat(toimipisteResult1.getOrganisaatio().getOpetuspisteenJarjNro()).isNull();
        assertThat(toimipisteResult1.getOrganisaatio().getToimipisteKoodi()).isEqualTo("oppilaitoskoodi01");
    }

    @Test
    public void saveToimipisteShouldGenerateToimipistekoodiWhenEmpty() {
        OrganisaatioRDTO koulutustoimija1 = OrganisaatioRDTOTestUtil.createOrganisaatio("koulutustoimija1", OrganisaatioTyyppi.KOULUTUSTOIMIJA.value(), rootOid);
        OrganisaatioResult koulutustoimijaResult1 = service.save(koulutustoimija1, false);
        String koulutustoimija1Oid = koulutustoimijaResult1.getOrganisaatio().getOid();
        OrganisaatioRDTO oppilaitos1 = OrganisaatioRDTOTestUtil.createOrganisaatio("oppilaitos1", OrganisaatioTyyppi.OPPILAITOS.value(), koulutustoimija1Oid);
        oppilaitos1.setOppilaitosKoodi("oppilaitoskoodi");
        OrganisaatioResult oppilaitosResult1 = service.save(oppilaitos1, false);
        String oppilaitos1oid = oppilaitosResult1.getOrganisaatio().getOid();

        OrganisaatioRDTO toimipiste1 = OrganisaatioRDTOTestUtil.createOrganisaatio("oppilaitos1, toimipiste1", OrganisaatioTyyppi.TOIMIPISTE.value(), oppilaitos1oid);
        OrganisaatioResult toimipisteResult1 = service.save(toimipiste1, false);
        assertThat(toimipisteResult1.getOrganisaatio().getOpetuspisteenJarjNro()).isEqualTo("01");
        assertThat(toimipisteResult1.getOrganisaatio().getToimipisteKoodi()).isEqualTo("oppilaitoskoodi01");

        jdbcTemplate.update("update organisaatio set toimipistekoodi = null where oid = ?", toimipisteResult1.getOrganisaatio().getOid());
        toimipisteResult1 = service.save(toimipiste1, true);
        assertThat(toimipisteResult1.getOrganisaatio().getOpetuspisteenJarjNro()).isEqualTo("01");
        assertThat(toimipisteResult1.getOrganisaatio().getToimipisteKoodi()).isEqualTo("oppilaitoskoodi01");
    }

    @Test
    public void saveOrganisaatioShouldValidateNimetAlkuPvm() {
        LocalDate localDate = LocalDate.of(2016, Month.FEBRUARY, 15);
        Date date1 = Date.from(localDate.atTime(9, 39).toInstant(ZoneOffset.UTC));
        Date date2 = Date.from(localDate.atTime(9, 40).toInstant(ZoneOffset.UTC));
        OrganisaatioRDTO koulutustoimija = OrganisaatioRDTOTestUtil.createOrganisaatio("koulutustoimija1", OrganisaatioTyyppi.KOULUTUSTOIMIJA.value(), rootOid);
        koulutustoimija.getNimet().add(OrganisaatioRDTOTestUtil.createNimi("koulutustoimija11", date1));
        koulutustoimija.getNimet().add(OrganisaatioRDTOTestUtil.createNimi("koulutustoimija12", date2));

        Throwable throwable = catchThrowable(() -> service.save(koulutustoimija, false));

        assertThat(throwable).isExactlyInstanceOf(OrganisaatioNameHistoryNotValidException.class);
    }

}
