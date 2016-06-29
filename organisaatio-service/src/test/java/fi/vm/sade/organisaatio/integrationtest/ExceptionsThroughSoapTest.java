package fi.vm.sade.organisaatio.integrationtest;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.eclipse.jetty.server.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import fi.vm.sade.organisaatio.api.model.GenericFault;
import fi.vm.sade.organisaatio.api.model.OrganisaatioService;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioSearchCriteriaDTO;

/**
 * @author Antti
 */
@Ignore
@ContextConfiguration(locations = {
        "classpath:cxf-client-for-test-jetty-only.xml"
})
@RunWith(SpringJUnit4ClassRunner.class)
public class ExceptionsThroughSoapTest {

    private static Server server;
    @Autowired
    private OrganisaatioService organisaatioService;

    /*@Ignore
    @Test
    public void clientSideServiceProxyThrowsBusinessExceptions() throws Exception {
        OrganisaatioTstUtils.checkBusinessException(organisaatioService);
    }
    */

    /*@BeforeClass
    public static void startJetty() throws Exception {
        server = JettyTstUtils.startJettyWithCxf(7070,
                "classpath:spring/test-context.xml",
                "classpath:META-INF/spring/context/ws-context.xml"
        );
    }

    @AfterClass
    public static void stopServer() throws Exception {
        JettyTstUtils.stop(server);
    }*/

}
