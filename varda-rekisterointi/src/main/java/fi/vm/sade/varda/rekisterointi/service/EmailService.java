package fi.vm.sade.varda.rekisterointi.service;

import fi.vm.sade.varda.rekisterointi.Template;
import fi.vm.sade.varda.rekisterointi.client.KayttooikeusClient;
import fi.vm.sade.varda.rekisterointi.client.OrganisaatioClient;
import fi.vm.sade.varda.rekisterointi.client.ViestintaClient;
import fi.vm.sade.varda.rekisterointi.model.*;
import fi.vm.sade.varda.rekisterointi.repository.RekisterointiRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.StreamSupport;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;

@Service
@RequiredArgsConstructor
public class EmailService {

    public static final List<Locale> LOCALES = List.of(new Locale("fi"), new Locale("sv"));
    private static final Logger LOGGER = LoggerFactory.getLogger(EmailService.class);
    private static final String SUBJECT_DELIMITER = " / ";
    private static final String FAILED_TASKS_EMAIL_ADDRESS = "yhteisetpalvelut@opintopolku.fi";

    private final RekisterointiRepository rekisterointiRepository;
    private final TemplateService templateService;
    private final MessageSource messageSource;
    private final ViestintaClient viestintaClient;
    private final KayttooikeusClient kayttooikeusClient;
    private final OrganisaatioClient organisaatioClient;

    /**
     * Lähettää ilmoituksen rekisteröintihakemuksen luonnista.
     *
     * @param id rekisteröinnin id
     */
    public void lahetaRekisterointiEmail(long id) {
        rekisterointiRepository.findById(id).ifPresentOrElse(rekisterointi -> {
            lahetaRekisterointiEmail(rekisterointi);
            lahetaRekisterointiEmail(rekisterointi.kayttaja, rekisterointi.tyyppi);
        }, () -> LOGGER.warn(
                "Rekisteröinti-ilmoituksen lähetys epäonnistui, rekisteröintiä ei löydy tunnisteella: {}", id));
    }

    /**
     * Lähettää ilmoituksen rekisteröintihakemukselle annetusta päätöksestä.
     *
     * @param id rekisteröinnin id.
     */
    public void lahetaPaatosEmail(long id) {
        rekisterointiRepository.findById(id).ifPresent(rekisterointi -> {
            String organisaatioNimi = rekisterointi.organisaatio.ytjNimi.nimi;
            EmailDto email = EmailDto.builder()
                    .emails(rekisterointi.sahkopostit)
                    .message(rekisterointi.tyyppi.equals("varda")
                        ? luoVardaPaatosViesti(rekisterointi.paatos, organisaatioNimi)
                        : luoPaatosViesti(rekisterointi.tyyppi, rekisterointi.paatos, organisaatioNimi))
                    .build();
            LOGGER.info("Lähetetään ilmoitus rekisteröinnin {} päätöksestä osoitteisiin: {}",
                    id, String.join(", ", rekisterointi.sahkopostit));
            viestintaClient.save(email, false);
        });
    }

    private EmailMessageDto luoPaatosViesti(String tyyppi, Paatos paatos, String organisaatioNimi) {
        if (paatos.hyvaksytty) {
            return EmailMessageDto.builder()
                    .subject(subjectToAllLanguages("rekisteroityminen.otsikko.hyvaksytty"))
                    .body(templateService.getContent(Template.GENERIC_HYVAKSYTTY, new Locale("fi"),
                            Map.of( "organisaatioNimi", organisaatioNimi, "tyyppi", tyyppi)))
                    .html(false)
                    .build();
        }
        return EmailMessageDto.builder()
                .subject(subjectToAllLanguages("rekisteroityminen.otsikko.hylatty"))
                .body(templateService.getContent(Template.GENERIC_HYLATTY, new Locale("fi"),
                        Map.of( "organisaatioNimi", organisaatioNimi, "perustelu", paatos.perustelu, "tyyppi", tyyppi)))
                .html(false)
                .build();
    }

    /**
     * Lähettää kuntien virkailijoille ilmoituksen käsittelemättömistä rekisteröintihakemuksista.
     */
    public void lahetaKuntaEmail() {
        Iterable<Rekisterointi> kasittelemattomat = rekisterointiRepository.findByRegistrationTypeAndTila(new String[]{"varda"}, Rekisterointi.Tila.KASITTELYSSA.toString());
        Set<String> kunnat = StreamSupport.stream(kasittelemattomat.spliterator(), false)
                .flatMap(rekisterointi -> rekisterointi.kunnat.stream()).collect(toSet());
        Map<VirkailijaDto, Long> virkailijat = getVirkailijaByKunta(kunnat);
        virkailijat.forEach(this::lahetaKuntaEmail);
    }

