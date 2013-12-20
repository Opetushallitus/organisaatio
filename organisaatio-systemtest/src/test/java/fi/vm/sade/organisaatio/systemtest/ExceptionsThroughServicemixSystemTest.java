package fi.vm.sade.organisaatio.systemtest;

import fi.vm.sade.organisaatio.integrationtest.OrganisaatioTstUtils;
import fi.vm.sade.organisaatio.service.OrganisaatioService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration(locations = {
        "classpath:spring/cxf-client.xml"
})
@RunWith(SpringJUnit4ClassRunner.class)
public class ExceptionsThroughServicemixSystemTest {

    @Autowired
    private OrganisaatioService organisaatioService;

    @Test
    public void clientSideServiceProxyThrowsBusinessExceptions() throws Exception {
        OrganisaatioTstUtils.checkBusinessException(organisaatioService);
    }

}
