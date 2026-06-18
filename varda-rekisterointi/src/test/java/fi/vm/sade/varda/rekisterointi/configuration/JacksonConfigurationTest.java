package fi.vm.sade.varda.rekisterointi.configuration;

import fi.vm.sade.varda.rekisterointi.model.Kayttaja;
import fi.vm.sade.varda.rekisterointi.model.KielistettyNimi;
import fi.vm.sade.varda.rekisterointi.model.Organisaatio;
import fi.vm.sade.varda.rekisterointi.model.OrganisaatioDto;
import fi.vm.sade.varda.rekisterointi.model.Osoite;
import fi.vm.sade.varda.rekisterointi.model.Paatos;
import fi.vm.sade.varda.rekisterointi.model.PaatosDto;
import fi.vm.sade.varda.rekisterointi.model.Rekisterointi;
import fi.vm.sade.varda.rekisterointi.model.RekisterointiDto;
import fi.vm.sade.varda.rekisterointi.model.Yhteystiedot;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.context.annotation.Import;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@Import(JacksonConfiguration.class)
public class JacksonConfigurationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void serializesApiDatesAsIsoStrings() throws Exception {
        Rekisterointi rekisterointi = new Rekisterointi(
                1L,
                Organisaatio.of(
                        "0000001-9",
                        "oid",
                        LocalDate.of(2024, 1, 15),
                        KielistettyNimi.of("Varda-yritys", "fi", LocalDate.of(2024, 1, 15)),
                        "yritysmuoto_26",
                        Set.of("organisaatiotyyppi_07"),
                        "kunta_091",
                        "maatjavaltiot1_fin",
                        Set.of("oppilaitoksenopetuskieli_1#1"),
                        Yhteystiedot.of("101234567", "testi@testiyritys.fi", Osoite.TYHJA, Osoite.TYHJA),
                        false),
                "varda",
                "vardatoimintamuoto_tm01",
                Set.of("Helsinki"),
                Set.of("foo@foo.bar"),
                Kayttaja.of(1L, "John", "Smith", "john.smith@example.com", "fi", null),
                LocalDateTime.of(2024, 2, 3, 4, 5, 6),
                new Paatos(true, LocalDateTime.of(2024, 2, 4, 5, 6, 7), "paattaja-oid", "ok"),
                Rekisterointi.Tila.HYVAKSYTTY);

        String json = objectMapper.writeValueAsString(rekisterointi);

        assertThat(json)
                .contains("\"alkuPvm\":\"2024-01-15\"")
                .contains("\"vastaanotettu\":\"2024-02-03T04:05:06\"")
                .contains("\"paatetty\":\"2024-02-04T05:06:07\"")
                .doesNotContain("\"alkuPvm\":[")
                .doesNotContain("\"vastaanotettu\":[")
                .doesNotContain("\"paatetty\":[");
    }

    @Test
    public void deserializesRekisterointiDtoApiPayload() throws Exception {
        String json = """
                {
                  "organisaatio": {
                    "ytunnus": "0000001-9",
                    "oid": "oid",
                    "alkuPvm": "2024-01-15",
                    "ytjNimi": {
                      "nimi": "Varda-yritys",
                      "kieli": "fi",
                      "alkuPvm": "2024-01-15"
                    },
                    "yritysmuoto": "yritysmuoto_26",
                    "tyypit": ["organisaatiotyyppi_07"],
                    "kotipaikkaUri": "kunta_091",
                    "maaUri": "maatjavaltiot1_fin",
                    "kieletUris": ["oppilaitoksenopetuskieli_1#1"],
                    "yhteystiedot": {
                      "puhelinnumero": "101234567",
                      "sahkoposti": "testi@testiyritys.fi",
                      "postiosoite": {
                        "katuosoite": "",
                        "postinumeroUri": "",
                        "postitoimipaikka": ""
                      },
                      "kayntiosoite": {
                        "katuosoite": "",
                        "postinumeroUri": "",
                        "postitoimipaikka": ""
                      }
                    },
                    "uudelleenRekisterointi": false,
                    "tuntematon": "sallitaan"
                  },
                  "toimintamuoto": "vardatoimintamuoto_tm01",
                  "tyyppi": "varda",
                  "kunnat": ["Helsinki"],
                  "sahkopostit": ["foo@foo.bar"],
                  "kayttaja": {
                    "id": null,
                    "etunimi": "John",
                    "sukunimi": "Smith",
                    "sahkoposti": "john.smith@example.com",
                    "asiointikieli": "fi",
                    "saateteksti": null
                  },
                  "tuntematon": "sallitaan"
                }
                """;

        RekisterointiDto dto = objectMapper.readValue(json, RekisterointiDto.class);

        assertThat(dto.organisaatio.ytunnus).isEqualTo("0000001-9");
        assertThat(dto.organisaatio.alkuPvm).isEqualTo(LocalDate.of(2024, 1, 15));
        assertThat(dto.organisaatio.ytjNimi.alkuPvm).isEqualTo(LocalDate.of(2024, 1, 15));
        assertThat(dto.kayttaja.sahkoposti).isEqualTo("john.smith@example.com");
    }

    @Test
    public void deserializesOrganisaatioServiceEpochMillisLocalDateAndIsoLocalDate() throws Exception {
        OrganisaatioDto epochDate = objectMapper.readValue("{\"alkuPvm\":258760800000}", OrganisaatioDto.class);
        OrganisaatioDto isoDate = objectMapper.readValue("{\"alkuPvm\":\"1992-01-01\"}", OrganisaatioDto.class);

        assertThat(epochDate.alkuPvm).isEqualTo(LocalDate.of(1978, 3, 15));
        assertThat(isoDate.alkuPvm).isEqualTo(LocalDate.of(1992, 1, 1));
    }

    @Test
    public void keepsOldPrimitiveNullHandling() throws Exception {
        PaatosDto dto = objectMapper.readValue("{\"rekisterointi\":1,\"hyvaksytty\":null}", PaatosDto.class);

        assertThat(objectMapper.isEnabled(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)).isFalse();
        assertThat(dto.hyvaksytty).isFalse();
    }

}
