package fi.vm.sade.koodisto.selenium;

import fi.vm.sade.generic.common.ValidationException;
import fi.vm.sade.organisaatio.api.model.OrganisaatioFatDTO;
import fi.vm.sade.organisaatio.integrationtest.OrganisaatioTstUtils;
import fi.vm.sade.organisaatio.service.OrganisaatioService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static fi.vm.sade.organisaatio.integrationtest.OrganisaatioTstUtils.buildYtunnus;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Antti Salonen
 */
@ContextConfiguration(locations = "classpath:spring/cxf-client.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class ServiceApiSpringTest {

    @Autowired
    private OrganisaatioService organisaatioService;

    @Test
    public void testServiceIsCxfProxy() {
        assertTrue("organisaatioService should JaxWsClientProxy: " + organisaatioService, organisaatioService.toString().contains("JaxWsClientProxy"));
    }

    @Test
    public void testServiceProxyPing() throws ValidationException {
        assertEquals("fi.vm.sade.organisaatio.service.OrganisaatioServiceImpl", organisaatioService.ping("asd"));
    }

    @Test
    public void testServiceProxyWithDTOParam() throws ValidationException {
        String nimi = "testServiceProxyWithDTOParam";
        OrganisaatioFatDTO param = OrganisaatioTstUtils.createKoulutustoimija(organisaatioService, nimi, buildYtunnus(), "yhteyshlo");
        assertEquals(nimi, param.getNimi());
    }

    /* TODO: poikkeukset ws:n läpi!!!
    @Test
    public void testServiceProxyWithBusinessException() throws ValidationException {
        KoulutustoimijaFatDTO param = new KoulutustoimijaFatDTO();
        param.setNimiFi("foo");
        organisaatioService.createKoulutustoimija(param);
        try {
            organisaatioService.createKoulutustoimija(param);
            fail("should throw business exception");
        } catch (Exception e) {
            if (e instanceof ValidationException) {
                // ok
            } else {
                fail("should throw ValidationException, but threw: "+e);
            }
        }
    }
    */

}
