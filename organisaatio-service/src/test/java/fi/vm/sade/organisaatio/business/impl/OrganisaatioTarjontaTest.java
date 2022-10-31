package fi.vm.sade.organisaatio.business.impl;

import com.google.gson.JsonParser;
import fi.vm.sade.properties.OphProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class OrganisaatioTarjontaTest {

    @InjectMocks
    private OrganisaatioTarjonta organisaatioTarjonta;

    @Mock
    private OrganisaatioRestToStream restToStream;

    @Spy
    private OphProperties properties = new OphProperties("/organisaatio-service-oph.properties");

    @BeforeEach
    public void setup() {
        properties.addFiles("/application.properties");
    }

    @Test
    public void alkavaKoulutusSeuraavanaPaivana() {
        when(restToStream.getInputStreamFromUri(anyString())).thenReturn(new JsonParser()
                .parse("{\"status\": \"OK\", \"result\": {\"tulokset\": [{\"tulokset\": [{\"tila\": \"JULKAISTU\", \"vuosi\": 2018}]}]}}"));
        String oid = "oid";
        Date lakkautusPvm = new Date(1546120800000l); // Sun Dec 30 00:00:00 EET 2018

        assertThat(organisaatioTarjonta.alkaviaKoulutuksia(oid, lakkautusPvm)).isTrue();
    }

    @Test
    public void alkavaKoulutusSamanaPaivana() {
        when(restToStream.getInputStreamFromUri(anyString())).thenReturn(new JsonParser()
                .parse("{\"status\": \"OK\", \"result\": {\"tulokset\": [{\"tulokset\": [{\"tila\": \"JULKAISTU\", \"vuosi\": 2018}]}]}}"));
        String oid = "oid";
        Date lakkautusPvm = new Date(1546207200000l); // Mon Dec 31 00:00:00 EET 2018

        assertThat(organisaatioTarjonta.alkaviaKoulutuksia(oid, lakkautusPvm)).isFalse();
    }

    @Test
    public void alkavaKoulutusEdellisenaPaivana() {
        when(restToStream.getInputStreamFromUri(anyString())).thenReturn(new JsonParser()
                .parse("{\"status\": \"OK\", \"result\": {\"tulokset\": [{\"tulokset\": [{\"tila\": \"JULKAISTU\", \"vuosi\": 2018}]}]}}"));
        String oid = "oid";
        Date lakkautusPvm = new Date(1546293600000l); // Tue Jan 01 00:00:00 EET 2019

        assertThat(organisaatioTarjonta.alkaviaKoulutuksia(oid, lakkautusPvm)).isFalse();
    }

}
