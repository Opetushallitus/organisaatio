package fi.vm.sade.organisaatio.business.impl;

import fi.vm.sade.organisaatio.business.OrganisaatioViestinta;
import fi.vm.sade.organisaatio.business.VanhentuneetTiedotSahkopostiService;
import fi.vm.sade.organisaatio.client.KayttooikeusClient;
import fi.vm.sade.organisaatio.dto.HenkiloOrganisaatioCriteria;
import fi.vm.sade.organisaatio.dto.VirkailijaCriteria;
import fi.vm.sade.organisaatio.dto.VirkailijaDto;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.model.OrganisaatioSahkoposti;
import fi.vm.sade.organisaatio.repository.OrganisaatioRepository;
import fi.vm.sade.organisaatio.repository.OrganisaatioSahkopostiRepository;
import fi.vm.sade.organisaatio.service.util.ViestintaUtil;
import fi.vm.sade.properties.OphProperties;
import fi.vm.sade.ryhmasahkoposti.api.dto.EmailData;
import fi.vm.sade.ryhmasahkoposti.api.dto.EmailMessage;
import fi.vm.sade.ryhmasahkoposti.api.dto.EmailRecipient;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.IOException;
import java.time.LocalDate;
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
    private final OrganisaatioViestinta organisaatioViestinta;
    private final OrganisaatioRepository organisaatioRepository;
    private final OrganisaatioSahkopostiRepository organisaatioSahkopostiRepository;
    private final MessageSource messageSource;
    private final Configuration freemarker;
    private final OphProperties properties;

    public VanhentuneetTiedotSahkopostiServiceImpl(KayttooikeusClient kayttooikeusClient,
                                                   OrganisaatioViestinta organisaatioViestinta,
                                                   OrganisaatioRepository organisaatioRepository,
                                                   OrganisaatioSahkopostiRepository organisaatioSahkopostiRepository,
                                                   MessageSource messageSource,
                                                   Configuration freemarker,
                                                   OphProperties properties) {
        this.kayttooikeusClient = kayttooikeusClient;
        this.organisaatioViestinta = organisaatioViestinta;
        this.organisaatioRepository = organisaatioRepository;
        this.organisaatioSahkopostiRepository = organisaatioSahkopostiRepository;
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
        Date tarkastusPvm = DateUtils.addYears(new Date(), -1);
        return organisaatioRepository.findByTarkastusPvm(tarkastusPvm, voimassaPvm, organisaatioOids, MAKSIMIMAARA);
    }

    private void lahetaSahkoposti(Organisaatio organisaatio) {
        haeVirkailijat(organisaatio.getOid()).stream()
                .filter(virkailija -> virkailija.getSahkoposti() != null)
                .collect(groupingBy(VanhentuneetTiedotSahkopostiServiceImpl::getAsiointikieli,
                        mapping(VirkailijaDto::getSahkoposti, toSet())))
                .forEach((kieli, sahkopostiosoitteet) -> lahetaSahkoposti(organisaatio, kieli, sahkopostiosoitteet));
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

    private void lahetaSahkoposti(Organisaatio organisaatio, String kieli, Collection<String> sahkopostiosoitteet) {
        Locale locale = new Locale(kieli);

        EmailMessage message = new EmailMessage();
        message.setCallingProcess(ViestintaUtil.CALLING_PROCESS);
        message.setLanguageCode(kieli);
        String otsikko = messageSource.getMessage("sahkopostit.vanhentuneettiedot.otsikko", null, locale);
        message.setSubject(otsikko);
        message.setBody(createBody(locale, organisaatio.getOid(), otsikko));
        message.setHtml(true);

        EmailData data = new EmailData();
        data.setEmail(message);
        data.setRecipient(sahkopostiosoitteet.stream()
                .map(sahkopostiosoite -> new EmailRecipient(null, null, sahkopostiosoite, kieli))
                .collect(toList()));

        String sahkopostiId = organisaatioViestinta.sendEmail(data, false);

        OrganisaatioSahkoposti organisaatioSahkoposti = new OrganisaatioSahkoposti();
        organisaatioSahkoposti.setOrganisaatio(organisaatio);
        organisaatioSahkoposti.setTyyppi(OrganisaatioSahkoposti.Tyyppi.VANHENTUNEET_TIEDOT);
        organisaatioSahkoposti.setAikaleima(new Date());
        organisaatioSahkoposti.setViestintapalveluId(sahkopostiId);
        organisaatioSahkopostiRepository.save(organisaatioSahkoposti);
    }

    private String createBody(Locale locale, String organisaatioOid, String otsikko) {
        try {
            freemarker.setClassForTemplateLoading(this.getClass(), "/templates");
            Template template = freemarker.getTemplate("/sahkoposti/vanhentuneettiedot.ftlh", locale);
            Map<String, Object> model = new HashMap<>();
            model.put("otsikko", otsikko);
            model.put("linkki", properties.url("organisaatio-ui.organisaatio.byOid", organisaatioOid));
            return FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
        } catch (IOException | TemplateException ex) {
            throw new RuntimeException(ex);
        }
    }

}
