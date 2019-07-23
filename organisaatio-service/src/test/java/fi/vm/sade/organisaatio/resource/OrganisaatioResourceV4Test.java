package fi.vm.sade.organisaatio.resource;

import fi.vm.sade.organisaatio.SecurityAwareTestBase;
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

import java.util.List;
import java.util.Set;

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

}
