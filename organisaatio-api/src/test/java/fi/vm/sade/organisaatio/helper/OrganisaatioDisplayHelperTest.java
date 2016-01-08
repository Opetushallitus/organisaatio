package fi.vm.sade.organisaatio.helper;

import java.util.Locale;

import org.junit.Assert;

import org.junit.Test;

import fi.vm.sade.organisaatio.api.model.types.EmailDTO;
import fi.vm.sade.organisaatio.api.model.types.MonikielinenTekstiTyyppi;
import fi.vm.sade.organisaatio.api.model.types.MonikielinenTekstiTyyppi.Teksti;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioDTO;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.api.model.types.OsoiteDTO;
import fi.vm.sade.organisaatio.api.model.types.WwwDTO;
import fi.vm.sade.organisaatio.api.search.OrganisaatioPerustieto;

public class OrganisaatioDisplayHelperTest {

    @Test
    public void testFormatOsoiteAsString() {

        OsoiteDTO osoite = new OsoiteDTO();
        osoite.setOsoite("jokukatu 2");
        osoite.setPostinumero("00202");
        osoite.setMaa("Suomi");
        osoite.setPostitoimipaikka("Espoo");
        Assert.assertEquals("jokukatu 2, 00202, Espoo, Suomi", OrganisaatioDisplayHelper.formatOsoiteAsString(osoite));
    }

    @Test
    public void testGetAvailableName() {
        OrganisaatioDTO dto = new OrganisaatioDTO();
        Assert.assertEquals("", OrganisaatioDisplayHelper.getAvailableName(dto));
        MonikielinenTekstiTyyppi mktt = new MonikielinenTekstiTyyppi();
        dto.setNimi(mktt);
        Teksti teksti = new Teksti();
        teksti.setKieliKoodi("sv");
        teksti.setValue("nimi");
        mktt.getTeksti().add(teksti);
        Assert.assertEquals("nimi", OrganisaatioDisplayHelper.getAvailableName(dto));
        teksti = new Teksti();
        teksti.setKieliKoodi("fi");
        teksti.setValue("nimi2");
        mktt.getTeksti().add(teksti);
        Assert.assertEquals("nimi", OrganisaatioDisplayHelper.getAvailableName(dto));
    }

    @Test
    public void testGetAvailableNameBasic() {
        OrganisaatioPerustieto optt = new OrganisaatioPerustieto();
        Assert.assertEquals("", OrganisaatioDisplayHelper.getAvailableNameBasic(optt));
        optt.setNimi("sv", "nimi");
        Assert.assertEquals("nimi", OrganisaatioDisplayHelper.getAvailableNameBasic(optt));
        optt.setNimi("fi", "nimi2");
        Assert.assertEquals("nimi2", OrganisaatioDisplayHelper.getAvailableNameBasic(optt));
    }

    @Test
    public void testGetCaption() {
        OrganisaatioDTO dto = new OrganisaatioDTO();
        dto.setYtunnus("1234567-1");
        dto.getTyypit().add(OrganisaatioTyyppi.KOULUTUSTOIMIJA);
        MonikielinenTekstiTyyppi mktt = new MonikielinenTekstiTyyppi();
        dto.setNimi(mktt);
        Teksti teksti = new Teksti();
        teksti.setKieliKoodi("sv");
        teksti.setValue("nimi");
        mktt.getTeksti().add(teksti);
        teksti = new Teksti();
        teksti.setKieliKoodi("fi");
        teksti.setValue("nimi2");

        // hmmm??
        Assert.assertEquals("nimi ( 1234567-1 ) Koulutustoimija",
                OrganisaatioDisplayHelper.getCaption(dto, new Locale("fi")));
        Assert.assertEquals("nimi ( 1234567-1 ) Koulutustoimija",
                OrganisaatioDisplayHelper.getCaption(dto, new Locale("sv")));
    }

    @Test
    public void testGetClosest() {
        OrganisaatioDTO dto = new OrganisaatioDTO();
        dto.setYtunnus("1234567-1");
        dto.getTyypit().add(OrganisaatioTyyppi.KOULUTUSTOIMIJA);
        MonikielinenTekstiTyyppi mktt = new MonikielinenTekstiTyyppi();
        dto.setNimi(mktt);
        Teksti teksti = new Teksti();
        teksti.setKieliKoodi("sv");
        teksti.setValue("nimi");
        mktt.getTeksti().add(teksti);
        teksti = new Teksti();
        teksti.setKieliKoodi("fi");
        teksti.setValue("nimi2");

        // hmmm??
        Assert.assertEquals("nimi", OrganisaatioDisplayHelper.getClosest(null, dto));
        Assert.assertEquals("nimi", OrganisaatioDisplayHelper.getClosest(new Locale("sv"), dto));
        Assert.assertEquals("nimi", OrganisaatioDisplayHelper.getClosest(new Locale("fi"), dto));
    }

    @Test
    public void testGetClosestBasic() {
        OrganisaatioPerustieto optt = new OrganisaatioPerustieto();
        optt.setNimi("sv", "nimi");
        optt.setNimi("fi", "nimi2");
        // NPE Assert.assertEquals("nimi",
        // OrganisaatioDisplayHelper.getClosestBasic(null, optt));
        Assert.assertEquals("nimi2", OrganisaatioDisplayHelper.getClosestBasic(new Locale("fi"), optt));
        Assert.assertEquals("nimi", OrganisaatioDisplayHelper.getClosestBasic(new Locale("sv"), optt));
    }

    @Test
    public void testGetOrganisaatioEmail() {
        OrganisaatioDTO dto = new OrganisaatioDTO();
        Assert.assertNull(null, OrganisaatioDisplayHelper.getOrganisaatioEmail(dto));
        dto.getYhteystiedot().add(new EmailDTO());
        dto.getYhteystiedot().add(new OsoiteDTO());
        Assert.assertNotNull(OrganisaatioDisplayHelper.getOrganisaatioEmail(dto));
    }

    @Test
    public void testGetOrganisaatioWww() {
        OrganisaatioDTO dto = new OrganisaatioDTO();
        Assert.assertNull(null, OrganisaatioDisplayHelper.getOrganisaatioWww(dto));
        dto.getYhteystiedot().add(new EmailDTO());
        dto.getYhteystiedot().add(new WwwDTO());
        Assert.assertNotNull(OrganisaatioDisplayHelper.getOrganisaatioWww(dto));
    }
}
