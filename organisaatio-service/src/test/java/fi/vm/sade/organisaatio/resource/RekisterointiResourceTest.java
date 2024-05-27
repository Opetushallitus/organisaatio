package fi.vm.sade.organisaatio.resource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import fi.vm.sade.organisaatio.client.KayttooikeusClient;
import fi.vm.sade.organisaatio.model.Kayttaja;
import fi.vm.sade.organisaatio.model.VardaRekisterointi;
import fi.vm.sade.organisaatio.ytj.api.YTJDTO;
import fi.vm.sade.organisaatio.ytj.api.YTJKieli;
import fi.vm.sade.organisaatio.ytj.api.YTJOsoiteDTO;
import fi.vm.sade.organisaatio.ytj.api.YTJService;
import fi.ytj.YTunnusDTO;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@Sql("/data/truncate_tables.sql")
@Sql("/data/basic_organisaatio_data.sql")
public class RekisterointiResourceTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private KayttooikeusClient kayttooikeusClient;
    @MockBean
    private YTJService ytjService;

    Kayttaja kayttaja = new Kayttaja("etu", "suku", "sposti@fi.fi", "FI");

    @Test
    @WithMockUser(value = "1.2.3.4.5", roles = {"APP_JOTAIN_MUUTA"})
    public void requiresVardaRekisterointiRole() throws Exception {
        VardaRekisterointi request = new VardaRekisterointi("2255802-1", kayttaja);
        mockMvc.perform(post("/api/rekisterointi/varda")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    private YTJDTO getYtjOrganisation(String ytunnus) {
        YTJDTO ytjOrg = new YTJDTO();
        ytjOrg.setNimi("firma");
        ytjOrg.setSvNimi("firman");
        ytjOrg.setYtunnus(ytunnus);
        ytjOrg.setYrityksenKieli("fi");
        ytjOrg.setAloitusPvm("01.01.2000");
        ytjOrg.setKotiPaikkaKoodi("248");
        ytjOrg.setYritysmuoto("66");
        ytjOrg.setPuhelin("12345");
        ytjOrg.setWww("www.fi");
        ytjOrg.setSahkoposti("sposti@www.fi");

        YTunnusDTO y = new YTunnusDTO();
        y.setYTunnus(ytunnus);
        y.setAlkupvm("01.01.2005");
        y.setLoppupvm(null);
        ytjOrg.setYritysTunnus(y);

        YTJOsoiteDTO postiosoite = new YTJOsoiteDTO();
        postiosoite.setKatu("postikatu 3");
        postiosoite.setPostinumero("00100");
        postiosoite.setToimipaikka("HELSINKI");
        ytjOrg.setPostiOsoite(postiosoite);

        YTJOsoiteDTO kayntiosoite = new YTJOsoiteDTO();
        kayntiosoite.setKatu("kayntikatu 1");
        postiosoite.setPostinumero("00200");
        postiosoite.setToimipaikka("HELSINKI");
        ytjOrg.setPostiOsoite(kayntiosoite);

        return ytjOrg;
    }

    @Test
    @WithMockUser(value = "1.2.3.4.5", roles = {"APP_VARDA_REKISTEROINTI"})
    public void vardaRekisterointiCreatesNewOrganisation() throws Exception {
        when(kayttooikeusClient.kutsuKayttaja(any(), any(), eq(123l), eq("testi@testi.fi")))
            .thenReturn(12345l);

        String ytunnus = "1234567-1";
        YTJDTO ytjOrg = getYtjOrganisation(ytunnus);
        when(ytjService.findByYTunnus(ytunnus, YTJKieli.FI))
            .thenReturn(ytjOrg);

        VardaRekisterointi request = new VardaRekisterointi(ytunnus, kayttaja);
        mockMvc.perform(post("/api/rekisterointi/varda")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                    {
                        "maskingActive": false,
                        "version": 0,
                        "kayttoryhmat": [],
                        "parentOidPath": "|1.2.246.562.24.00000000001|",
                        "kayntiosoite": {},
                        "yhteystietoArvos": [],
                        "toimipistekoodi": "",
                        "muutKotipaikatUris": [],
                        "kotipaikkaUri": "kunta_248",
                        "tyypit": ["organisaatiotyyppi_07"],
                        "nimi": { "fi": "firma", "sv": "firman" },
                        "alkuPvm": "2005-01-01",
                        "status": "AKTIIVINEN",
                        "ryhmatyypit": [],
                        "vuosiluokat": [],
                        "kuvaus2": {},
                        "piilotettu": false,
                        "maaUri": "maatjavaltiot1_fin",
                        "parentOid": "1.2.246.562.24.00000000001",
                        "kieletUris": ["oppilaitoksenopetuskieli_1#1"],
                        "ytunnus": "1234567-1",
                        "nimet": [
                          {
                            "nimi": { "fi": "firma", "sv": "firman" },
                            "alkuPvm": "2000-01-01",
                            "version": 0
                          }
                        ],
                        "yritysmuoto": "66",
                        "ytjkieli": "kieli_fi#1",
                        "lyhytNimi": { "fi": "firma", "sv": "firman" },
                        "lisatiedot": [],
                        "muutOppilaitosTyyppiUris": []
                      }"""));
    }

    @Test
    @WithMockUser(value = "1.2.3.4.5", roles = {"APP_VARDA_REKISTEROINTI"})
    public void vardaRekisterointiUpdatesExistingOrganisation() throws Exception {
        when(kayttooikeusClient.kutsuKayttaja(any(), eq("1.2.2004.1"), eq(123l), eq("testi@testi.fi")))
            .thenReturn(12345l);

        VardaRekisterointi request = new VardaRekisterointi("2255802-1", kayttaja);
        mockMvc.perform(post("/api/rekisterointi/varda")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tyypit.[1]").value("organisaatiotyyppi_07"));
    }
}
