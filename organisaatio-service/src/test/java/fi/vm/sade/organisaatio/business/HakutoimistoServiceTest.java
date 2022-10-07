package fi.vm.sade.organisaatio.business;

import com.google.common.collect.ImmutableMap;
import fi.vm.sade.organisaatio.resource.dto.HakutoimistoDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Sql({"/data/truncate_tables.sql"})
@Sql({"/data/basic_organisaatio_data.sql"})
@SpringBootTest
class HakutoimistoServiceTest {

    @Autowired
    HakutoimistoService hakutoimistoService;

    @Test
    @Transactional(readOnly = true)
    void testFetchingHakutoimisto() {
        HakutoimistoDTO hakutoimisto = hakutoimistoService.hakutoimisto("1.2.2004.4");
        assertEquals("Hakutoimiston nimi FI", hakutoimisto.nimi.get("kieli_fi#1"));
        HakutoimistoDTO expected = new HakutoimistoDTO(
                ImmutableMap.of("kieli_fi#1", "Hakutoimiston nimi FI", "kieli_en#1", "Hakutoimiston nimi EN"),
                ImmutableMap.of(
                        "kieli_fi#1", new HakutoimistoDTO.HakutoimistonYhteystiedotDTO(hakutoimistonOsoite("1.2.2004.4", "fi"), hakutoimistonOsoite("1.2.2004.5", "fi"), "http://www.foo.fi", "foo@bar.com", "123456789"),
                        "kieli_sv#1", new HakutoimistoDTO.HakutoimistonYhteystiedotDTO(hakutoimistonOsoite("1.2.2004.6", "sv"), null, null, null, null),
                        "kieli_en#1", new HakutoimistoDTO.HakutoimistonYhteystiedotDTO(hakutoimistonOsoite("1.2.2004.7", "en"), hakutoimistonOsoite("1.2.2004.8", "en"), "http://www.foo.fi/en", null, null)));

        assertEquals(expected, hakutoimisto);
    }

    @Test
    @Transactional(readOnly = true)
    void testMixedOsoitetyyppi() {
        HakutoimistoDTO hakutoimisto = hakutoimistoService.hakutoimisto("1.2.8000.1");
        assertEquals("Hakutoimiston nimi EN", hakutoimisto.nimi.get("kieli_en#1"));
        HakutoimistoDTO expected = new HakutoimistoDTO(
                ImmutableMap.of("kieli_fi#1", "Hakutoimiston nimi FI", "kieli_en#1", "Hakutoimiston nimi EN"),
                ImmutableMap.of(
                        "kieli_en#1", new HakutoimistoDTO.HakutoimistonYhteystiedotDTO(hakutoimistonOsoite("1.2.2004.9", "en"), hakutoimistonOsoite("1.2.2004.10", "en"), null, null, null)));

        assertEquals(expected, hakutoimisto);
    }

    private HakutoimistoDTO.OsoiteDTO hakutoimistonOsoite(String yhteystietoOid, String lang) {
        if ("en".equals(lang)) {
            return new HakutoimistoDTO.OsoiteDTO(yhteystietoOid, "Hassuttimenkatu 2, 10000 Juupajoki, Finland", null, null);
        }
        return new HakutoimistoDTO.OsoiteDTO(yhteystietoOid, "fi".equals(lang) ? "Hassuttimenkatu 2" : "Hassutingatan 2", "posti_10000", "Juupajoki");
    }
}