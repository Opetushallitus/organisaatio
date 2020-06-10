package fi.vm.sade.organisaatio.resource;

import fi.vm.sade.oid.service.OIDService;
import fi.vm.sade.oid.service.mock.OIDServiceMock;
import fi.vm.sade.organisaatio.SecurityAwareTestBase;
import fi.vm.sade.organisaatio.business.impl.OrganisaatioKoodistoMock;
import fi.vm.sade.organisaatio.dto.v4.OrganisaatioRDTOV4;
import fi.vm.sade.organisaatio.resource.v4.OrganisaatioResourceV4;
import fi.vm.sade.rajapinnat.ytj.api.YTJService;
import fi.vm.sade.rajapinnat.ytj.mock.YTJServiceMock;
import fi.vm.sade.security.OidProvider;
import fi.vm.sade.security.OrganisationHierarchyAuthorizer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;

@RunWith(SpringRunner.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@SpringBootTest
@AutoConfigureTestDatabase
public class OrganisaatioResourceV4Test extends SecurityAwareTestBase {


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
            return spy(new YTJServiceMock());
        }

        @Bean
        @Primary
        public OIDService oidService() {
            return new OIDServiceMock();
        }
    }
    @Autowired
    private OrganisaatioResourceV4 resource;

    @Before
    public void setup() {
        executeSqlScript("classpath:data/basic_organisaatio_data.sql", false);
    }

    @After
    public void cleanup() {
        executeSqlScript("classpath:data/truncate_tables.sql", false);
    }

    @Test
    public void findByOids() {
        Set<String> oids = singleton("1.2.8000.1");

        List<OrganisaatioRDTOV4> organisaatiot = resource.findByOids(oids);

        assertThat(organisaatiot).extracting(OrganisaatioRDTOV4::getOid).containsExactly("1.2.8000.1");
    }

}
