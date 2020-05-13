package fi.vm.sade.varda.rekisterointi.config;

import fi.vm.sade.varda.rekisterointi.client.KoodistoClient;
import fi.vm.sade.varda.rekisterointi.model.KoodistoType;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Collections;

@Configuration
@Profile("test | integration-test")
public class TestConfiguration {

    @Bean
    public KoodistoClient koodistoClient() {
        // jyrätään koodistoclient mockilla, jotta spring context ei hajoa sen initialisointiin
        KoodistoClient client = Mockito.mock(KoodistoClient.class);
        Mockito.when(client.listKoodit(Mockito.any(KoodistoType.class))).thenReturn(Collections.emptyList());
        return client;
    }

}
