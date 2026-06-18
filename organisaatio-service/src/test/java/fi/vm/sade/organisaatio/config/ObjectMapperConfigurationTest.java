package fi.vm.sade.organisaatio.config;

import fi.vm.sade.organisaatio.dto.v4.OrganisaatioRDTOV4;
import fi.ytj.ArrayOfYTieto;
import fi.ytj.YTunnusDTO;
import fi.ytj.YritysTunnusHistoriaDTO;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class ObjectMapperConfigurationTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void javaSqlTimestampDeserializer() throws IOException {
        String json = "1535523954000";

        Timestamp timestamp = objectMapper.readValue(json, Timestamp.class);

        assertThat(timestamp).hasSameTimeAs("2018-08-29T05:25:54-01:00");
    }

    @Test
    public void tarkastusPvmSerializerUsesLegacyTimestampFormat() throws IOException {
        OffsetDateTime dateTime = OffsetDateTime.of(2018, 8, 29, 05, 25, 54, 0, ZoneOffset.ofHours(-1));
        Timestamp timestamp = new Timestamp(dateTime.toInstant().toEpochMilli());
        OrganisaatioRDTOV4 organisaatio = new OrganisaatioRDTOV4();
        organisaatio.setTarkastusPvm(timestamp);

        String json = objectMapper.writeValueAsString(organisaatio);

        assertThat(json).contains("\"tarkastusPvm\":1535523954000");
    }

    @Test
    public void organisaatioRdtoUsesLegacyYtjPropertyNames() throws IOException {
        OrganisaatioRDTOV4 organisaatio = new OrganisaatioRDTOV4();
        organisaatio.setYTunnus("1234567-1");
        organisaatio.setYTJKieli("kieli_fi#1");
        organisaatio.setYTJPaivitysPvm(new Date(1535523954000L));

        String json = objectMapper.writeValueAsString(organisaatio);

        assertThat(json)
                .contains("\"ytunnus\":\"1234567-1\"")
                .contains("\"ytjkieli\":\"kieli_fi#1\"")
                .contains("\"ytjpaivitysPvm\":\"2018-08-29\"")
                .doesNotContain("\"YTunnus\"")
                .doesNotContain("\"YTJKieli\"")
                .doesNotContain("\"YTJPaivitysPvm\"");

        OrganisaatioRDTOV4 deserialized = objectMapper.readValue(
                "{\"ytunnus\":\"1234567-1\",\"ytjkieli\":\"kieli_fi#1\",\"ytjpaivitysPvm\":\"2018-08-29\"}",
                OrganisaatioRDTOV4.class);

        assertThat(deserialized.getYTunnus()).isEqualTo("1234567-1");
        assertThat(deserialized.getYTJKieli()).isEqualTo("kieli_fi#1");
        assertThat(deserialized.getYTJPaivitysPvm()).isNotNull();
    }

    @Test
    public void generatedYtjDtosUseLegacyYtjPropertyNames() throws IOException {
        YTunnusDTO ytunnus = new YTunnusDTO();
        ytunnus.setYTunnus("1234567-1");
        YritysTunnusHistoriaDTO historia = new YritysTunnusHistoriaDTO();
        historia.setYTunnusVanha("1234567-1");
        historia.setYTunnusUusi("7654321-1");
        ArrayOfYTieto yTieto = new ArrayOfYTieto();

        String ytunnusJson = objectMapper.writeValueAsString(ytunnus);
        String historiaJson = objectMapper.writeValueAsString(historia);
        String yTietoJson = objectMapper.writeValueAsString(yTieto);

        assertThat(ytunnusJson)
                .contains("\"ytunnus\":\"1234567-1\"")
                .doesNotContain("\"YTunnus\"");
        assertThat(historiaJson)
                .contains("\"ytunnusVanha\":\"1234567-1\"")
                .contains("\"ytunnusUusi\":\"7654321-1\"")
                .doesNotContain("\"YTunnusVanha\"")
                .doesNotContain("\"YTunnusUusi\"");
        assertThat(yTietoJson)
                .contains("\"ytieto\":[]")
                .doesNotContain("\"YTieto\"");
    }

}
