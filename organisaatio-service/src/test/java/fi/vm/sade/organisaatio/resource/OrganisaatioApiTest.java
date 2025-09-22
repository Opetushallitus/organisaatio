package fi.vm.sade.organisaatio.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.vm.sade.oid.OIDService;
import fi.vm.sade.organisaatio.SecurityAwareTestBase;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.auth.OrganisaatioPermissionServiceImpl;
import fi.vm.sade.organisaatio.dto.v4.OrganisaatioHakutulosV4;
import fi.vm.sade.organisaatio.dto.v4.OrganisaatioPerustietoV4;
import fi.vm.sade.organisaatio.dto.v4.OrganisaatioRDTOV4;
import fi.vm.sade.organisaatio.util.OrganisaatioRDTOTestUtil;
import fi.vm.sade.organisaatio.ytj.api.YTJService;
import fi.vm.sade.security.OidProvider;
import fi.vm.sade.security.OrganisationHierarchyAuthorizer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.mock;

import static fi.vm.sade.organisaatio.util.OrganisaatioRDTOTestUtil.OPH_OID;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
@ComponentScan(basePackages = "fi.vm.sade.organisaatio")
@SpringBootTest
@Sql("/data/truncate_tables.sql")
class OrganisaatioApiTest extends SecurityAwareTestBase {


    @TestConfiguration
    static class TestContextConfiguration {

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
    }

    @Autowired
    private OrganisaatioApi resource;

    @BeforeEach
    public void setup() {
        executeSqlScript("classpath:data/basic_organisaatio_data.sql", false);
    }

    @AfterEach
    public void cleanup() {
        executeSqlScript("classpath:data/truncate_tables.sql", false);
    }

    @Test
    void findByOids() {
        Set<String> oids = singleton("1.2.8000.1");

        List<OrganisaatioRDTOV4> organisaatiot = resource.findByOids(oids);

        assertThat(organisaatiot).extracting(OrganisaatioRDTOV4::getOid).containsExactly("1.2.8000.1");
    }

    @Test
    void findDescendantsReturnsAllDescendants() {
        String parentOid = "1.2.246.562.24.00000000001";
        String[] allDescendants = new String[]{
                "1.2.2004.1", "1.2.2004.2", "1.2.2004.3",
                "1.2.2004.4", "1.2.2004.5", "1.2.2004.6",
                "1.2.2005.4", "1.2.2005.5", "1.2.8000.1",
                "1.2.2020.1", "1.2.8001.2"
        };
        OrganisaatioHakutulosV4 results = resource.findDescendants(parentOid);
        assertThat(results.getNumHits()).isEqualTo(11);
        List<String> resultOids = results.getOrganisaatiot().stream()
                .map(OrganisaatioApiTest::collectOids)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        assertThat(resultOids).containsExactlyInAnyOrder(allDescendants);
    }

    @Test
    void findDescendantsReturnsPublicDescendantsOnly() {
        String parentOid = "1.2.8000.1";
        setCurrentUser("1.2.3.4.5", getAuthority(
                "APP_" + OrganisaatioPermissionServiceImpl.ORGANISAATIOHALLINTA + "_CRUD",
                parentOid));
        OrganisaatioHakutulosV4 results = resource.findDescendants(parentOid);
        assertThat(results.getNumHits()).isEqualTo(0); // ainoa jälkeläinen on piilotettu
    }

    private static List<String> collectOids(OrganisaatioPerustietoV4 organisaatio) {
        List<String> oids = new ArrayList<>();
        oids.add(organisaatio.getOid());
        organisaatio.getChildren().forEach(child -> oids.addAll(collectOids(child)));
        return oids;
    }

    @Test
    void findDescendantsReturnsCorrectHierarchy() {
        String parentOid = "1.2.246.562.24.00000000001";
        String childOid = "1.2.2004.1";
        String grandChildOid = "1.2.2004.2";
        OrganisaatioHakutulosV4 results = resource.findDescendants(parentOid);
        OrganisaatioPerustietoV4 organization = find(childOid, results.getOrganisaatiot());
        assertThat(organization).isNotNull();
        assertThat(organization.getParentOid()).isEqualTo(parentOid);
        organization = find(grandChildOid, organization.getChildren());
        assertThat(organization).isNotNull();
        assertThat(organization.getParentOid()).isEqualTo(childOid);
    }

