package fi.vm.sade.varda.rekisterointi.client;

import tools.jackson.databind.ObjectMapper;
import fi.vm.sade.varda.rekisterointi.model.BaseDto;
import fi.vm.sade.varda.rekisterointi.model.Koodi;
import fi.vm.sade.varda.rekisterointi.model.KoodistoType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.function.Function;

import static fi.vm.sade.varda.rekisterointi.util.Constants.CALLER_ID;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * Client koodistopalvelun käyttämiseen.
 */
@Component
@Profile({"!test & !integration-test"})
public class KoodistoClient {

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(35);

    private final HttpClient httpClient;
    private final String virkailijaUrl;
    private final ObjectMapper objectMapper;

    /**
     * Alustaa clientin annetulla HTTP-clientillä, konfiguraatiolla ja <code>ObjectMapper</code>illa.
     *
     * @param httpClient    HTTP-client
     * @param virkailijaUrl virkailijan palveluosoite
     * @param objectMapper  Jackson object mapper
     */
    public KoodistoClient(HttpClient httpClient,
                          @Value("${varda-rekisterointi.url-virkailija}") String virkailijaUrl,
                          ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.virkailijaUrl = virkailijaUrl;
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
        String url = virkailijaUrl + "/koodisto-service/rest/json/" + koodisto.uri + "/koodi";
        List<String> parameters = new ArrayList<>();
        versio.ifPresent(value -> parameters.add("koodistoVersio=" + value));
        onlyValid.ifPresent(value -> parameters.add("onlyValidKoodis=" + value));
        if (!parameters.isEmpty()) {
            url += "?" + String.join("&", parameters);
        }
        return listKoodit(url);
    }

    private Collection<Koodi> listKoodit(String url) {
        KoodiDto[] koodit = objectMapper.readValue(get(url), KoodiDto[].class);
        return Arrays.stream(koodit).map(KoodistoClient::dtoToKoodi).collect(toList());
    }

    private String get(String url) {
        try {
            var request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(REQUEST_TIMEOUT)
                    .header("Caller-Id", CALLER_ID)
                    .GET()
                    .build();
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new RuntimeException(String.format("Url %s returned status %d", url, response.statusCode()));
            }
            return response.body();
        } catch (IOException e) {
            throw new RuntimeException(String.format("Error while executing GET %s", url), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(String.format("Interrupted while executing GET %s", url), e);
        }
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
