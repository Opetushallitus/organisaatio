package fi.vm.sade.varda.rekisterointi.service;

import com.github.kagkarlsson.scheduler.SchedulerClient;
import com.github.kagkarlsson.scheduler.task.Task;
import fi.vm.sade.varda.rekisterointi.RequestContext;
import fi.vm.sade.varda.rekisterointi.client.OrganisaatioClient;
import fi.vm.sade.varda.rekisterointi.util.Constants;
import fi.vm.sade.varda.rekisterointi.util.RequestContextImpl;
import fi.vm.sade.varda.rekisterointi.exception.InvalidInputException;
import fi.vm.sade.varda.rekisterointi.exception.UnauthorizedException;
import fi.vm.sade.varda.rekisterointi.model.*;
import fi.vm.sade.varda.rekisterointi.repository.RekisterointiRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RekisterointiServiceTest {

        private static final Long SAVED_REKISTEROINTI_ID = 1L;
        private static final Long INVALID_REKISTEROINTI_ID = 2L;
        private static final String PAATTAJA_OID = "1.234.56789";
        private static final Rekisterointi SAVED_REKISTEROINTI = getRekisterointi();

        @MockitoBean
        private RekisterointiRepository rekisterointiRepository;

        @MockitoBean
        private SchedulerClient schedulerClient;

        @MockitoBean(name = "rekisterointiEmailTask")
        Task<Long> rekisterointiEmailTask;

        @MockitoBean(name = "paatosEmailTask")
        Task<Long> paatosEmailTask;

        @MockitoBean(name = "luoTaiPaivitaOrganisaatioTask")
        Task<Long> luoTaiPaivitaOrganisaatioTask;

        @Autowired
        private RekisterointiService rekisterointiService;

        @MockitoBean
        private OrganisaatioClient organisaatioClient;

        private List<String> OPH_AUTHORITIES = List.of("ORGANISAATIOIDEN_REKISTEROITYMINEN_OPH_" + OrganisaatioService.OPH_OID);
        private List<String> VARDA_AUTHORITIES = List.of("ORGANISAATIOIDEN_REKISTEROITYMINEN_VARDA_1.2.3.4.5");
        private List<String> JOTPA_AUTHORITIES = List.of("ORGANISAATIOIDEN_REKISTEROITYMINEN_JOTPA_1.2.3.4.5");

        private PaatosDto paatos = new PaatosDto(
                        SAVED_REKISTEROINTI_ID,
                        false,
                        "Rekisteröinti tehty 110% väärin.");

        private Authentication getAuthentication(List<String> roles) {
                return new Authentication() {
                        @Override
                        public String getName() {
                                return PAATTAJA_OID;
                        }

                        @Override
                        public Collection<? extends GrantedAuthority> getAuthorities() {
                                return roles
                                        .stream()
                                        .map(r -> (GrantedAuthority) () -> String.format("ROLE_" + Constants.VARDA_ROLE + "_%s", r))
                                        .toList();
                        }

                        @Override
                        public Object getCredentials() {
                                return null;
                        }

                        @Override
                        public Object getDetails() {
                                return null;
                        }

                        @Override
                        public Object getPrincipal() {
                                return null;
                        }

                        @Override
                        public boolean isAuthenticated() {
                                return true;
                        }

                        @Override
                        public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
                        }
                };
        }

        @Before
        public void mockRepositoryCalls() {
                when(rekisterointiRepository.save(any(Rekisterointi.class)))
                                .thenReturn(SAVED_REKISTEROINTI.withId(SAVED_REKISTEROINTI_ID));
                when(rekisterointiRepository.findById(SAVED_REKISTEROINTI_ID))
                                .thenReturn(Optional.of(SAVED_REKISTEROINTI));
                when(rekisterointiRepository.findById(INVALID_REKISTEROINTI_ID))
                                .thenReturn(Optional.empty());
        }

        @Test
        public void createSavesRekisterointi() {
                Long id = rekisterointiService.create(SAVED_REKISTEROINTI, requestContext("user1"));
                assertEquals(SAVED_REKISTEROINTI_ID, id);
                assertFalse(SAVED_REKISTEROINTI.organisaatio.uudelleenRekisterointi);
                verify(rekisterointiRepository).save(SAVED_REKISTEROINTI);
        }

        @Test
        public void createSavesUudelleenRekisterointi() {
                when(rekisterointiRepository.findByYtunnus(SAVED_REKISTEROINTI.organisaatio.ytunnus))
                                .thenReturn(List.of(SAVED_REKISTEROINTI));

                Rekisterointi rekisterointi = getRekisterointi();
                Long id = rekisterointiService.create(rekisterointi, requestContext("user1"));
                assertEquals(SAVED_REKISTEROINTI_ID, id);
                assertTrue(rekisterointi.organisaatio.uudelleenRekisterointi);
                verify(rekisterointiRepository).save(rekisterointi);
        }

        @Test(expected = InvalidInputException.class)
        public void resolveThrowsOnInvalidRekisterointiId() {
                PaatosDto paatos = new PaatosDto(
                                INVALID_REKISTEROINTI_ID,
                                true,
                                "Miksipä ei?");
                rekisterointiService.resolve(getAuthentication(OPH_AUTHORITIES), paatos, requestContext(null));
        }

        @Test(expected = IllegalStateException.class)
        public void resolveThrowsOnInvalidRekisterointiTila() {
                PaatosDto paatos = new PaatosDto(123L, false, "Juuh elikkäs");
                Rekisterointi hylatty = TestiRekisterointi.validiVardaRekisterointi()
                                .withId(paatos.rekisterointi)
                                .withPaatos(new Paatos(paatos.hyvaksytty, LocalDateTime.now(), PAATTAJA_OID,
                                                paatos.perustelu));
                when(rekisterointiRepository.findById(hylatty.id)).thenReturn(Optional.of(hylatty));
                rekisterointiService.resolve(getAuthentication(OPH_AUTHORITIES), paatos, requestContext(null));
        }

        @Test(expected = UnauthorizedException.class)
        public void resolveThrowsUnauthorizedForJotpaAuthoritiesOnVardaRekisterointi() {
                rekisterointiService.resolve(getAuthentication(JOTPA_AUTHORITIES), paatos, requestContext(null));
        }

        @Test(expected = UnauthorizedException.class)
        public void resolveThrowsUnauthorizedIfVardaVirkailijaHasWrongKunta() {
                OrganisaatioDto kunta = new OrganisaatioDto();
                kunta.kotipaikkaUri = "kunta_200";
                when(organisaatioClient.getKuntaByOid(eq("1.2.3.4.5"))).thenReturn(Optional.of(kunta));
                rekisterointiService.resolve(getAuthentication(VARDA_AUTHORITIES), paatos, requestContext(null));
        }

        @Test(expected = UnauthorizedException.class)
        public void resolveThrowsUnauthorizedIfVardaVirkailijaDoesNotHaveAnyKunta() {
                rekisterointiService.resolve(getAuthentication(VARDA_AUTHORITIES), paatos, requestContext(null));
        }

        @Test
        public void resolveSavesPaatosAndUpdatesRekisterointiTila() {
                rekisterointiService.resolve(getAuthentication(OPH_AUTHORITIES), paatos, requestContext(null));
                ArgumentCaptor<Rekisterointi> captor = ArgumentCaptor.forClass(Rekisterointi.class);
                verify(rekisterointiRepository).save(captor.capture());
                assertEquals(Rekisterointi.Tila.HYLATTY, captor.getValue().tila);
        }

        @Test
        public void resolveSavesPaatosAndUpdatesRekisterointiTilaIfVardaVirkailijaHasKunta() {
                OrganisaatioDto kunta = new OrganisaatioDto();
                kunta.kotipaikkaUri = "kunta_091";
                when(organisaatioClient.getKuntaByOid(eq("1.2.3.4.5"))).thenReturn(Optional.of(kunta));
                rekisterointiService.resolve(getAuthentication(VARDA_AUTHORITIES), paatos, requestContext(null));
                ArgumentCaptor<Rekisterointi> captor = ArgumentCaptor.forClass(Rekisterointi.class);
                verify(rekisterointiRepository).save(captor.capture());
                assertEquals(Rekisterointi.Tila.HYLATTY, captor.getValue().tila);
        }

        @Test
        public void resolveBatchSavesEachPaatosAndUpdatesRekisterointiTila() {
                List<Long> hakemusTunnukset = List.of(1L, 1L);
                PaatosBatch paatokset = new PaatosBatch(
                                true,
                                "OK!",
                                hakemusTunnukset);
                rekisterointiService.resolveBatch(getAuthentication(OPH_AUTHORITIES), paatokset, requestContext(null));
                ArgumentCaptor<Rekisterointi> captor = ArgumentCaptor.forClass(Rekisterointi.class);
                verify(rekisterointiRepository, times(hakemusTunnukset.size())).save(captor.capture());
                assertEquals(Rekisterointi.Tila.HYVAKSYTTY, captor.getValue().tila);
        }

        private RequestContext requestContext(String userId) {
                if (userId == null) {
                        return new RequestContextImpl("127.0.0.1");
                }
                return new RequestContextImpl(userId, "127.0.0.1");
        }

        public static Rekisterointi getRekisterointi() {
                return new Rekisterointi(
                                SAVED_REKISTEROINTI_ID,
                                Organisaatio.of(
                                                "0000000-0",
                                                null,
                                                LocalDate.now(),
                                                KielistettyNimi.of(
                                                                "fi", "Varda-yritys", null),
                                                "yritysmuoto_26",
                                                Set.of("organisaatiotyyppi_07"),
                                                "kunta_091",
                                                "maatjavaltiot1_fin",
                                                Set.of("opetuskieli"),
                                                Yhteystiedot.of(
                                                                "+358101234567",
                                                                "testi@testiyritys.fi",
                                                                Osoite.TYHJA, Osoite.TYHJA),
                                                false),
                                "varda",
                                "vardatoimintamuoto_tm01",
                                Set.of("kunta_091"),
                                Set.of("testi.sahkoposti@foo.bar"),
                                Kayttaja.builder()
                                                .etunimi("Testi")
                                                .sukunimi("Henkilö")
                                                .sahkoposti("testi.kayttaja@foo.bar")
                                                .asiointikieli("fi")
                                                .build(),
                                LocalDateTime.now(),
                                null,
                                Rekisterointi.Tila.KASITTELYSSA);
        }
}
