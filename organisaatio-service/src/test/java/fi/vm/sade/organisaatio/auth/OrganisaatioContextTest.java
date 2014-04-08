package fi.vm.sade.organisaatio.auth;

import junit.framework.Assert;

import org.junit.Test;

import fi.vm.sade.organisaatio.api.model.types.MonikielinenTekstiTyyppi;
import fi.vm.sade.organisaatio.api.model.types.MonikielinenTekstiTyyppi.Teksti;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioDTO;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.api.search.OrganisaatioPerustieto;

public class OrganisaatioContextTest {

    @Test
    public void test() {
        // with fat
        OrganisaatioDTO org = new OrganisaatioDTO();
        org.setOid("1");
        org.getTyypit().add(OrganisaatioTyyppi.MUU_ORGANISAATIO);

        MonikielinenTekstiTyyppi mktt = new MonikielinenTekstiTyyppi();
        Teksti teksti = new Teksti();
        teksti.setKieliKoodi("fi");
        teksti.setValue("nimi");
        mktt.getTeksti().add(teksti);
        org.setNimi(mktt);
        OrganisaatioContext context = OrganisaatioContext.get(org);
        Assert.assertEquals("1", context.getOrgOid());
        Assert.assertTrue(context.getOrgTypes().contains(OrganisaatioTyyppi.MUU_ORGANISAATIO));
        Assert.assertEquals(1, context.getOrgTypes().size());
        Assert.assertNotNull(context.toString());

        context = OrganisaatioContext.get((OrganisaatioDTO)null);
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
