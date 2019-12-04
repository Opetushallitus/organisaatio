package fi.vm.sade.varda.rekisterointi.service;

import fi.vm.sade.varda.rekisterointi.Template;
import fi.vm.sade.varda.rekisterointi.client.KayttooikeusClient;
import fi.vm.sade.varda.rekisterointi.client.OrganisaatioClient;
import fi.vm.sade.varda.rekisterointi.client.ViestintaClient;
import fi.vm.sade.varda.rekisterointi.model.*;
import fi.vm.sade.varda.rekisterointi.repository.RekisterointiRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.StreamSupport;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final RekisterointiRepository rekisterointiRepository;
    private final TemplateService templateService;
    private final MessageSource messageSource;
    private final ViestintaClient viestintaClient;
    private final KayttooikeusClient kayttooikeusClient;
    private final OrganisaatioClient organisaatioClient;

    public void lahetaRekisterointiEmail(long id) {
        rekisterointiRepository.findById(id).ifPresent(rekisterointi -> {
            lahetaRekisterointiEmail(rekisterointi);
            lahetaRekisterointiEmail(rekisterointi.kayttaja);
        });
    }

    private void lahetaRekisterointiEmail(Rekisterointi rekisterointi) {
        EmailDto email = EmailDto.builder()
                .emails(rekisterointi.sahkopostit)
                .message(luoViesti(rekisterointi, new Locale("fi")))
                .build();
        viestintaClient.save(email, false);
    }

    private EmailMessageDto luoViesti(Rekisterointi rekisterointi, Locale locale) {
        String organisaatioNimi = rekisterointi.organisaatio.ytjNimi.nimi;
        return EmailMessageDto.builder()
                .subject(messageSource.getMessage("rekisteroityminen.kayttaja.otsikko", null, locale))
                .body(templateService.getContent(Template.REKISTEROITYMINEN_KAYTTAJA, locale,
                        Map.of("organisaatioNimi", organisaatioNimi)))
                .html(true)
                .build();
    }

    private void lahetaRekisterointiEmail(Kayttaja kayttaja) {
        Locale locale = new Locale(kayttaja.asiointikieli);
        String body = templateService.getContent(Template.REKISTEROITYMINEN_PAAKAYTTAJA, locale,
                Map.of("etunimi", kayttaja.etunimi));
        EmailDto email = EmailDto.builder()
                .email(kayttaja.sahkoposti)
                .message(EmailMessageDto.builder()
                        .subject(messageSource.getMessage("rekisteroityminen.paakayttaja.otsikko", null, locale))
                        .body(body)
                        .html(true)
                        .build())
                .build();
        viestintaClient.save(email, false);
    }

    public void lahetaPaatosEmail(long id) {
        rekisterointiRepository.findById(id).ifPresent(rekisterointi -> {
            String organisaatioNimi = rekisterointi.organisaatio.ytjNimi.nimi;
            EmailDto email = EmailDto.builder()
                    .emails(rekisterointi.sahkopostit)
                    .message(luoViesti(rekisterointi.paatos, new Locale(rekisterointi.kayttaja.asiointikieli), organisaatioNimi))
                    .build();
            viestintaClient.save(email, false);
        });
    }

    private EmailMessageDto luoViesti(Paatos paatos, Locale locale, String organisaatioNimi) {
        if (paatos.hyvaksytty) {
            return EmailMessageDto.builder()
                    .subject(messageSource.getMessage("rekisteroityminen.hyvaksytty.otsikko", null, locale))
                    .body(templateService.getContent(Template.REKISTEROITYMINEN_HYVAKSYTTY, locale,
                            Map.of("organisaatioNimi", organisaatioNimi)))
                    .html(true)
                    .build();
        }
        return EmailMessageDto.builder()
                .subject(messageSource.getMessage("rekisteroityminen.hylatty.otsikko", null, locale))
                .body(templateService.getContent(Template.REKISTEROITYMINEN_HYLATTY, locale,
                        Map.of("organisaatioNimi", organisaatioNimi, "perustelu", paatos.perustelu)))
                .html(true)
                .build();
    }

    public void lahetaKuntaEmail() {
        Iterable<Rekisterointi> kasittelemattomat = rekisterointiRepository.findByTila(Rekisterointi.Tila.KASITTELYSSA.toString());
        Set<String> kunnat = StreamSupport.stream(kasittelemattomat.spliterator(), false)
                .flatMap(rekisterointi -> rekisterointi.kunnat.stream()).collect(toSet());
        Map<VirkailijaDto, Long> virkailijat = getVirkailijaByKunta(kunnat);
        virkailijat.forEach(this::lahetaKuntaEmail);
    }

    private Map<VirkailijaDto, Long> getVirkailijaByKunta(Set<String> kunnat) {
        return kunnat.stream()
                .map(kunta -> getOrganisaatioByKunta(kunta).stream().map(organisaatio -> organisaatio.oid))
                .flatMap(organisaatioOid -> getVirkailijaByOrganisaatio(organisaatioOid.collect(toSet())).stream())
                .filter(virkailija -> virkailija.sahkoposti != null && !virkailija.sahkoposti.isEmpty())
                .collect(groupingBy(identity(), counting()));
    }

    private Collection<OrganisaatioV4Dto> getOrganisaatioByKunta(String kunta) {
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
        viestintaClient.save(email, false);
    }

}