    public void lahetaOngelmaRaportti(Set<TaskMonitoringService.TaskFailure> epaonnistuneet) {
        Set<TaskFailureInformation> failures = epaonnistuneet.stream().map(failure ->
                new TaskFailureInformation(
                        failure,
                        rekisterointiRepository.findById(failure.rekisterointi).orElseThrow(
                                () -> new IllegalStateException("No registration found by id: " + failure.rekisterointi)
                        ))).collect(toSet());
        lahetaVirheRaportti(failures);
    }

    private void lahetaRekisterointiEmail(Rekisterointi rekisterointi) {
        EmailDto email = EmailDto.builder()
                .emails(rekisterointi.sahkopostit)
                .message(luoKayttajaViesti(rekisterointi))
                .build();
        LOGGER.info("Lähetetään ilmoitus rekisteröinnistä osoitteisiin: {}",
                String.join(", ", rekisterointi.sahkopostit));
        viestintaClient.save(email, false);
    }

    private EmailMessageDto luoKayttajaViesti(Rekisterointi rekisterointi) {
        String organisaatioNimi = rekisterointi.organisaatio.ytjNimi.nimi;
        if (rekisterointi.tyyppi.equals("varda")) {
            return EmailMessageDto.builder()
                .subject(subjectToAllLanguages("rekisteroityminen.kayttaja.otsikko"))
                .body(templateService.getContent(Template.REKISTEROITYMINEN_KAYTTAJA, new Locale("fi"),
                        Map.of("messageSource", messageSource, "locales", LOCALES, "organisaatioNimi", organisaatioNimi)))
                .html(true)
                .build();
        } else {
            return EmailMessageDto.builder()
                .subject(subjectToAllLanguages("rekisteroityminen.otsikko.kayttaja"))
                .body(templateService.getContent(Template.GENERIC_KAYTTAJA, new Locale("fi"),
                        Map.of("organisaatioNimi", organisaatioNimi, "tyyppi", rekisterointi.tyyppi)))
                .html(false)
                .build();
        }
    }

    private void lahetaRekisterointiEmail(Kayttaja kayttaja, String tyyppi) {
        Locale locale = new Locale(kayttaja.asiointikieli);
        EmailMessageDto message = tyyppi.equals("varda")
                ? EmailMessageDto.builder()
                        .subject(messageSource.getMessage("rekisteroityminen.paakayttaja.otsikko", null, locale))
                        .body(templateService.getContent(Template.REKISTEROITYMINEN_PAAKAYTTAJA, locale,
                                Map.of("etunimi", kayttaja.etunimi)))
                        .html(true)
                        .build()
                : EmailMessageDto.builder()
                        .subject(subjectToAllLanguages("rekisteroityminen.otsikko.paakayttaja"))
                        .body(templateService.getContent(Template.GENERIC_PAAKAYTTAJA, locale,
                                Map.of("etunimi", kayttaja.etunimi, "tyyppi", tyyppi)))
                        .html(false)
                        .build();
        EmailDto email = EmailDto.builder()
                .email(kayttaja.sahkoposti)
                .message(message)
                .build();
        LOGGER.info("Lähetetään ilmoitus rekisteröinnistä pääkäyttäjälle: {}", kayttaja.sahkoposti);
        viestintaClient.save(email, false);
    }

    private EmailMessageDto luoVardaPaatosViesti(Paatos paatos, String organisaatioNimi) {
        if (paatos.hyvaksytty) {
            return EmailMessageDto.builder()
                    .subject(subjectToAllLanguages("rekisteroityminen.hyvaksytty.otsikko"))
                    .body(templateService.getContent(Template.REKISTEROITYMINEN_HYVAKSYTTY, new Locale("fi"),
                            Map.of("messageSource", messageSource, "locales", LOCALES, "organisaatioNimi", organisaatioNimi)))
                    .html(true)
                    .build();
        }
        return EmailMessageDto.builder()
                .subject(subjectToAllLanguages("rekisteroityminen.hylatty.otsikko"))
                .body(templateService.getContent(Template.REKISTEROITYMINEN_HYLATTY, new Locale("fi"),
                        Map.of("messageSource", messageSource, "locales", LOCALES, "organisaatioNimi", organisaatioNimi, "perustelu", paatos.perustelu)))
                .html(true)
                .build();
    }

