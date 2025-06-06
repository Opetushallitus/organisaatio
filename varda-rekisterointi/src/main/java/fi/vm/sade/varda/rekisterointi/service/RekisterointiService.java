package fi.vm.sade.varda.rekisterointi.service;

import com.github.kagkarlsson.scheduler.SchedulerClient;
import com.github.kagkarlsson.scheduler.task.Task;
import fi.vm.sade.varda.rekisterointi.RequestContext;
import fi.vm.sade.varda.rekisterointi.dto.RekisterointiAuditDto;
import fi.vm.sade.varda.rekisterointi.event.CreatedEvent;
import fi.vm.sade.varda.rekisterointi.event.UpdatedEvent;
import fi.vm.sade.varda.rekisterointi.exception.InvalidInputException;
import fi.vm.sade.varda.rekisterointi.exception.UnauthorizedException;
import fi.vm.sade.varda.rekisterointi.model.Paatos;
import fi.vm.sade.varda.rekisterointi.model.PaatosBatch;
import fi.vm.sade.varda.rekisterointi.model.PaatosDto;
import fi.vm.sade.varda.rekisterointi.model.Rekisterointi;
import fi.vm.sade.varda.rekisterointi.repository.RekisterointiRepository;
import fi.vm.sade.varda.rekisterointi.util.AuthenticationUtils;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Palvelu rekisteröintihakemusten käsittelemiseen.
 */
