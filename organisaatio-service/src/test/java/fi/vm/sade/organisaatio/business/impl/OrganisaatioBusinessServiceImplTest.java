package fi.vm.sade.organisaatio.business.impl;

import fi.vm.sade.oid.OIDService;
import fi.vm.sade.organisaatio.SecurityAwareTestBase;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.business.OrganisaatioBusinessService;
import fi.vm.sade.organisaatio.business.OrganisaatioFindBusinessService;
import fi.vm.sade.organisaatio.business.exception.OrganisaatioNameHistoryNotValidException;
import fi.vm.sade.organisaatio.dto.VarhaiskasvatuksenKielipainotusDto;
import fi.vm.sade.organisaatio.dto.VarhaiskasvatuksenToiminnallinepainotusDto;
import fi.vm.sade.organisaatio.dto.VarhaiskasvatuksenToimipaikkaTiedotDto;
import fi.vm.sade.organisaatio.dto.v4.OrganisaatioRDTOV4;
import fi.vm.sade.organisaatio.dto.v4.ResultRDTOV4;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.model.OrganisaatioResult;
import fi.vm.sade.organisaatio.model.OrganisaatioSuhde;
import fi.vm.sade.organisaatio.repository.OrganisaatioRepository;
import fi.vm.sade.organisaatio.resource.OIDServiceMock;
import fi.vm.sade.organisaatio.resource.dto.OrganisaatioNimiRDTO;
import fi.vm.sade.organisaatio.resource.dto.OrganisaatioRDTO;
import fi.vm.sade.organisaatio.service.converter.OrganisaatioToOrganisaatioRDTOConverter;
import fi.vm.sade.organisaatio.util.OrganisaatioRDTOTestUtil;
import fi.vm.sade.organisaatio.ytj.api.YTJService;
import fi.vm.sade.security.OidProvider;
import fi.vm.sade.security.OrganisationHierarchyAuthorizer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.jdbc.Sql;

import jakarta.validation.ValidationException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link OrganisaatioBusinessServiceImpl} class.
 * Created by vrouvine on 02/12/14.
 */
@SpringBootTest
@Sql("/data/truncate_tables.sql")
@Sql("/data/basic_organisaatio_data.sql")
public class OrganisaatioBusinessServiceImplTest extends SecurityAwareTestBase {

    @TestConfiguration
    static class OrganisaatioBusinessServiceImplTestContextConfiguration {

        @Bean
        @Primary
        public OrganisationHierarchyAuthorizer authorizer() {
            return new OrganisationHierarchyAuthorizer();
        }

        @Bean
        @Primary
        public OidProvider oidProvider() {
            return new OidProvider();
        }

        @Bean
        @Primary
        public YTJService ytjService() {
            return mock(YTJService.class);
        }

        @Bean
        @Primary
        public OIDService oidService() {
            return new OIDServiceMock();
        }

        @Bean
        @Primary
        public OrganisaatioTarjonta organisaatioTarjonta() {
            OrganisaatioTarjonta mocked = mock(OrganisaatioTarjonta.class);
            when(mocked.alkaviaKoulutuksia(any())).thenReturn(false);
            return mocked;
        }
    }

    private final String rootOid = "1.2.246.562.24.00000000001";
    @Autowired
    private OrganisaatioRepository organisaatioRepository;
    @Autowired
    private OrganisaatioBusinessService service;
    @Autowired
    private OrganisaatioFindBusinessService organisaatioFindBusinessService;
    @Autowired
    private OrganisaatioBusinessServiceImpl organisaatioBusinessServiceImpl;
    @Autowired
    private OrganisaatioToOrganisaatioRDTOConverter organisaatioToOrganisaatioRDTOConverter;

