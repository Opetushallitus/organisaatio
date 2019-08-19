package fi.vm.sade.varda.rekisterointi.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.vm.sade.javautils.httpclient.OphHttpClient;
import fi.vm.sade.properties.OphProperties;
import fi.vm.sade.varda.rekisterointi.model.BaseDto;
import fi.vm.sade.varda.rekisterointi.model.Koodi;
import fi.vm.sade.varda.rekisterointi.model.KoodistoType;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Component
public class KoodistoClient {

    private final OphHttpClient httpClient;
    private final OphProperties properties;
    private final ObjectMapper objectMapper;

    public KoodistoClient(OphHttpClient httpClient, OphProperties properties, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public Collection<Koodi> listKoodit(KoodistoType koodisto) {
        Map<String, Object> parameters = new LinkedHashMap<>();
        koodisto.versio.ifPresent(versio -> parameters.put("koodistoVersio", versio));
        koodisto.onlyValid.ifPresent(onlyValid -> parameters.put("onlyValidKoodis", onlyValid));
        String url = properties.url("koodisto-service.koodi", koodisto.uri, parameters);
        return listKoodit(url);
    }

    private Collection<Koodi> listKoodit(String url) {
        KoodiDto[] koodit = httpClient.get(url)
                .execute(response -> objectMapper.readValue(response.asInputStream(), KoodiDto[].class));
        return Arrays.stream(koodit).map(KoodistoClient::dtoToKoodi).collect(toList());
    }

    private static Koodi dtoToKoodi(KoodiDto dto) {
        Koodi koodi = new Koodi();
        koodi.uri = dto.koodiUri;
        koodi.arvo = dto.koodiArvo;
        koodi.nimi = metadataTo(dto.metadata, metadata -> metadata.nimi);
        return koodi;
    }

    private static Map<String, String> metadataTo(List<KoodiMetadataDto> metadataList, Function<KoodiMetadataDto, String> valueProvider) {
        return metadataList.stream()
                .filter(metadata -> metadata != null && isNotEmpty(metadata.kieli) && isNotEmpty(valueProvider.apply(metadata)))
                .collect(toMap(metadata -> metadata.kieli.toLowerCase(), valueProvider));
    }

    private static boolean isNotEmpty(String str) {
        return str != null && !str.isEmpty();
    }

    private static class KoodiDto extends BaseDto {
        public String koodiUri;
        public String koodiArvo;
        public List<KoodiMetadataDto> metadata;
    }

    private static class KoodiMetadataDto extends BaseDto {
        public String kieli;
        public String nimi;
    }

}
