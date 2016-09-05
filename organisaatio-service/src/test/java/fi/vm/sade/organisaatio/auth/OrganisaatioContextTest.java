package fi.vm.sade.organisaatio.auth;

import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.api.search.OrganisaatioPerustieto;
import fi.vm.sade.organisaatio.resource.dto.OrganisaatioRDTO;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class OrganisaatioContextTest {

    @Test
    public void test() {
        // with fat
        OrganisaatioRDTO org = new OrganisaatioRDTO();
        org.setOid("1");
        org.getTyypit().add(OrganisaatioTyyppi.MUU_ORGANISAATIO.value());

        Map<String,String> teksti = new HashMap<>();
        teksti.put("fi", "nimi");
        org.setNimi(teksti);
        OrganisaatioContext context = OrganisaatioContext.get(org);
        Assert.assertEquals("1", context.getOrgOid());
        Assert.assertTrue(context.getOrgTypes().contains(OrganisaatioTyyppi.MUU_ORGANISAATIO));
        Assert.assertEquals(1, context.getOrgTypes().size());
        Assert.assertNotNull(context.toString());

        context = OrganisaatioContext.get((OrganisaatioRDTO)null);
        Assert.assertNotNull(context.toString());

        // with perus
        OrganisaatioPerustieto perus = new OrganisaatioPerustieto();
        perus.setOid("1");
        perus.getOrganisaatiotyypit().add(OrganisaatioTyyppi.MUU_ORGANISAATIO);
        perus.setNimi("fi", "nimi");
        context = OrganisaatioContext.get(perus);
        Assert.assertEquals("1", context.getOrgOid());
        Assert.assertTrue(context.getOrgTypes().contains(OrganisaatioTyyppi.MUU_ORGANISAATIO));
        Assert.assertEquals(1, context.getOrgTypes().size());
        Assert.assertNotNull(context.toString());

        context = OrganisaatioContext.get((OrganisaatioPerustieto)null);
        Assert.assertNotNull(context.toString());
    }

}
