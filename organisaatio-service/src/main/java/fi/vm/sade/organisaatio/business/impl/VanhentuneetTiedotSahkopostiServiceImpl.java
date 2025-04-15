package fi.vm.sade.organisaatio.business.impl;

import fi.vm.sade.organisaatio.business.VanhentuneetTiedotSahkopostiService;
import fi.vm.sade.organisaatio.client.KayttooikeusClient;
import fi.vm.sade.organisaatio.dto.HenkiloOrganisaatioCriteria;
import fi.vm.sade.organisaatio.dto.VirkailijaCriteria;
import fi.vm.sade.organisaatio.dto.VirkailijaDto;
import fi.vm.sade.organisaatio.email.EmailService;
import fi.vm.sade.organisaatio.email.QueuedEmail;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.repository.OrganisaatioRepository;
import fi.vm.sade.properties.OphProperties;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Stream;

import static java.util.Collections.*;
import static java.util.stream.Collectors.*;

@Service
@Transactional
public class VanhentuneetTiedotSahkopostiServiceImpl implements VanhentuneetTiedotSahkopostiService {

    private static final String PALVELU = "ORGANISAATIOHALLINTA";
    private static final String KAYTTOOIKEUS = "VASTUUKAYTTAJAT";
    private static final Collection<String> TUETUT_KIELET = Stream.of("fi", "sv").collect(toSet());
    private static final String OLETUSKIELI = "fi";
    private static final long MAKSIMIMAARA = 20;

    private final KayttooikeusClient kayttooikeusClient;
    private final EmailService emailService;
    private final OrganisaatioRepository organisaatioRepository;
    private final MessageSource messageSource;
    private final Configuration freemarker;
    private final OphProperties properties;

    public VanhentuneetTiedotSahkopostiServiceImpl(KayttooikeusClient kayttooikeusClient,
                                                   EmailService emailService,
                                                   OrganisaatioRepository organisaatioRepository,
                                                   MessageSource messageSource,
                                                   Configuration freemarker,
                                                   OphProperties properties) {
        this.kayttooikeusClient = kayttooikeusClient;
        this.emailService = emailService;
        this.organisaatioRepository = organisaatioRepository;
        this.messageSource = messageSource;
        this.freemarker = freemarker;
        this.properties = properties;
    }

    public void lahetaSahkopostit() {
        haeOrganisaatiot().forEach(this::lahetaSahkoposti);
    }

    private Collection<Organisaatio> haeOrganisaatiot() {
        // haetaan organisaatiot joissa on halutun käyttöoikeuden omaavia virkailijoita
        HenkiloOrganisaatioCriteria criteria = new HenkiloOrganisaatioCriteria();
        criteria.setKayttajaTyyppi("VIRKAILIJA");
        criteria.setPassivoitu(false);
        criteria.setKayttooikeudet(singletonList(PALVELU + "_" + KAYTTOOIKEUS));
        Collection<String> organisaatioOids = kayttooikeusClient.listOrganisaatioOid(criteria);

        if (organisaatioOids.isEmpty()) {
            return emptyList();
        }

        LocalDate voimassaPvm = LocalDate.now();
        Date tarkastusPvm = Date.from(ZonedDateTime.now().minusYears(1).toInstant());
        return organisaatioRepository.findByTarkastusPvm(tarkastusPvm, voimassaPvm, organisaatioOids, MAKSIMIMAARA);
    }

    private void lahetaSahkoposti(Organisaatio organisaatio) {
        haeVirkailijat(organisaatio.getOid()).stream()
                .filter(virkailija -> virkailija.getSahkoposti() != null)
                .collect(groupingBy(VanhentuneetTiedotSahkopostiServiceImpl::getAsiointikieli,
                        mapping(VirkailijaDto::getSahkoposti, toList())))
                .forEach((kieli, sahkopostiosoitteet) -> queueEmail(organisaatio, kieli, sahkopostiosoitteet));
    }

    private Collection<VirkailijaDto> haeVirkailijat(String organisaatioOid) {
        // haetaan organisaation virkailijat joilla on haluttu käyttöoikeus
        VirkailijaCriteria criteria = new VirkailijaCriteria();
        criteria.setPassivoitu(false);
        criteria.setDuplikaatti(false);
        criteria.setOrganisaatioOids(singleton(organisaatioOid));
        criteria.setKayttooikeudet(singletonMap(PALVELU, singletonList(KAYTTOOIKEUS)));
        return kayttooikeusClient.listVirkailija(criteria);
    }

    private static String getAsiointikieli(VirkailijaDto virkailija) {
        return Optional.ofNullable(virkailija.getAsiointikieli()).filter(TUETUT_KIELET::contains).orElse(OLETUSKIELI);
    }

    private void queueEmail(Organisaatio organisaatio, String kieli, List<String> sahkopostiosoitteet) {
        Locale locale = Locale.of(kieli);
        String otsikko = messageSource.getMessage("sahkopostit.vanhentuneettiedot.otsikko", null, locale);
        QueuedEmail email = QueuedEmail.builder()
            .subject(otsikko)
            .recipients(sahkopostiosoitteet)
            .body(createBody(kieli, organisaatio.getOid(), otsikko))
            .build();

        emailService.queueEmail(email);
    }

    private String createBody(String kieli, String organisaatioOid, String otsikko) {
        try {
            freemarker.setClassForTemplateLoading(this.getClass(), "/templates");
            Template template = freemarker.getTemplate("sahkoposti/vanhentuneettiedot_" + kieli + ".ftlh");
            Map<String, Object> model = new HashMap<>();
            model.put("otsikko", otsikko);
            model.put("linkki", properties.url("organisaatio-ui.organisaatio.byOid", organisaatioOid));
            return FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
        } catch (IOException | TemplateException ex) {
            throw new RuntimeException(ex);
        }
    }

}
