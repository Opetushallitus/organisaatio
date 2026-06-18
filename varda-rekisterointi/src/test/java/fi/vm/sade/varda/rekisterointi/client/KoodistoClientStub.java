package fi.vm.sade.varda.rekisterointi.client;

import tools.jackson.databind.ObjectMapper;
import fi.vm.sade.javautils.httpclient.OphHttpClient;
import fi.vm.sade.varda.rekisterointi.model.Koodi;
import fi.vm.sade.varda.rekisterointi.model.KoodistoType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

@Primary
@Component
public class KoodistoClientStub extends KoodistoClient {

    public KoodistoClientStub(OphHttpClient httpClient,
                              @Value("${varda-rekisterointi.url-virkailija}") String virkailijaUrl,
                              ObjectMapper objectMapper) {
        super(httpClient, virkailijaUrl, objectMapper);
    }

    @Override
    public Collection<Koodi> listKoodit(KoodistoType koodisto) {
        return Collections.emptyList();
    }

    @Override
    public Collection<Koodi> listKoodit(KoodistoType koodisto, Optional<Integer> versio, Optional<Boolean> onlyValid) {
        return Collections.emptyList();
    }
}