@Service
@AllArgsConstructor
public class RekisterointiService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RekisterointiService.class);

    private final RekisterointiRepository rekisterointiRepository;
    private final OrganisaatioService organisaatioService;
    private final ApplicationEventPublisher eventPublisher;
    private final SchedulerClient schedulerClient;
    @Qualifier("rekisterointiEmailTask")
    private final Task<Long> rekisterointiEmailTask;
    @Qualifier("paatosEmailTask")
    private final Task<Long> paatosEmailTask;
    @Qualifier("luoTaiPaivitaOrganisaatioTask")
    private final Task<Long> luoTaiPaivitaOrganisaatioTask;

    /**
     * Listaa rekisteröintihakemukset tilan ja (valinnaisen) organisaatiohakutermin perusteella.
     *
     * @param tila          hakemusten tila
     * @param organisaatio  organisaation nimi tai sen osa (ei pakollinen)
     * @return ehtoihin täsmäävät rekisteröintihakemukset.
     */
    public Iterable<Rekisterointi> listByTilaAndOrganisaatio(Rekisterointi.Tila tila, String organisaatio) {
        if (organisaatio == null || organisaatio.length() == 0) {
            return rekisterointiRepository.findByTila(tila.toString());
        }
        return rekisterointiRepository.findByTilaAndOrganisaatioContaining(tila.toString(), organisaatio);
    }

    /**
     * Listaa rekisteröintihakemukset tilan, käsittelykunnan ja (valinnaisen) organisaatiohakutermin perusteella.
     * @param tila          hakemusten tila
     * @param kunnat        käsittelykunnat
     * @param organisaatio  organisaation nimi tai sen osa (ei pakollinen)
     * @return ehtoihin täsmäävät rekisteröintihakemukset.
     */
    public Iterable<Rekisterointi> listByTilaAndKunnatAndOrganisaatio(Rekisterointi.Tila tila, String[] kunnat,
                                                                      String organisaatio) {
        if (organisaatio == null || organisaatio.length() == 0) {
            return rekisterointiRepository.findByTilaAndKunnat(tila.toString(), kunnat);
        }
        return rekisterointiRepository.findByTilaAndKunnatAndOrganisaatioContaining(
                tila.toString(), kunnat, organisaatio);
    }

    public Iterable<Rekisterointi> listByVardaKunnat(String[] kunnat) {
        return rekisterointiRepository.findByVardaKunnat(kunnat);
    }

    public Iterable<Rekisterointi> listByRegistrationTypes(String[] registrationTypes) {
        return rekisterointiRepository.findByRegistrationTypes(registrationTypes);
    }

    /**
     * Luo rekisteröintihakemuksen.
     * @param rekisterointi     rekisteröinnin tiedot
     * @param requestContext    kontekstitiedot
     * @return luodun hakemuksen tunniste.
     */
    @Transactional
    public long create(Rekisterointi rekisterointi, RequestContext requestContext) {
        rekisterointi.organisaatio.setUudelleenRekisterointi(rekisterointiRepository.findByYtunnus(rekisterointi.organisaatio.ytunnus).iterator().hasNext());
        Rekisterointi saved = rekisterointiRepository.save(rekisterointi);
        String taskId = String.format("%s-%d", rekisterointiEmailTask.getName(), saved.id);
        schedulerClient.scheduleIfNotExists(rekisterointiEmailTask.instance(taskId, saved.id), Instant.now());
        eventPublisher.publishEvent(new CreatedEvent<>(requestContext, "rekisterointi", saved.id));
        LOGGER.info("Rekisteröinti luotu tunnuksella: {}", saved.id);
        return saved.id;
    }

    public void authorizeRekisterointi(Rekisterointi rekisterointi, Authentication authentication) {
        String[] registrationTypes = AuthenticationUtils.getRegistrationTypes(authentication);
        if (!Arrays.asList(registrationTypes).contains(rekisterointi.tyyppi)) {
            throw new UnauthorizedException();
        }

        List<String> organisaatioOidit = organisaatioService.haeOrganisaatioOidit(authentication.getAuthorities());
        if (organisaatioService.onOphVirkailija(organisaatioOidit)) {
            return;
        }

        if ("varda".equals(rekisterointi.tyyppi)) {
            List<String> authenticationKunnat = organisaatioService.virkailijanKunnat(organisaatioOidit);
            List<String> matchingKunnat = rekisterointi.kunnat.stream().filter(k -> authenticationKunnat.contains(k)).toList();
            if (matchingKunnat.isEmpty()) {
                throw new UnauthorizedException();
            }
        }
    }

    /**
     * Luo päätöksen rekisteröintihakemukselle.
     * @param paattajaOid       päätöken tekijän OID
     * @param paatosDto         päätös
     * @param requestContext    kontekstitiedot
     * @return päätöksen saanut rekisteröintihakemus.
     */
    @Transactional
    public Rekisterointi resolve(Authentication authentication, PaatosDto paatosDto, RequestContext requestContext) {
        Rekisterointi rekisterointi = rekisterointiRepository.findById(paatosDto.rekisterointi).orElseThrow(
                () -> new InvalidInputException("Rekisteröintiä ei löydy, id: " + paatosDto.rekisterointi));
        authorizeRekisterointi(rekisterointi, authentication);
        Paatos paatos = new Paatos(paatosDto.hyvaksytty, LocalDateTime.now(), authentication.getName(), paatosDto.perustelu);
        if (rekisterointi.tila != Rekisterointi.Tila.KASITTELYSSA) {
            throw new IllegalStateException(
                    "Päätöstä ei voi tehdä; rekisteröinti ei ole käsittelyssä-tilassa, id: " + rekisterointi.id);
        }
        RekisterointiAuditDto auditBeforeDto = new RekisterointiAuditDto(rekisterointi.tila);
        Rekisterointi saved = rekisterointiRepository.save(rekisterointi.withPaatos(paatos));
        LOGGER.info("Päätös tallennettu rekisteröinnille: {}", rekisterointi.id);
        RekisterointiAuditDto auditAfterDto = new RekisterointiAuditDto(saved.tila);
        LOGGER.info("Rekisteröinnin {} tila päivitetty: {}", saved.id, saved.tila);
        eventPublisher.publishEvent(new UpdatedEvent<>(requestContext, "rekisterointi", saved.id,
                auditBeforeDto, auditAfterDto));

        if (paatos.hyvaksytty) {
            ajastaOrganisaationLuontiTaiPaivitys(saved);
        } else {
            ajastaHylkaysViesti(saved);
        }
        return saved;
    }

    private void ajastaOrganisaationLuontiTaiPaivitys(Rekisterointi rekisterointi) {
        schedulerClient.scheduleIfNotExists(
                luoTaiPaivitaOrganisaatioTask.instance(taskId(luoTaiPaivitaOrganisaatioTask, rekisterointi.id), rekisterointi.id),
                Instant.now()
        );
    }

    private void ajastaHylkaysViesti(Rekisterointi rekisterointi) {
        schedulerClient.scheduleIfNotExists(
                paatosEmailTask.instance(taskId(paatosEmailTask, rekisterointi.id), rekisterointi.id),
                Instant.now());
    }

    private String taskId(Task<Long> task, Long rekisterointiId) {
        return String.format("%s-%d", task.getName(), rekisterointiId);
    }

    @Transactional
    public void resolveBatch(Authentication authentication, PaatosBatch paatokset, RequestContext requestContext) {
        paatokset.hakemukset.forEach(id -> resolve(
                authentication,
                new PaatosDto(
                        id,
                        paatokset.hyvaksytty,
                        paatokset.perustelu
                ),
                requestContext
        ));
    }

}
