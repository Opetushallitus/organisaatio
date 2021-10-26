package fi.vm.sade.organisaatio.resource;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.api.search.OrganisaatioHakutulos;
import fi.vm.sade.organisaatio.api.search.OrganisaatioPerustieto;
import fi.vm.sade.organisaatio.api.search.OrganisaatioSearchCriteria;
import fi.vm.sade.organisaatio.dto.v2.OrganisaatioSearchCriteriaDTOV2;
import fi.vm.sade.organisaatio.repository.OrganisaatioRepository;
import fi.vm.sade.organisaatio.resource.dto.HakutoimistoDTO;
import fi.vm.sade.organisaatio.resource.dto.OrganisaatioRDTO;
import fi.vm.sade.organisaatio.resource.dto.ResultRDTO;
import fi.vm.sade.organisaatio.resource.v2.OrganisaatioResourceV2;
import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@org.springframework.test.context.jdbc.Sql(
        scripts = "classpath:data/basic_organisaatio_data.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
@org.springframework.test.context.jdbc.Sql(
        scripts = "classpath:data/truncate_tables.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
)
@ActiveProfiles("dev")
@ComponentScan(basePackages = "fi.vm.sade.organisaatio")
@SpringBootTest
@AutoConfigureTestDatabase
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class OrganisaatioResourceTest {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    @Autowired
    private OrganisaatioResource res;
    @Autowired
    private OrganisaatioRepository organisaatioRepository;

    @Autowired
    private OrganisaatioResourceV2 res2;

    @Value("${root.organisaatio.oid}")
    private String rootOrganisaatioOid;


    public OrganisaatioResourceTest() {
        Locale.setDefault(Locale.US);
    }

    @Test
    public void testParentOids() throws Exception {
        String reference = Joiner.on("/").join(
                new String[]{rootOrganisaatioOid, "1.2.2004.1", "1.2.2004.3", "1.2.2005.4"});

        String s = res.parentoids("1.2.2005.4");
        assertEquals(reference, s);
    }

    @Test
    public void testParentOidsWithRoot() throws Exception {
        String reference = Joiner.on("/").join(
                new String[]{rootOrganisaatioOid});

        String s = res.parentoids(rootOrganisaatioOid);
        assertEquals(reference, s);
    }

    @Test
    public void testParentOidsWithoutOrg() throws Exception {
        String reference = Joiner.on("/").join(
                new String[]{rootOrganisaatioOid, "does_not_exist"});

        String s = res.parentoids("does_not_exist");
        assertEquals(reference, s);
    }

    @Test
    @WithMockUser(roles = {"APP_ORGANISAATIOHALLINTA", "APP_ORGANISAATIOHALLINTA_READ_UPDATE_1.2.246.562.24.00000000001"})
    public void testChangeParentOid() throws Exception {
        String oldParentOid = "1.2.2004.1";
        String parentOid = "1.2.2004.5";

        // Change parent from root -> root2
        OrganisaatioRDTO node2foo = res.getOrganisaatioByOID("1.2.2004.3", false);
        node2foo.setParentOid(parentOid);
        ResultRDTO updated = res.updateOrganisaatio(node2foo.getOid(), node2foo);
        assertEquals(parentOid, updated.getOrganisaatio().getParentOid(), "Parent oid should match!");
        LOG.info("Path: {}", updated.getOrganisaatio().getParentOidPath());
        List<OrganisaatioRDTO> children = res.children(updated.getOrganisaatio().getOid(), false);
        assertEquals(2, children.size(), "Children count should match!");
        for (OrganisaatioRDTO child : children) {
            LOG.info("Child oid path: {}, id path: {}", child.getParentOidPath());
            assertEquals(updated.getOrganisaatio().getParentOidPath() + child.getParentOid() + "|",
                    child.getParentOidPath(), "Child parent oid path should match!");
        }
    }

    @Test
    @WithMockUser(roles = {"APP_ORGANISAATIOHALLINTA", "APP_ORGANISAATIOHALLINTA_READ_UPDATE_1.2.246.562.24.00000000001"})
    public void testSearchOrganisaatios() throws Exception {
        //Finding all koulutustoimijat
        OrganisaatioSearchCriteria searchCriteria = createOrgSearchCriteria(OrganisaatioTyyppi.KOULUTUSTOIMIJA.value(), null, null, true, null);
        OrganisaatioHakutulos result = res.searchHierarchy(searchCriteria);
        assertEquals(6, result.getNumHits());

        //List roots
        Set<String> oidList = new HashSet<>();
        oidList.add("1.2.2004.1");
        oidList.add("1.2.2004.5");
        searchCriteria = createOrgSearchCriteria(null, null, null, true, oidList);
        result = res.searchHierarchy(searchCriteria);
        for (OrganisaatioPerustieto org : result.getOrganisaatiot()) {
            LOG.debug("ORG: {}", org.getOid());
        }
        assertEquals(7, result.getNumHits());
        assertThat(result.getOrganisaatiot())
                .flatExtracting(perustieto -> Stream.concat(Stream.of(perustieto.getOid()), this.allChildrenFlat(perustieto.getChildren()).map(OrganisaatioPerustieto::getOid)).collect(Collectors.toSet()))
                // 1.2.2004.6 on lakkautettu joten se ei ole validi vaikka sillä onkin 1.2.2004.5 organisaatio indeksin parent pathissaan
                .containsExactlyInAnyOrder("1.2.2004.1", "1.2.2004.2", "1.2.2004.3", "1.2.2004.4", "1.2.2005.4", "1.2.2004.5", "1.2.2005.5");

        //Finding all organisaatios with bar in name
        searchCriteria = createOrgSearchCriteria(null, null, "bar", true, null);
        result = res.searchHierarchy(searchCriteria);
        for (OrganisaatioPerustieto org : result.getOrganisaatiot()) {
            LOG.debug("ORG: {}", org.getOid());
        }
        assertEquals(5, result.getNumHits());

        //Finding only organisaatios that are of oppilaitostyyppi Ammattikorkeakoulut
        searchCriteria = createOrgSearchCriteria(null, "oppilaitostyyppi_41#1", null, true, null);
        result = res.searchHierarchy(searchCriteria);
        assertEquals(2, result.getNumHits());

        //Finding only organisaatios that are of organisaatiotyyppi Varhaiskasvatuksen jarjestaja
        searchCriteria = createOrgSearchCriteria(OrganisaatioTyyppi.VARHAISKASVATUKSEN_JARJESTAJA.value(), null, null, true, null);
        result = res.searchHierarchy(searchCriteria);
        assertEquals(2, result.getNumHits()); // the matching organization plus its child
        assertThat(result.getOrganisaatiot())
                .flatExtracting(OrganisaatioPerustieto::getOrganisaatiotyypit)
                .containsExactlyInAnyOrder(OrganisaatioTyyppi.OPPILAITOS, OrganisaatioTyyppi.VARHAISKASVATUKSEN_JARJESTAJA);
    }

    private Stream<OrganisaatioPerustieto> allChildrenFlat(Collection<OrganisaatioPerustieto> organisaatioPerustieto) {
        return Stream.concat(organisaatioPerustieto.stream(), organisaatioPerustieto.stream().flatMap(child -> CollectionUtils.isEmpty(child.getChildren()) ? Stream.empty() : this.allChildrenFlat(child.getChildren())));
    }

    @Test
    @WithMockUser(roles = {"APP_ORGANISAATIOHALLINTA", "APP_ORGANISAATIOHALLINTA_READ_UPDATE_1.2.246.562.24.00000000001"})
    public void testSearchHierarchyReturnsToimipistekoodi() throws Exception {
        // Get the hierarchy
        OrganisaatioSearchCriteriaDTOV2 searchCriteria = createOrgSearchCriteriaDTOV2();
        OrganisaatioHakutulos result = res2.searchOrganisaatioHierarkia(searchCriteria);
        assertEquals(9, result.getNumHits());
        for (OrganisaatioPerustieto org : result.getOrganisaatiot()) {
            if(org.getOid().equals("1.2.2004.1")) {
                assertNotNull(org.getOppilaitosKoodi());
                // see test data
                for(OrganisaatioPerustieto child : org.getChildren()) {
                    if(child.getOid().equals("1.2.2004.1")) {
                        assertNotNull(child.getToimipistekoodi());
                        assertEquals("123451", child.getToimipistekoodi());
                    } else if (child.getOid().equals("1.2.2004.3")) {
                        assertNotNull(child.getToimipistekoodi());
                        assertEquals("123452", child.getToimipistekoodi());
                    }
                }
            }
            LOG.debug("ORG: {}", org.getOid());
        }
    }


    @Test
    public void testFetchingHakutoimisto() throws Exception {
        HakutoimistoDTO hakutoimisto = (HakutoimistoDTO) res2.hakutoimisto("1.2.2004.4");
        assertEquals("Hakutoimiston nimi FI", hakutoimisto.nimi.get("kieli_fi#1"));
        HakutoimistoDTO expected = new HakutoimistoDTO(
                ImmutableMap.of("kieli_fi#1", "Hakutoimiston nimi FI", "kieli_en#1", "Hakutoimiston nimi EN"),
                ImmutableMap.of(
                        "kieli_fi#1", new HakutoimistoDTO.HakutoimistonYhteystiedotDTO(hakutoimistonOsoite("1.2.2004.4", "fi"), hakutoimistonOsoite("1.2.2004.5", "fi"), "http://www.foo.fi", "foo@bar.com", "123456789"),
                        "kieli_sv#1", new HakutoimistoDTO.HakutoimistonYhteystiedotDTO(hakutoimistonOsoite("1.2.2004.6", "sv"), null, null, null, null),
                        "kieli_en#1", new HakutoimistoDTO.HakutoimistonYhteystiedotDTO(hakutoimistonOsoite("1.2.2004.7", "en"), hakutoimistonOsoite("1.2.2004.8", "en"), "http://www.foo.fi/en", null, null)));

        assertEquals(expected, hakutoimisto);
    }

    @Test
    public void testMixedOsoitetyyppi() throws Exception {
        HakutoimistoDTO hakutoimisto = (HakutoimistoDTO) res2.hakutoimisto("1.2.8000.1");
        assertEquals("Hakutoimiston nimi EN", hakutoimisto.nimi.get("kieli_en#1"));
        HakutoimistoDTO expected = new HakutoimistoDTO(
                ImmutableMap.of("kieli_fi#1", "Hakutoimiston nimi FI", "kieli_en#1", "Hakutoimiston nimi EN"),
                ImmutableMap.of(
                        "kieli_en#1", new HakutoimistoDTO.HakutoimistonYhteystiedotDTO(hakutoimistonOsoite("1.2.2004.9", "en"), hakutoimistonOsoite("1.2.2004.10", "en"), null, null, null)));

        assertEquals(expected, hakutoimisto);
    }

    private HakutoimistoDTO.OsoiteDTO hakutoimistonOsoite(String yhteystietoOid, String lang) {
        if("en".equals(lang)) {
            return new HakutoimistoDTO.OsoiteDTO(yhteystietoOid, "Hassuttimenkatu 2, 10000 Juupajoki, Finland", null, null);
        }
        return new HakutoimistoDTO.OsoiteDTO(yhteystietoOid, "fi".equals(lang) ? "Hassuttimenkatu 2" : "Hassutingatan 2", "posti_10000" , "Juupajoki");
    }

    @Test
    public void testFetchingHakutoimistoForMissingOrganisation() {
        ResponseStatusException thrown = assertThrows(
                ResponseStatusException.class,
                () -> res2.hakutoimisto("non.existing.oid"));
        assertEquals(404, thrown.getStatus().value());
    }

    @Test
    public void testFetchingMissingHakutoimisto() {
        ResponseStatusException thrown = assertThrows(
                ResponseStatusException.class,
                () -> res2.hakutoimisto("1.2.2004.6"));
        assertEquals(404, thrown.getStatus().value());
    }

    private OrganisaatioSearchCriteria createOrgSearchCriteria(String organisaatioTyyppi, String oppilaitosTyyppi, String searchStr,
                                                               boolean suunnitellut, Set<String> oids) {
        OrganisaatioSearchCriteria sc = new OrganisaatioSearchCriteria();
        sc.setOrganisaatioTyyppi(organisaatioTyyppi);//organisaatioTyyppi = organisaatioTyyppi;
        Set<String> tyypit = new HashSet<>();
        if (!StringUtils.isEmpty(oppilaitosTyyppi)) {
            tyypit.add(oppilaitosTyyppi);
        }
        sc.setOppilaitosTyyppi(tyypit);
        sc.setSearchStr(searchStr);
        sc.setSuunnitellut(suunnitellut);
        if (oids != null) {
            sc.setOidRestrictionList(oids);
        }
        return sc;

    }

    private OrganisaatioSearchCriteriaDTOV2 createOrgSearchCriteriaDTOV2() {
        OrganisaatioSearchCriteriaDTOV2 sc = new OrganisaatioSearchCriteriaDTOV2();
        sc.setLakkautetut(false);
        sc.setAktiiviset(true);
        sc.setSuunnitellut(true);
        return sc;
    }
}
