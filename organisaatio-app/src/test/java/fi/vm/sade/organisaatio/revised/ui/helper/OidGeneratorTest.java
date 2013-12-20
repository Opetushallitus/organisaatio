package fi.vm.sade.organisaatio.revised.ui.helper;

import junit.framework.Assert;

import org.junit.Test;

import fi.vm.sade.oid.service.ExceptionMessage;
import fi.vm.sade.oid.service.mock.OIDServiceMock;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioDTO;
import fi.vm.sade.organisaatio.api.model.types.OsoiteDTO;
import fi.vm.sade.organisaatio.api.model.types.YhteystietoArvoDTO;
import fi.vm.sade.organisaatio.api.model.types.YhteystietoDTO;

public class OidGeneratorTest {

    @Test
    public void test() throws ExceptionMessage {

        final OIDServiceMock mock = new OIDServiceMock();
        OrganisaatioDTO organisaatio = getOrganisaatio();
        Assert.assertEquals(1, organisaatio.getMuutOsoitteet().size());
        Assert.assertEquals(1, organisaatio.getYhteystiedot().size());
        Assert.assertEquals(1, organisaatio.getYhteystietoArvos().size());

        for (YhteystietoDTO yt : organisaatio.getYhteystiedot()) {
            Assert.assertNull(yt.getYhteystietoOid());
            OidGenerator.generateOid(yt, mock);
            Assert.assertNotNull(yt.getYhteystietoOid());
            final String oid = yt.getYhteystietoOid();
            OidGenerator.generateOid(yt, mock);
            Assert.assertEquals(oid, yt.getYhteystietoOid());
        }

        for (YhteystietoDTO yt : organisaatio.getMuutOsoitteet()) {
            Assert.assertNull(yt.getYhteystietoOid());
            OidGenerator.generateOid(yt, mock);
            Assert.assertNotNull(yt.getYhteystietoOid());
            final String oid = yt.getYhteystietoOid();
            OidGenerator.generateOid(yt, mock);
            Assert.assertEquals(oid, yt.getYhteystietoOid());
        }

        organisaatio = getOrganisaatio();
        OidGenerator.generateOids(organisaatio, mock);
        
        for (YhteystietoDTO yt : organisaatio.getYhteystiedot()) {
            Assert.assertNotNull(yt.getYhteystietoOid());
        }
        
        Assert.assertNotNull(organisaatio.getOid());
        final String oid = organisaatio.getOid();
        OidGenerator.generateOids(organisaatio, mock);
        Assert.assertEquals(oid, organisaatio.getOid());

        for (YhteystietoDTO yt : organisaatio.getMuutOsoitteet()) {
            Assert.assertNotNull(yt.getYhteystietoOid());
        }

    }

    private OrganisaatioDTO getOrganisaatio() {
        OrganisaatioDTO organisaatio = new OrganisaatioDTO();
        organisaatio.getYhteystiedot().add(new YhteystietoDTO() {
        });

        organisaatio.getMuutOsoitteet().add(new OsoiteDTO());
        organisaatio.getYhteystietoArvos().add(new YhteystietoArvoDTO());
        return organisaatio;
    }

}