    @Test
    public void processOrganisaatioSuhdeChangesNoNewChanges() {
        Set<Organisaatio> results = service.processNewOrganisaatioSuhdeChanges();
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    public void processOrganisaatiosuhdeChangesOneNewChange() throws Exception {
        long parentId = 7L;
        long childId = 4L;
        String newParentOid = "1.2.2004.5";

        int rowCount = countRowsInTable("organisaatiosuhde");
        // Make new organisaatiosuhde change
        Date time = new Date();
        jdbcTemplate.update("insert into organisaatiosuhde (id, version, suhdetyyppi, child_id, parent_id, alkupvm) values (-1, 1, 'HISTORIA', ?, ?, ?)",
                new Object[]{childId, parentId, time});
        // End old organisaatiosuhde
        jdbcTemplate.update("update organisaatiosuhde set loppupvm = ? where id = ?", new Object[]{time, 3});

        assertEquals(rowCount + 1, countRowsInTable("organisaatiosuhde"));

        Set<Organisaatio> results = service.processNewOrganisaatioSuhdeChanges();
        assertNotNull(results);
        assertEquals(1, results.size());

        Organisaatio modified = results.iterator().next();
        assertEquals(Long.valueOf(4), modified.getId());

        assertEquals("|" + rootOid + "|" + newParentOid + "|", modified.getParentOidPath());

        String oid = "1.2.2004.4";
        Organisaatio org = organisaatioRepository.findFirstByOid(oid);
        assertEquals(modified.getParentOidPath() + modified.getOid() + "|", org.getParentOidPath());

        oid = "1.2.2005.4";
        Organisaatio org2 = organisaatioRepository.findFirstByOid(oid);
        assertEquals(modified.getParentOidPath() + modified.getOid() + "|", org2.getParentOidPath());

        oid = "1.2.2005.5";
        org2 = organisaatioRepository.findFirstByOid(oid);
        assertEquals(org.getParentOidPath() + org.getOid() + "|", org2.getParentOidPath());

        checkParent(modified, "1.2.2004.4");
        checkParent(modified, "1.2.2005.4");
        checkParent(org, "1.2.2005.5");
        checkParentOidPath(org, "1.2.2005.5");
    }

    @Test
    public void processNewOrganisaatioSuhdeChangesWithHistoria() {
        Organisaatio koulutustoimija1 = createOrganisaatio("koulutustoimija1", OrganisaatioTyyppi.KOULUTUSTOIMIJA, "koulutustoimija1", rootOid);
        assertThat(koulutustoimija1).returns("|" + rootOid + "|", Organisaatio::getParentOidPath);
        Organisaatio koulutustoimija2 = createOrganisaatio("koulutustoimija2", OrganisaatioTyyppi.KOULUTUSTOIMIJA, "koulutustoimija2", rootOid);
        assertThat(koulutustoimija2).returns("|" + rootOid + "|", Organisaatio::getParentOidPath);

        Organisaatio oppilaitos = createOrganisaatio("oppilaitos", OrganisaatioTyyppi.OPPILAITOS, "oppilaitos", koulutustoimija1.getOid());
        assertThat(oppilaitos)
                .returns(koulutustoimija1.getParentOidPath() + koulutustoimija1.getOid() + "|", Organisaatio::getParentOidPath)
                .returns(koulutustoimija1.getParentIdPath() + koulutustoimija1.getId() + "|", Organisaatio::getParentIdPath);
        Organisaatio toimipiste = createOrganisaatio("toimipiste", OrganisaatioTyyppi.TOIMIPISTE, "toimipiste", oppilaitos.getOid());
        assertThat(toimipiste)
                .returns(oppilaitos.getParentOidPath() + oppilaitos.getOid() + "|", Organisaatio::getParentOidPath)
                .returns(oppilaitos.getParentIdPath() + oppilaitos.getId() + "|", Organisaatio::getParentIdPath);

        service.changeOrganisaatioParent(oppilaitos, koulutustoimija2, new Date());

        Set<Organisaatio> muokatut = service.processNewOrganisaatioSuhdeChanges();
        assertThat(muokatut).extracting(Organisaatio::getOid).contains(oppilaitos.getOid());

        assertThat(organisaatioRepository.findFirstByOid(oppilaitos.getOid()))
                .returns(koulutustoimija2.getParentOidPath() + koulutustoimija2.getOid() + "|", Organisaatio::getParentOidPath)
                .returns(koulutustoimija2.getParentIdPath() + koulutustoimija2.getId() + "|", Organisaatio::getParentIdPath);
        assertThat(organisaatioRepository.findFirstByOid(toimipiste.getOid()))
                .returns(oppilaitos.getParentOidPath() + oppilaitos.getOid() + "|", Organisaatio::getParentOidPath)
                .returns(oppilaitos.getParentIdPath() + oppilaitos.getId() + "|", Organisaatio::getParentIdPath);
    }

    private ZonedDateTime toDay(Date d) {
        return d.toInstant().atZone(ZoneId.systemDefault()).truncatedTo(ChronoUnit.DAYS);
    }

    @Test
    public void settingOppilaitosLakkautusPvmAlsoSetsChildToimipisteLakkautusPvm() {
        Organisaatio oppilaitos = createOrganisaatio("oppilaitosLakkautusPvm", OrganisaatioTyyppi.OPPILAITOS, "oppilaitosLakkautusPvm", rootOid);
        Organisaatio toimipiste1 = createOrganisaatio("toimipiste1LakkautusPvm", OrganisaatioTyyppi.TOIMIPISTE, "toimipiste1LakkautusPvm", oppilaitos.getOid());
        Organisaatio toimipiste2 = createOrganisaatio("toimipiste2LakkautusPvm", OrganisaatioTyyppi.TOIMIPISTE, "toimipiste2LakkautusPvm", oppilaitos.getOid());

        Date lakkautusPvm = new Date();
        OrganisaatioRDTO update = organisaatioToOrganisaatioRDTOConverter.convert(oppilaitos);
        update.setYhteystiedot(Set.of());
        update.setLakkautusPvm(lakkautusPvm);
        service.saveOrUpdate(update);

        Organisaatio toimipiste1Updated = organisaatioRepository.findFirstByOid(toimipiste1.getOid());
        assertEquals(toDay(lakkautusPvm), toDay(toimipiste1Updated.getLakkautusPvm()));
        Organisaatio toimipiste2Updated = organisaatioRepository.findFirstByOid(toimipiste2.getOid());
        assertEquals(toDay(lakkautusPvm), toDay(toimipiste2Updated.getLakkautusPvm()));
    }

    @Test
    public void settingOppilaitosLakkautusPvmDoesNotChangeChildToimipisteLakkautusPvm() {
        Organisaatio oppilaitos = createOrganisaatio("oppilaitosLakkautusPvm2", OrganisaatioTyyppi.OPPILAITOS, "oppilaitosLakkautusPvm2", rootOid);
        Organisaatio toimipiste1 = createOrganisaatio("toimipiste1LakkautusPvm2", OrganisaatioTyyppi.TOIMIPISTE, "toimipiste1LakkautusPvm2", oppilaitos.getOid());

        Date toimipisteLakkautusPvm = new Date();
        OrganisaatioRDTO toimipisteUpdate = organisaatioToOrganisaatioRDTOConverter.convert(oppilaitos);
        toimipisteUpdate.setYhteystiedot(Set.of());
        toimipisteUpdate.setLakkautusPvm(toimipisteLakkautusPvm);
        service.saveOrUpdate(toimipisteUpdate);

        Date oppilaitosLakkautusPvm = Date.from(Instant.now().minusSeconds(100000));
        OrganisaatioRDTO oppilaitosUpdate = organisaatioToOrganisaatioRDTOConverter.convert(oppilaitos);
        oppilaitosUpdate.setYhteystiedot(Set.of());
        oppilaitosUpdate.setLakkautusPvm(oppilaitosLakkautusPvm);
        service.saveOrUpdate(oppilaitosUpdate);

        Organisaatio toimipiste1Updated = organisaatioRepository.findFirstByOid(toimipiste1.getOid());
        assertNotEquals(toDay(toimipisteLakkautusPvm), toDay(toimipiste1Updated.getLakkautusPvm()));
    }

    @Test
    public void processNewOrganisaatioSuhdeChangesWithLiitos() {
        Organisaatio koulutustoimija = createOrganisaatio("koulutustoimija", OrganisaatioTyyppi.KOULUTUSTOIMIJA, "koulutustoimija", rootOid);
        assertThat(koulutustoimija).returns("|" + rootOid + "|", Organisaatio::getParentOidPath);

        Organisaatio oppilaitos1 = createOrganisaatio("oppilaitos1", OrganisaatioTyyppi.OPPILAITOS, "oppilaitos1", koulutustoimija.getOid());
        assertThat(oppilaitos1)
                .returns(koulutustoimija.getParentOidPath() + koulutustoimija.getOid() + "|", Organisaatio::getParentOidPath)
                .returns(koulutustoimija.getParentIdPath() + koulutustoimija.getId() + "|", Organisaatio::getParentIdPath);
        Organisaatio toimipiste1 = createOrganisaatio("toimipiste1", OrganisaatioTyyppi.TOIMIPISTE, "toimipiste1", oppilaitos1.getOid());
        assertThat(toimipiste1)
                .returns(oppilaitos1.getParentOidPath() + oppilaitos1.getOid() + "|", Organisaatio::getParentOidPath)
                .returns(oppilaitos1.getParentIdPath() + oppilaitos1.getId() + "|", Organisaatio::getParentIdPath);

        Organisaatio oppilaitos2 = createOrganisaatio("oppilaitos2", OrganisaatioTyyppi.OPPILAITOS, "oppilaitos2", koulutustoimija.getOid());
        assertThat(oppilaitos2)
                .returns(koulutustoimija.getParentOidPath() + koulutustoimija.getOid() + "|", Organisaatio::getParentOidPath)
                .returns(koulutustoimija.getParentIdPath() + koulutustoimija.getId() + "|", Organisaatio::getParentIdPath);
        Organisaatio toimipiste2 = createOrganisaatio("toimipiste2", OrganisaatioTyyppi.TOIMIPISTE, "toimipiste2", oppilaitos2.getOid());
        assertThat(toimipiste2)
                .returns(oppilaitos2.getParentOidPath() + oppilaitos2.getOid() + "|", Organisaatio::getParentOidPath)
                .returns(oppilaitos2.getParentIdPath() + oppilaitos2.getId() + "|", Organisaatio::getParentIdPath);

        service.mergeOrganisaatio(oppilaitos1, oppilaitos2, new Date());
        Set<Organisaatio> muokatut = service.processNewOrganisaatioSuhdeChanges();
        assertThat(muokatut).extracting(Organisaatio::getOid).contains(oppilaitos1.getOid());

        assertThat(organisaatioRepository.findFirstByOid(oppilaitos1.getOid()))
                .returns(koulutustoimija.getParentOidPath() + koulutustoimija.getOid() + "|", Organisaatio::getParentOidPath)
                .returns(koulutustoimija.getParentIdPath() + koulutustoimija.getId() + "|", Organisaatio::getParentIdPath);
        assertThat(organisaatioRepository.findFirstByOid(toimipiste1.getOid()))
                .returns(oppilaitos2.getParentOidPath() + oppilaitos2.getOid() + "|", Organisaatio::getParentOidPath)
                .returns(oppilaitos2.getParentIdPath() + oppilaitos2.getId() + "|", Organisaatio::getParentIdPath);
    }

    @Test
    public void processNewOrganisaatioSuhdeChangesWithLiitosToAnotherTree() {
        Organisaatio koulutustoimija1 = createOrganisaatio("koulutustoimija1", OrganisaatioTyyppi.KOULUTUSTOIMIJA, "koulutustoimija1", rootOid);
        assertThat(koulutustoimija1).returns("|" + rootOid + "|", Organisaatio::getParentOidPath);
        Organisaatio oppilaitos1 = createOrganisaatio("oppilaitos1", OrganisaatioTyyppi.OPPILAITOS, "oppilaitos1", koulutustoimija1.getOid());
        assertThat(oppilaitos1)
                .returns(koulutustoimija1.getParentOidPath() + koulutustoimija1.getOid() + "|", Organisaatio::getParentOidPath)
                .returns(koulutustoimija1.getParentIdPath() + koulutustoimija1.getId() + "|", Organisaatio::getParentIdPath);
        Organisaatio toimipiste1 = createOrganisaatio("toimipiste1", OrganisaatioTyyppi.TOIMIPISTE, "toimipiste1", oppilaitos1.getOid());
        assertThat(toimipiste1)
                .returns(oppilaitos1.getParentOidPath() + oppilaitos1.getOid() + "|", Organisaatio::getParentOidPath)
                .returns(oppilaitos1.getParentIdPath() + oppilaitos1.getId() + "|", Organisaatio::getParentIdPath);

        Organisaatio koulutustoimija2 = createOrganisaatio("koulutustoimija2", OrganisaatioTyyppi.KOULUTUSTOIMIJA, "koulutustoimija2", rootOid);
        Organisaatio oppilaitos2 = createOrganisaatio("oppilaitos2", OrganisaatioTyyppi.OPPILAITOS, "oppilaitos2", koulutustoimija2.getOid());
        assertThat(oppilaitos2)
                .returns(koulutustoimija2.getParentOidPath() + koulutustoimija2.getOid() + "|", Organisaatio::getParentOidPath)
                .returns(koulutustoimija2.getParentIdPath() + koulutustoimija2.getId() + "|", Organisaatio::getParentIdPath);
        Organisaatio toimipiste2 = createOrganisaatio("toimipiste2", OrganisaatioTyyppi.TOIMIPISTE, "toimipiste2", oppilaitos2.getOid());
        assertThat(toimipiste2)
                .returns(oppilaitos2.getParentOidPath() + oppilaitos2.getOid() + "|", Organisaatio::getParentOidPath)
                .returns(oppilaitos2.getParentIdPath() + oppilaitos2.getId() + "|", Organisaatio::getParentIdPath);

        service.mergeOrganisaatio(oppilaitos1, oppilaitos2, new Date());

        Set<Organisaatio> muokatut = service.processNewOrganisaatioSuhdeChanges();
        assertThat(muokatut).extracting(Organisaatio::getOid).contains(oppilaitos1.getOid());

        assertThat(organisaatioRepository.findFirstByOid(oppilaitos1.getOid()))
                .returns(koulutustoimija1.getParentOidPath() + koulutustoimija1.getOid() + "|", Organisaatio::getParentOidPath)
                .returns(koulutustoimija1.getParentIdPath() + koulutustoimija1.getId() + "|", Organisaatio::getParentIdPath);
        assertThat(organisaatioRepository.findFirstByOid(toimipiste1.getOid()))
                .returns(oppilaitos2.getParentOidPath() + oppilaitos2.getOid() + "|", Organisaatio::getParentOidPath)
                .returns(oppilaitos2.getParentIdPath() + oppilaitos2.getId() + "|", Organisaatio::getParentIdPath);
    }

    private void setParentSuhteetStartDatesInThePast(Organisaatio organisaatio) {
        try {
            Date farPast = new SimpleDateFormat("yyyy-MM-dd").parse("1970-01-01");
            for (OrganisaatioSuhde os : organisaatio.getParentSuhteet()) {
                os.setAlkuPvm(farPast);
            }
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private void checkParentOidPath(Organisaatio parent, String oid) {
        Organisaatio org = organisaatioRepository.findFirstByOid(oid);
        assertEquals(parent.getParentOidPath() + parent.getOid() + "|", org.getParentOidPath());
    }

    private void checkParent(Organisaatio parent, String oid) {
        Organisaatio org = organisaatioRepository.findFirstByOid(oid);
        String pop = org.getParentOidPath();
        String[] list = pop.split("[|]+");
        String parentOid = list[list.length - 1];
        Organisaatio orgParent = organisaatioRepository.findFirstByOid(parentOid);
        assertEquals(parent.getOid(), orgParent.getOid());
    }

    @Test
    public void editingRemovedIsNotAllowed() {
        OrganisaatioRDTO model = new OrganisaatioRDTO();
        String removedOid = "1.2.2004.4";
        model.setOid(removedOid);
        jdbcTemplate.update("update organisaatio set organisaatiopoistettu = TRUE where oid = ?", removedOid);
        try {
            service.saveOrUpdate(model);
            fail();
        } catch (Throwable e) {
            assertNotNull(e.getMessage());
            assertEquals(e.getMessage(), "validation.Organisaatio.poistettu");
        }
    }

    @Test
    public void saveNewAndUpdateOrganisation() {
        OrganisaatioRDTO model = OrganisaatioRDTOTestUtil.createOrganisaatio("orgrandomnimijsdflsfsf", OrganisaatioTyyppi.MUU_ORGANISAATIO.value(), "65432.1", rootOid, true);
        model.setKayntiosoite(null);
        model.setYhteystiedot(null);
        model.setKieletUris(null);
        model.setAlkuPvm(new Date());
        OrganisaatioResult organisaatioResult = service.saveOrUpdate(model);
        assertNotNull(organisaatioResult.getOrganisaatio().getId());

        model = organisaatioToOrganisaatioRDTOConverter.convert(organisaatioResult.getOrganisaatio());
        organisaatioRepository.getJpaEntityManager().detach(organisaatioResult.getOrganisaatio());
        model.setYTunnus("4567891-0");
        organisaatioResult = service.saveOrUpdate(model);
        assertEquals("4567891-0", organisaatioResult.getOrganisaatio().getYtunnus());

    }

       @Test
    public void saveToimipisteShouldGenerateOpetuspisteenJarjNroAndToimipistekoodiWhenEmpty() {
        OrganisaatioRDTO koulutustoimija1 = OrganisaatioRDTOTestUtil.createOrganisaatio("koulutustoimija1", OrganisaatioTyyppi.KOULUTUSTOIMIJA.value(), rootOid, true);
        OrganisaatioResult koulutustoimijaResult1 = service.saveOrUpdate(koulutustoimija1);
        String koulutustoimija1Oid = koulutustoimijaResult1.getOrganisaatio().getOid();
        OrganisaatioRDTO oppilaitos1 = OrganisaatioRDTOTestUtil.createOrganisaatio("oppilaitos1", OrganisaatioTyyppi.OPPILAITOS.value(), koulutustoimija1Oid, true);
        oppilaitos1.setOppilaitosKoodi("oppilaitoskoodi");
        OrganisaatioResult oppilaitosResult1 = service.saveOrUpdate(oppilaitos1);
        String oppilaitos1oid = oppilaitosResult1.getOrganisaatio().getOid();

        OrganisaatioRDTO toimipiste1 = OrganisaatioRDTOTestUtil.createOrganisaatio("oppilaitos1, toimipiste1", OrganisaatioTyyppi.TOIMIPISTE.value(), oppilaitos1oid, true);
        OrganisaatioResult toimipisteResult1 = service.saveOrUpdate(toimipiste1);
        assertThat(toimipisteResult1.getOrganisaatio().getOpetuspisteenJarjNro()).isEqualTo("01");
        assertThat(toimipisteResult1.getOrganisaatio().getToimipisteKoodi()).isEqualTo("oppilaitoskoodi01");

        OrganisaatioRDTO toimipiste2 = OrganisaatioRDTOTestUtil.createOrganisaatio("oppilaitos2, toimipiste2", OrganisaatioTyyppi.TOIMIPISTE.value(), oppilaitos1oid, true);
        OrganisaatioResult toimipisteResult2 = service.saveOrUpdate(toimipiste2);

        toimipisteResult2.getOrganisaatio().setOpetuspisteenJarjNro(null);
        toimipisteResult2.getOrganisaatio().setToimipisteKoodi(null);
        organisaatioRepository.saveAndFlush(toimipisteResult2.getOrganisaatio());
        //jdbcTemplate.update("update organisaatio set opetuspisteenjarjnro = null, toimipistekoodi = null where oid = ?", toimipisteResult1.getOrganisaatio().getOid());
        toimipisteResult2 = service.saveOrUpdate(toimipiste2);
        assertThat(toimipisteResult2.getOrganisaatio().getOpetuspisteenJarjNro()).isEqualTo("02");
        assertThat(toimipisteResult2.getOrganisaatio().getToimipisteKoodi()).isEqualTo("oppilaitoskoodi02");
    }

    @Test
    public void saveToimipisteShouldMaintainToimipistekoodiWhenOpetuspisteenJarjNroEmpty() {
        OrganisaatioRDTO koulutustoimija1 = OrganisaatioRDTOTestUtil.createOrganisaatio("koulutustoimija1", OrganisaatioTyyppi.KOULUTUSTOIMIJA.value(), rootOid, true);
        OrganisaatioResult koulutustoimijaResult1 = service.saveOrUpdate(koulutustoimija1);
        String koulutustoimija1Oid = koulutustoimijaResult1.getOrganisaatio().getOid();
        OrganisaatioRDTO oppilaitos1 = OrganisaatioRDTOTestUtil.createOrganisaatio("oppilaitos1", OrganisaatioTyyppi.OPPILAITOS.value(), koulutustoimija1Oid, true);
        oppilaitos1.setOppilaitosKoodi("oppilaitoskoodi");
        OrganisaatioResult oppilaitosResult1 = service.saveOrUpdate(oppilaitos1);
        String oppilaitos1oid = oppilaitosResult1.getOrganisaatio().getOid();

        OrganisaatioRDTO toimipiste1 = OrganisaatioRDTOTestUtil.createOrganisaatio("oppilaitos1, toimipiste1", OrganisaatioTyyppi.TOIMIPISTE.value(), oppilaitos1oid, true);
        OrganisaatioResult toimipisteResult1 = service.saveOrUpdate(toimipiste1);
        assertThat(toimipisteResult1.getOrganisaatio().getOpetuspisteenJarjNro()).isEqualTo("01");
        assertThat(toimipisteResult1.getOrganisaatio().getToimipisteKoodi()).isEqualTo("oppilaitoskoodi01");

        toimipisteResult1.getOrganisaatio().setOpetuspisteenJarjNro(null);
        Organisaatio org = organisaatioRepository.saveAndFlush(toimipisteResult1.getOrganisaatio());
        toimipiste1.setOid(org.getOid());
        toimipisteResult1 = service.saveOrUpdate(toimipiste1);
        assertThat(toimipisteResult1.getOrganisaatio().getOpetuspisteenJarjNro()).isNull();
        assertThat(toimipisteResult1.getOrganisaatio().getToimipisteKoodi()).isEqualTo("oppilaitoskoodi01");
    }

    @Test
    public void saveToimipisteShouldGenerateToimipistekoodiWhenEmpty() {
        OrganisaatioRDTO koulutustoimija1 = OrganisaatioRDTOTestUtil.createOrganisaatio("koulutustoimija1", OrganisaatioTyyppi.KOULUTUSTOIMIJA.value(), rootOid, true);
        OrganisaatioResult koulutustoimijaResult1 = service.saveOrUpdate(koulutustoimija1);
        String koulutustoimija1Oid = koulutustoimijaResult1.getOrganisaatio().getOid();
        OrganisaatioRDTO oppilaitos1 = OrganisaatioRDTOTestUtil.createOrganisaatio("oppilaitos1", OrganisaatioTyyppi.OPPILAITOS.value(), koulutustoimija1Oid, true);
        oppilaitos1.setOppilaitosKoodi("oppilaitoskoodi");
        OrganisaatioResult oppilaitosResult1 = service.saveOrUpdate(oppilaitos1);
        String oppilaitos1oid = oppilaitosResult1.getOrganisaatio().getOid();

        OrganisaatioRDTO toimipiste1 = OrganisaatioRDTOTestUtil.createOrganisaatio("oppilaitos1, toimipiste1", OrganisaatioTyyppi.TOIMIPISTE.value(), oppilaitos1oid, true);
        OrganisaatioResult toimipisteResult1 = service.saveOrUpdate(toimipiste1);
        assertThat(toimipisteResult1.getOrganisaatio().getOpetuspisteenJarjNro()).isEqualTo("01");
        assertThat(toimipisteResult1.getOrganisaatio().getToimipisteKoodi()).isEqualTo("oppilaitoskoodi01");


        jdbcTemplate.update("update organisaatio set toimipistekoodi = null where oid = ?", toimipisteResult1.getOrganisaatio().getOid());
        toimipisteResult1 = service.saveOrUpdate(toimipiste1);
        assertThat(toimipisteResult1.getOrganisaatio().getOpetuspisteenJarjNro()).isEqualTo("01");
        assertThat(toimipisteResult1.getOrganisaatio().getToimipisteKoodi()).isEqualTo("oppilaitoskoodi01");
    }

    @Test
    public void saveOrganisaatioShouldValidateNimetAlkuPvm() {
        LocalDate localDate = LocalDate.of(2016, Month.FEBRUARY, 15);
        Date date1 = Date.from(localDate.atTime(9, 39).toInstant(ZoneOffset.UTC));
        Date date2 = Date.from(localDate.atTime(9, 40).toInstant(ZoneOffset.UTC));
        OrganisaatioRDTO koulutustoimija = OrganisaatioRDTOTestUtil.createOrganisaatio("koulutustoimija1", OrganisaatioTyyppi.KOULUTUSTOIMIJA.value(), rootOid, true);
        koulutustoimija.getNimet().add(OrganisaatioRDTOTestUtil.createNimi("koulutustoimija11", date1));
        koulutustoimija.getNimet().add(OrganisaatioRDTOTestUtil.createNimi("koulutustoimija12", date2));

        Throwable throwable = catchThrowable(() -> service.saveOrUpdate(koulutustoimija));

        assertThat(throwable).isExactlyInstanceOf(OrganisaatioNameHistoryNotValidException.class);
    }

    @Test
    public void updateCurrentOrganisaatioNimet() {
        OrganisaatioRDTO oppilaitosRdto = OrganisaatioRDTOTestUtil.createOrganisaatio("nimi", OrganisaatioTyyppi.OPPILAITOS.value(), rootOid, true);
        oppilaitosRdto.getNimet().forEach(nimi -> nimi.setAlkuPvm(Date.from(LocalDate.now().minus(2, ChronoUnit.DAYS).atStartOfDay(ZoneId.systemDefault()).toInstant())));
        oppilaitosRdto.getNimet().add(OrganisaatioRDTOTestUtil.createNimi("toinen nimi", Date.from(LocalDate.now().plus(2, ChronoUnit.DAYS).atStartOfDay(ZoneId.systemDefault()).toInstant())));
        OrganisaatioResult organisaatioResult = this.service.saveOrUpdate(oppilaitosRdto);
        Date paivitysPvmOnCreate = organisaatioResult.getOrganisaatio().getPaivitysPvm();

        // Muutetaan tuleva alkupvm voimassa olevaksi tästä hetkestä eteen päin
        this.jdbcTemplate.update("UPDATE organisaatio_nimi SET alkupvm = CURRENT_TIMESTAMP WHERE alkupvm > CURRENT_TIMESTAMP ");
        this.service.updateCurrentOrganisaatioNimet();
        Organisaatio oppilaitos = this.organisaatioFindBusinessService.findById(organisaatioResult.getOrganisaatio().getOid());
        assertThat(oppilaitos.getNimi().getString("fi")).isEqualTo("toinen nimi");
        assertThat(oppilaitos.getPaivitysPvm()).isAfter(paivitysPvmOnCreate);
    }

    @Test
    public void UpdateNimiValues() {
        HashMap<String, String> oldParentNimi = new HashMap<>();
        oldParentNimi.put("fi", "Vanha parent oppilaitos");
        oldParentNimi.put("en", "Old parent oppilaitos");
        oldParentNimi.put("sv", "Gammalt parent oppilaitos");

        HashMap<String, String> newParentNimi = new HashMap<>();
        newParentNimi.put("fi", "Uusi parent oppilaitos");
        newParentNimi.put("en", "New parent oppilaitos");

        HashMap<String, String> currentChildNimi = new HashMap<>();
        currentChildNimi.put("fi", "Vanha parent oppilaitos, toimipiste");
        currentChildNimi.put("en", "toimipiste");
        currentChildNimi.put("sv", "toimipiste");

        service.updateNimiValues(oldParentNimi, currentChildNimi, newParentNimi);

        assertThat(currentChildNimi.get("fi")).isEqualTo("Uusi parent oppilaitos, toimipiste");
        assertThat(currentChildNimi.get("en")).isEqualTo("New parent oppilaitos, toimipiste");
        assertThat(currentChildNimi.get("sv")).isEqualTo("toimipiste");
    }


    @Test
    public void lisaaVarhaiskasvatuksenToimipaikkaTieto() {
        OrganisaatioRDTOV4 organisaatio = new OrganisaatioRDTOV4();
        organisaatio.setTyypit(Collections.singleton(OrganisaatioTyyppi.VARHAISKASVATUKSEN_TOIMIPAIKKA.koodiValue()));
        organisaatio.setParentOid("1.2.8000.1"); // Varhaiskasvatuksen järjestäjä
        OrganisaatioNimiRDTO nimi = OrganisaatioRDTOTestUtil.createNimi("nimi", new Date());
        organisaatio.setNimi(nimi.getNimi());
        organisaatio.setNimet(Collections.singletonList(nimi));

        VarhaiskasvatuksenToimipaikkaTiedotDto varhaiskasvatuksenToimipaikkaTiedotDto = new VarhaiskasvatuksenToimipaikkaTiedotDto();
        varhaiskasvatuksenToimipaikkaTiedotDto.setToimintamuoto("vardatoimintamuoto_tm02");
        varhaiskasvatuksenToimipaikkaTiedotDto.setKasvatusopillinenJarjestelma("vardakasvatusopillinenjarjestelma_kj99");
        VarhaiskasvatuksenToiminnallinepainotusDto varhaiskasvatuksenToiminnallinenpainotusDto = new VarhaiskasvatuksenToiminnallinepainotusDto();
        varhaiskasvatuksenToiminnallinenpainotusDto.setToiminnallinenpainotus("vardatoiminnallinenpainotus_tp99");
        varhaiskasvatuksenToiminnallinenpainotusDto.setAlkupvm(LocalDate.now());
        varhaiskasvatuksenToiminnallinenpainotusDto.setLoppupvm(LocalDate.now().plusDays(10));
        varhaiskasvatuksenToimipaikkaTiedotDto.setVarhaiskasvatuksenToiminnallinenpainotukset(Collections.singleton(varhaiskasvatuksenToiminnallinenpainotusDto));
        varhaiskasvatuksenToimipaikkaTiedotDto.setPaikkojenLukumaara(10L);
        VarhaiskasvatuksenKielipainotusDto varhaiskasvatuksenKielipainotusDto = new VarhaiskasvatuksenKielipainotusDto();
        varhaiskasvatuksenKielipainotusDto.setKielipainotus("kieli_bh");
        varhaiskasvatuksenKielipainotusDto.setAlkupvm(LocalDate.now());
        varhaiskasvatuksenKielipainotusDto.setLoppupvm(LocalDate.now().plusDays(10));
        varhaiskasvatuksenToimipaikkaTiedotDto.setVarhaiskasvatuksenKielipainotukset(Collections.singleton(varhaiskasvatuksenKielipainotusDto));
        varhaiskasvatuksenToimipaikkaTiedotDto.setVarhaiskasvatuksenJarjestamismuodot(Collections.singleton("vardajarjestamismuoto_jm03"));
        organisaatio.setVarhaiskasvatuksenToimipaikkaTiedot(varhaiskasvatuksenToimipaikkaTiedotDto);

        ResultRDTOV4 result = this.service.saveOrUpdate(organisaatio);
        assertThat(result.getOrganisaatio().getParentOid()).isEqualTo("1.2.8000.1");
        assertThat(result.getOrganisaatio().getParentOidPath()).isEqualTo("|1.2.246.562.24.00000000001|1.2.8000.1|");
        assertThat(result.getOrganisaatio().getTyypit()).containsExactly(OrganisaatioTyyppi.VARHAISKASVATUKSEN_TOIMIPAIKKA.koodiValue());
        assertThat(result.getOrganisaatio().getVarhaiskasvatuksenToimipaikkaTiedot())
                .extracting(VarhaiskasvatuksenToimipaikkaTiedotDto::getToimintamuoto,
                        VarhaiskasvatuksenToimipaikkaTiedotDto::getKasvatusopillinenJarjestelma,
                        VarhaiskasvatuksenToimipaikkaTiedotDto::getVarhaiskasvatuksenJarjestamismuodot,
                        VarhaiskasvatuksenToimipaikkaTiedotDto::getPaikkojenLukumaara)
                .containsExactly("vardatoimintamuoto_tm02", "vardakasvatusopillinenjarjestelma_kj99", Collections.singleton("vardajarjestamismuoto_jm03"), 10L);
        assertThat(result.getOrganisaatio().getVarhaiskasvatuksenToimipaikkaTiedot().getVarhaiskasvatuksenKielipainotukset())
                .extracting(VarhaiskasvatuksenKielipainotusDto::getKielipainotus)
                .containsExactly("kieli_bh");
        assertThat(result.getOrganisaatio().getVarhaiskasvatuksenToimipaikkaTiedot().getVarhaiskasvatuksenToiminnallinenpainotukset())
                .extracting(VarhaiskasvatuksenToiminnallinepainotusDto::getToiminnallinenpainotus)
                .containsExactly("vardatoiminnallinenpainotus_tp99");
    }


    @Test
    public void lisaaVarhaiskasvatuksenToimipaikkaTietoVaaralleTyypille() {
        OrganisaatioRDTOV4 organisaatio = new OrganisaatioRDTOV4();
        organisaatio.setTyypit(Collections.singleton(OrganisaatioTyyppi.TOIMIPISTE.koodiValue()));
        organisaatio.setParentOid("1.2.8000.1"); // Varhaiskasvatuksen järjestäjä
        OrganisaatioNimiRDTO nimi = OrganisaatioRDTOTestUtil.createNimi("nimi", new Date());
        organisaatio.setNimi(nimi.getNimi());
        organisaatio.setNimet(Collections.singletonList(nimi));

        VarhaiskasvatuksenToimipaikkaTiedotDto varhaiskasvatuksenToimipaikkaTiedotDto = new VarhaiskasvatuksenToimipaikkaTiedotDto();
        organisaatio.setVarhaiskasvatuksenToimipaikkaTiedot(varhaiskasvatuksenToimipaikkaTiedotDto);

        assertThatThrownBy(() -> this.service.saveOrUpdate(organisaatio))
                .isInstanceOf(ValidationException.class)
                .hasMessage("validation.Organisaatio.varhaiskasvatuksentoimipaikka.badorganisationtype");
    }

    @Test
    public void calculateToimipisteKoodiTest() {
        OrganisaatioRDTO koulutustoimija1 = OrganisaatioRDTOTestUtil.createOrganisaatio("koulutustoimija1", OrganisaatioTyyppi.KOULUTUSTOIMIJA.value(), rootOid, true);
        OrganisaatioResult koulutustoimijaResult1 = service.saveOrUpdate(koulutustoimija1);
        String koulutustoimija1Oid = koulutustoimijaResult1.getOrganisaatio().getOid();
        OrganisaatioRDTO oppilaitos1 = OrganisaatioRDTOTestUtil.createOrganisaatio("oppilaitos1", OrganisaatioTyyppi.OPPILAITOS.value(), koulutustoimija1Oid, true);
        oppilaitos1.setOppilaitosKoodi("oppilaitoskoodi");
        OrganisaatioResult oppilaitosResult1 = service.saveOrUpdate(oppilaitos1);
        String oppilaitos1oid = oppilaitosResult1.getOrganisaatio().getOid();

        OrganisaatioRDTO toimipiste1 = OrganisaatioRDTOTestUtil.createOrganisaatio("oppilaitos1, toimipiste1", OrganisaatioTyyppi.TOIMIPISTE.value(), oppilaitos1oid, true);
        OrganisaatioResult toimipisteResult1 = service.saveOrUpdate(toimipiste1);
        Organisaatio koulutustoimija = koulutustoimijaResult1.getOrganisaatio();
        Organisaatio toimipiste = toimipisteResult1.getOrganisaatio();
        Organisaatio oppilaitos = oppilaitosResult1.getOrganisaatio();

        assertThat(organisaatioBusinessServiceImpl.calculateToimipisteKoodi(toimipiste, koulutustoimija)).isEqualTo("oppilaitoskoodi01");
        toimipiste.setOpetuspisteenJarjNro(null);
        assertThat(organisaatioBusinessServiceImpl.calculateToimipisteKoodi(toimipiste, oppilaitos)).isEqualTo("");
        assertThat(organisaatioBusinessServiceImpl.calculateToimipisteKoodi(null, koulutustoimija)).isEqualTo("");
        assertThat(organisaatioBusinessServiceImpl.calculateToimipisteKoodi(oppilaitos, null)).isEqualTo("oppilaitoskoodi");
        assertThat(organisaatioBusinessServiceImpl.calculateToimipisteKoodi(koulutustoimija, oppilaitos)).isEqualTo("");
    }

    @Test
    public void testSetPaivittajaDataSetsCurrenUserAndPaivitysPvm() {
        Date epoch = new Date(0);
        OrganisaatioRDTO koulutustoimija1 = OrganisaatioRDTOTestUtil.createOrganisaatio("koulutustoimija1", OrganisaatioTyyppi.KOULUTUSTOIMIJA.value(), rootOid, true);
        OrganisaatioResult koulutustoimijaResult1 = service.saveOrUpdate(koulutustoimija1);
        koulutustoimijaResult1.getOrganisaatio().setPaivitysPvm(epoch);
        organisaatioBusinessServiceImpl.setPaivittajaData(koulutustoimijaResult1.getOrganisaatio());
        assertThat(koulutustoimijaResult1.getOrganisaatio().getPaivittaja()).isEqualTo("ophadmin");
        assertNotEquals(koulutustoimijaResult1.getOrganisaatio().getPaivitysPvm(), epoch);
    }

    private Organisaatio createOrganisaatio(String nimi, OrganisaatioTyyppi organisaatioTyyppi, String oid, String rootOid) {
        Organisaatio organisaatio = service
                .saveOrUpdate(OrganisaatioRDTOTestUtil.createOrganisaatio(nimi, organisaatioTyyppi.value(), oid, rootOid, true))
                .getOrganisaatio();
        setParentSuhteetStartDatesInThePast(organisaatio);
        organisaatioRepository.save(organisaatio);
        return organisaatio;
    }
}