    private static OrganisaatioPerustietoV4 find(String oid, Set<OrganisaatioPerustietoV4> organizations) {
        return organizations.stream().filter(o -> o.getOid().equals(oid)).findFirst().orElse(null);
    }

    @Test
    void findDescendantsReturnsCorrectParentOidPath() {
        String parentOid = "1.2.2004.1";
        String childOid = "1.2.2004.2";
        String expectedParentPath = "1.2.246.562.24.00000000001/1.2.2004.1";
        OrganisaatioHakutulosV4 results = resource.findDescendants(parentOid);
        OrganisaatioPerustietoV4 childOrg = results.getOrganisaatiot().stream().filter(
                organisaatio -> childOid.equals(organisaatio.getOid())
        ).findFirst().orElseThrow(() -> new IllegalStateException("Organisaatiota ei löydy: " + childOid));
        assertThat(childOrg.getParentOidPath()).isEqualTo(expectedParentPath);
    }

    @Test
    void newOrganisaatioSavesYhteystiedot() {
        setCurrentUser("1.2.3.4.5", getAuthority("ROLE_APP_ORGANISAATIOHALLINTA_CRUD", OPH_OID));
        OrganisaatioRDTOV4 model = OrganisaatioRDTOTestUtil.createOrganisaatioV4("orgwithyhteystiedot", OrganisaatioTyyppi.MUU_ORGANISAATIO, null, OPH_OID);
        model.setYhteystiedot(Set.of(
            Map.of(
                "kieli", "kieli_fi#1",
                "osoite", "posti 1",
                "osoiteTyyppi", "posti",
                "postinumeroUri", "posti_00100",
                "postitoimipaikka", "HELSINKI"
            ),
            Map.of(
                "kieli", "kieli_fi#1",
                "osoite", "posti 2",
                "osoiteTyyppi", "kaynti",
                "postinumeroUri", "posti_00100",
                "postitoimipaikka", "HELSINKI"
            ),
            Map.of(
                "email", "testi@muusi.com",
                "kieli", "kieli_fi#1"
            )
        ));
        model.setAlkuPvm(new Date());
        OrganisaatioRDTOV4 organisaatioResult = resource.newOrganisaatio(model).getOrganisaatio();

        OrganisaatioRDTOV4 savedOrg = resource.findByOids(Set.of(organisaatioResult.getOid())).get(0);
        assertThat(savedOrg.getPostiosoite()).contains(
                entry("osoite", "posti 1"),
                entry("postinumeroUri", "posti_00100"),
                entry("postitoimipaikka", "HELSINKI"));

        assertThat(savedOrg.getKayntiosoite()).contains(
                entry("osoite", "posti 2"),
                entry("postinumeroUri", "posti_00100"),
                entry("postitoimipaikka", "HELSINKI"));
        assertThat(savedOrg
                .getYhteystiedot()
                .stream()
                .filter((y) -> {
                    return y.get("email") != null;
                })
                .findFirst()
                .get()
                .get("email")
            ).isEqualTo("testi@muusi.com");
    }

    String organisationJsonWithIdInYhteystieto = """
            {
              "nimi": {
                "fi": "Koulutuspalvelut Oy"
              },
              "alkuPvm": "2016-05-23",
              "parentOid": "1.2.246.562.24.00000000001",
              "tyypit": [
                "organisaatiotyyppi_02"
              ],
              "yhteystiedot": [
                {
                  "numero": "0122331",
                  "tyyppi": "puhelin",
                  "kieli": "kieli_fi#1",
                  "id": "20987888"
                }
              ],
              "nimet": [
                {
                  "nimi": {
                    "fi": "Koulutuspalvelut Oy"
                  },
                  "alkuPvm": "2024-05-07"
                }
              ],
              "oppilaitosTyyppiUri": "oppilaitostyyppi_xx#1",
              "kotipaikkaUri": "kunta_186",
              "maaUri": "maatjavaltiot1_fin",
              "kieletUris": [
                "oppilaitoksenopetuskieli_1#1"
              ],
              "ytjkieli": "kieli_fi#1"
            }
            """;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void newOrganisaatioIgnoresIdInYhteystieto() throws JsonProcessingException {
        setCurrentUser("1.2.3.4.5", getAuthority("ROLE_APP_ORGANISAATIOHALLINTA_CRUD", OPH_OID));
        var organisaatio = objectMapper.readValue(organisationJsonWithIdInYhteystieto, OrganisaatioRDTOV4.class);
        resource.newOrganisaatio(organisaatio);
    }
}
