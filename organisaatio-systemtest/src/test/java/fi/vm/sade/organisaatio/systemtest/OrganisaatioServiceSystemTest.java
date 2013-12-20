package fi.vm.sade.organisaatio.systemtest;

import fi.vm.sade.generic.common.ValidationException;
import fi.vm.sade.organisaatio.api.model.OrganisaatioFatDTO;
import fi.vm.sade.organisaatio.integrationtest.OrganisaatioServiceTest;
import fi.vm.sade.organisaatio.integrationtest.OrganisaatioTstUtils;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;

import static fi.vm.sade.organisaatio.integrationtest.OrganisaatioTstUtils.buildYtunnus;
import static org.junit.Assert.*;

/**
 * Kokeellinen tapa ajaa service-moduulin integraatiotestejä myös systeemitesteinä koko stäkkiä vasten
 * (tosin ois kyllä parempi jos ei tarttis periä ollenkaan?)
 *
 * @author Antti
 */
@ContextConfiguration(locations = {
        "classpath:spring/cxf-client.xml"
})
public class OrganisaatioServiceSystemTest extends OrganisaatioServiceTest {

    // other test methods inherited

    @Test
    public void testServiceIsCxfProxy() {
        assertTrue("organisaatioService should JaxWsClientProxy: " + organisaatioService, organisaatioService.toString().contains("JaxWsClientProxy"));
    }

    @Test
    public void testTransactionsThroughEsb() throws ValidationException {
        // check we are using jax-ws through servicemix esb
        testServiceIsCxfProxy();
        // check we have real organisaatioservice on the other end of the bus instead of mock
        assertEquals("fi.vm.sade.organisaatio.service.OrganisaatioServiceImpl", organisaatioService.ping("..."));
        // call create organisaatio
        String nimi = "testTransactions";
        OrganisaatioFatDTO koulutustoimija = OrganisaatioTstUtils.createKoulutustoimija(organisaatioService, nimi, buildYtunnus(), "yhteyshlo");
        // check that created organisaatio is found in separate query, so we can be sure it is really stored in the db
        OrganisaatioFatDTO koulutustoimija2 = organisaatioService.read(koulutustoimija.getId());
        assertNotNull(koulutustoimija2);
    }

    // TODO: temp - ylikirjoitetaan testit jotka luottaa bisnespoikkeuksiin, jotka eivät vielä toimi ws yli - muuta kun toimii

    @Override
    public void createKoulutustoimija_cannotCreateWithoutMandatoryYhteystiedot() {
    }

    @Override
    public void createKoulutustoimija_cannotCreateWithSameYTunnus() throws ValidationException {
    }

    @Override
    public void createKoulutustoimija_cannotCreateWithSameNimi() throws ValidationException {
    }

}
