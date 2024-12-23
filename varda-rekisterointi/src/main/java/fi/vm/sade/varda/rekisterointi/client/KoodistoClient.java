package fi.vm.sade.varda.rekisterointi.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.vm.sade.javautils.httpclient.OphHttpClient;
import fi.vm.sade.properties.OphProperties;
import fi.vm.sade.varda.rekisterointi.model.BaseDto;
import fi.vm.sade.varda.rekisterointi.model.Koodi;
import fi.vm.sade.varda.rekisterointi.model.KoodistoType;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * Client koodistopalvelun käyttämiseen.
 */
@Component
@Profile({"!test & !integration-test"})
public class KoodistoClient {

    private final OphHttpClient httpClient;
    private final OphProperties properties;
    private final ObjectMapper objectMapper;

    /**
     * Alustaa clientin annetulla HTTP-clientillä, konfiguraatiolla ja <code>ObjectMapper</code>illa.
     *
     * @param httpClient    HTTP-client
     * @param properties    konfiguraatio
     * @param objectMapper  Jackson object mapper
     */
    public KoodistoClient(OphHttpClient httpClient, OphProperties properties, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    /**
     * Listaa annetun koodiston koodit.
     *
     * @param koodisto  haluttu koodisto
     *
     * @return koodiston koodit.
     */
    public Collection<Koodi> listKoodit(KoodistoType koodisto) {
        return listKoodit(koodisto, Optional.empty(), Optional.empty());
    }

    /**
     * Lista annetun koodiston koodit.
     *
     * @param koodisto  haluttu koodisto
     * @param versio    mikäli annettu, haetaan vain halutun version koodit
     * @param onlyValid sisällytetäänkö vain voimassaolevat koodit?
     *
     * @return koodiston koodit.
     */
    public Collection<Koodi> listKoodit(KoodistoType koodisto, Optional<Integer> versio, Optional<Boolean> onlyValid) {
        Map<String, Object> parameters = new LinkedHashMap<>();
        versio.ifPresent(value -> parameters.put("koodistoVersio", value));
        onlyValid.ifPresent(value -> parameters.put("onlyValidKoodis", value));
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
