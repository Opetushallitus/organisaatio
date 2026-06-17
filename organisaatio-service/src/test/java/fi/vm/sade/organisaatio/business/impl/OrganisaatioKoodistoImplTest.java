package fi.vm.sade.organisaatio.business.impl;

import tools.jackson.databind.ObjectMapper;
import fi.vm.sade.organisaatio.KoulutustoimijaBuilder;
import fi.vm.sade.organisaatio.OppilaitosBuilder;
import fi.vm.sade.organisaatio.client.OrganisaatioKoodistoClient;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.repository.OrganisaatioRepository;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;

import static fi.vm.sade.organisaatio.business.OrganisaatioKoodisto.KoodistoUri.KIELI;
import static fi.vm.sade.organisaatio.ResourceUtils.classPathResourceAsString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

@SpringBootTest
public class OrganisaatioKoodistoImplTest {
    private OrganisaatioKoodistoImpl impl;

    private OrganisaatioKoodistoClient clientMock;
    private OrganisaatioRepository daoMock;

    @Autowired
    private ObjectMapper mapper;

    @BeforeEach
    public void setup() {
        clientMock = mock(OrganisaatioKoodistoClient.class);
        daoMock = mock(OrganisaatioRepository.class);
        impl = new OrganisaatioKoodistoImpl(clientMock, daoMock, mapper);
        ReflectionTestUtils.setField(impl, "urlVirkailija", "https://virkailija.testiopintopolku.fi");
    }

    @Test
    public void paivitaKoodistoKoulutustoimija() throws JSONException {
        Organisaatio organisaatio = new KoulutustoimijaBuilder("oid")
                .nimi("fi", "Aalto-korkeakoulusäätiö sr")
                .nimi("sv", "Aalto-korkeakoulusäätiö sr")
                .nimi("en", "Aalto-korkeakoulusäätiö sr")
                .opetuskieli("oppilaitoksenopetuskieli_1#1")
                .alkuPvm(LocalDate.of(2008, 10, 22))
                .kotipaikka("kunta_091")
                .maa("maatjavaltiot1_fin")
                .ytunnus("2228357-4")
                .build();
        when(clientMock.get(anyString())).thenReturn(classPathResourceAsString("json/paivita-koodisto-koulutustoimija/get-koodisto-koulutustoimija.json"));

        impl.paivitaKoodisto(organisaatio);

        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(clientMock).put(stringArgumentCaptor.capture());
        assertEquals(classPathResourceAsString("json/paivita-koodisto-koulutustoimija/put-koodisto-koulutustoimija.json"), stringArgumentCaptor.getValue(), true);
    }

    @Test
    public void paivitaKoodistoOppilaitosIncludesCodeElementsPassive() throws JSONException {
        Organisaatio organisaatio = new OppilaitosBuilder("oid")
                .nimi("fi", "Lapuan ammatti-instituutti")
                .nimi("sv", "Lapuan ammatti-instituutti")
                .nimi("en", "Lapuan ammatti-instituutti")
                .alkuPvm(LocalDate.of(2000, 1, 1))
                .lakkautusPvm(LocalDate.of(2002, 7, 31))
                .kotipaikka("kunta_408")
                .oppilaitoskoodi("02428")
                .oppilaitostyyppi("oppilaitostyyppi_21#1")
                .build();
        when(clientMock.get(anyString())).thenReturn(classPathResourceAsString("json/paivita-koodisto-oppilaitosnumero-passive/get-koodisto-oppilaitosnumero-passive.json"));

        impl.paivitaKoodisto(organisaatio);

        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(clientMock).put(stringArgumentCaptor.capture());
        assertEquals(classPathResourceAsString("json/paivita-koodisto-oppilaitosnumero-passive/put-koodisto-oppilaitosnumero-passive.json"), stringArgumentCaptor.getValue(), true);
    }

    @Test
    public void haeKooditAddsOptionalQueryParametersToUrl() {
        when(clientMock.get(anyString())).thenReturn("[]");

        impl.haeKoodit(KIELI, Optional.of(1), Optional.of(false));

        verify(clientMock).get("https://virkailija.testiopintopolku.fi/koodisto-service/rest/json/kieli/koodi?koodistoVersio=1&onlyValidKoodis=false");
    }

}
