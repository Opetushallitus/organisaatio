package fi.vm.sade.organisaatio.revised.ui.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Ignore;
import org.junit.Test;

import fi.vm.sade.organisaatio.api.model.types.OrganisaatioDTO;
import fi.vm.sade.organisaatio.api.model.types.YhteystietoDTO;
import fi.vm.sade.rajapinnat.ytj.api.YTJDTO;
import fi.vm.sade.rajapinnat.ytj.api.YTJOsoiteDTO;

public class YtjToOrganisaatioMapperTest {

    @Ignore
    @Test
    public void test() {
        final Date now = new Date();
        final OrganisaatioDTO org = new OrganisaatioDTO();
        YTJDTO ytj = new YTJDTO();
        ytj.setNimi("nimiYTJ");
        ytj.setKotiPaikka("kotipaikkaYTJ");
        ytj.setYtunnus("ytunnusYTJ");
        ytj.setYritysmuoto("yritysmuotoYTJ");
        ytj.setPuhelin("puhelinYTJ");
        ytj.setWww("wwwYTJ");
        YTJOsoiteDTO kayntiOsoite = new YTJOsoiteDTO();
        kayntiOsoite.setKatu("käyntiosoiteYTJ");
        kayntiOsoite.setPostinumero("010101");
        ytj.setKayntiOsoite(kayntiOsoite);
        YTJOsoiteDTO postiOsoite = new YTJOsoiteDTO();
        postiOsoite.setKatu("postiosoiteYTJ");
        postiOsoite.setPostinumero("010101");
        ytj.setPostiOsoite(postiOsoite);
        ytj.setSahkoposti("sahkopostiYTJ");
        final OrganisaatioDTO result = YtjToOrganisaatioMapper.mapYtjToOrganisaatio(ytj, org);
        assertEquals("nimiYTJ", result.getNimi().getTeksti().get(0).getValue());
        assertEquals("fi", result.getNimi().getTeksti().get(0).getKieliKoodi());
        assertEquals("kotipaikkaYTJ", result.getKotipaikka());
        assertEquals("ytunnusYTJ", result.getYtunnus());
        assertEquals("yritysmuotoYTJ", result.getYritysmuoto());
        assertNotNull(result.getYtjPaivitysPvm());
        assertTrue(!result.getYtjPaivitysPvm().before(now));
       assertEquals(5, result.getYhteystiedot().size());
    }

}
