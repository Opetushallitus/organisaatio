package fi.vm.sade.organisaatio.resource;

import fi.vm.sade.organisaatio.SecurityAwareTestBase;
import fi.vm.sade.organisaatio.dto.v4.OrganisaatioHakutulosV4;
import fi.vm.sade.organisaatio.dto.v4.OrganisaatioPerustietoV4;
import fi.vm.sade.organisaatio.dto.v4.OrganisaatioRDTOV4;
import fi.vm.sade.organisaatio.resource.v4.OrganisaatioResourceV4;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;

@ContextConfiguration(locations = {"classpath:spring/test-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class OrganisaatioResourceV4Test extends SecurityAwareTestBase {

    @Autowired
    private OrganisaatioResourceV4 resource;

    @Before
    public void setup() {
        executeSqlScript("data/basic_organisaatio_data.sql", false);
    }

    @After
    public void cleanup() {
        executeSqlScript("data/truncate_tables.sql", false);
    }

    @Test
    public void findByOids() {
        Set<String> oids = singleton("1.2.8000.1");

        List<OrganisaatioRDTOV4> organisaatiot = resource.findByOids(oids);

        assertThat(organisaatiot).extracting(OrganisaatioRDTOV4::getOid).containsExactly("1.2.8000.1");
    }

    @Test
    public void findDescendantsReturnsAllDescendants() {
        String parentOid = "1.2.246.562.24.00000000001";
        String[] allDescendants = new String[] {
                "1.2.2004.1", "1.2.2004.2", "1.2.2004.3",
                "1.2.2004.4", "1.2.2004.5", "1.2.2004.6",
                "1.2.2005.4", "1.2.2005.5", "1.2.8000.1"
        };
        OrganisaatioHakutulosV4 results = resource.findDescendants(parentOid);
        assertThat(results.getNumHits()).isEqualTo(9);
        List<String> resultOids = results.getOrganisaatiot().stream()
                .map(OrganisaatioResourceV4Test::collectOids)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        assertThat(resultOids).containsExactlyInAnyOrder(allDescendants);
    }

    private static List<String> collectOids(OrganisaatioPerustietoV4 organisaatio) {
        List<String> oids = new ArrayList<>();
        oids.add(organisaatio.getOid());
        organisaatio.getChildren().forEach(child -> oids.addAll(collectOids(child)));
        return oids;
    }

    @Test
    public void findDescendantsReturnsCorrectHierarchy() {
        String parentOid = "1.2.246.562.24.00000000001";
        String childOid = "1.2.2004.1";
        String grandChildOid = "1.2.2004.2";
        OrganisaatioHakutulosV4 results = resource.findDescendants(parentOid);
        OrganisaatioPerustietoV4 organization = find(childOid, results.getOrganisaatiot());
        assertThat(organization).isNotNull();
        organization = find(grandChildOid, organization.getChildren());
        assertThat(organization).isNotNull();
    }

    private static OrganisaatioPerustietoV4 find(String oid, Set<OrganisaatioPerustietoV4> organizations) {
        return organizations.stream().filter(o -> o.getOid().equals(oid)).findFirst().orElse(null);
    }

    @Test
    public void findDescendantsReturnsCorrectParentOidPath() {
        String parentOid = "1.2.2004.1";
        String childOid = "1.2.2004.2";
        String expectedParentPath = "|1.2.246.562.24.00000000001|1.2.2004.1|";
        OrganisaatioHakutulosV4 results = resource.findDescendants(parentOid);
        OrganisaatioPerustietoV4 childOrg = results.getOrganisaatiot().stream().filter(
                organisaatio -> childOid.equals(organisaatio.getOid())
        ).findFirst().orElseThrow(() -> new IllegalStateException("Organisaatiota ei löydy: " + childOid));
        assertThat(childOrg.getParentOidPath()).isEqualTo(expectedParentPath);
    }

}