    private Map<VirkailijaDto, Long> getVirkailijaByKunta(Set<String> kunnat) {
        return kunnat.stream()
                .map(kunta -> getOrganisaatioByKunta(kunta).stream().map(organisaatio -> organisaatio.oid))
                .flatMap(organisaatioOid -> getVirkailijaByOrganisaatio(organisaatioOid.collect(toSet())).stream())
                .filter(virkailija -> virkailija.sahkoposti != null && !virkailija.sahkoposti.isEmpty())
                .collect(groupingBy(identity(), counting()));
    }

    private Collection<OrganisaatioDto> getOrganisaatioByKunta(String kunta) {
        OrganisaatioCriteria organisaatioCriteria = new OrganisaatioCriteria();
        organisaatioCriteria.aktiiviset = true;
        organisaatioCriteria.yritysmuoto = List.of("Kunta");
        organisaatioCriteria.kunta = List.of(kunta);
        return organisaatioClient.listBy(organisaatioCriteria);
    }

    private Collection<VirkailijaDto> getVirkailijaByOrganisaatio(Set<String> organisaatioOids) {
        VirkailijaCriteria virkailijaCriteria = new VirkailijaCriteria();
        virkailijaCriteria.passivoitu = false;
        virkailijaCriteria.duplikaatti = false;
        virkailijaCriteria.organisaatioOids = organisaatioOids;
        virkailijaCriteria.kayttooikeudet = Map.of("YKSITYISTEN_REKISTEROITYMINEN", List.of("CRUD"));
        return kayttooikeusClient.listVirkailijaBy(virkailijaCriteria);
    }

    private void lahetaKuntaEmail(VirkailijaDto virkailija, Long organisaatioLkm) {
        Locale locale = new Locale(Optional.ofNullable(virkailija.asiointikieli).orElse("fi"));
        String subject = messageSource.getMessage("rekisteroityminen.kunta.otsikko", null, locale);
        Map<String, Object> variables = Map.of("organisaatioLkm", organisaatioLkm);
        String body = templateService.getContent(Template.REKISTEROITYMINEN_KUNTA, locale, variables);
        EmailDto email = EmailDto.builder()
                .email(virkailija.sahkoposti)
                .message(EmailMessageDto.builder()
                        .subject(subject)
                        .body(body)
                        .html(true)
                        .build())
                .build();
        LOGGER.info("Lähetetään ilmoitus rekisteröitymisestä virkailijalle: {}", virkailija.sahkoposti);
        viestintaClient.save(email, false);
    }

    private void lahetaVirheRaportti(Set<TaskFailureInformation> failures) {
        Locale locale = new Locale("fi");
        String subject = messageSource.getMessage("taskien-virheraportti.otsikko", null, locale);
        Map<String, Object> variables = Map.of(
                "epaonnistuneetLkm", failures.size(),
                "epaonnistuneet", failures);
        String body = templateService.getContent(Template.AJASTETTUJEN_TASKIEN_VIRHERAPORTTI, locale, variables);
        EmailDto email = EmailDto.builder()
                .email(FAILED_TASKS_EMAIL_ADDRESS)
                .message(EmailMessageDto.builder()
                        .subject(subject)
                        .body(body)
                        .html(true)
                        .build())
                .build();
        LOGGER.info("Lähetetään raportti epäonnistuneista taskeista osoitteeseen: {}", FAILED_TASKS_EMAIL_ADDRESS);
        viestintaClient.save(email, false);
    }

    private String subjectToAllLanguages(String code) {
        return subjectToAllLanguages(locale -> messageSource.getMessage(code, null, locale));
    }

    private String subjectToAllLanguages(Function<Locale, String> messageByLocale) {
        return LOCALES.stream().map(messageByLocale::apply).collect(joining(SUBJECT_DELIMITER));
    }

    public static class TaskFailureInformation {
        public final TaskMonitoringService.TaskFailure virhe;
        public final Rekisterointi rekisterointi;
        public TaskFailureInformation(TaskMonitoringService.TaskFailure virhe, Rekisterointi rekisterointi) {
            this.virhe = virhe;
            this.rekisterointi = rekisterointi;
        }
    }

}
