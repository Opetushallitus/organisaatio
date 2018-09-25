package fi.vm.sade.organisaatio.business.impl;

import fi.vm.sade.organisaatio.business.OrganisaatioViestinta;
import fi.vm.sade.organisaatio.dao.OrganisaatioDAO;
import fi.vm.sade.organisaatio.dto.HenkiloOrganisaatioCriteria;
import fi.vm.sade.organisaatio.dto.VirkailijaCriteria;
import fi.vm.sade.organisaatio.dto.VirkailijaDto;
import fi.vm.sade.organisaatio.service.util.ViestintaUtil;
import fi.vm.sade.properties.OphProperties;
import fi.vm.sade.ryhmasahkoposti.api.dto.EmailData;
import fi.vm.sade.ryhmasahkoposti.api.dto.EmailMessage;
import fi.vm.sade.ryhmasahkoposti.api.dto.EmailRecipient;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Collection;
import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import java.util.stream.Stream;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

@Service
public class VanhentuneetTiedotSahkopostiService {

    private static final Logger LOGGER = LoggerFactory.getLogger(VanhentuneetTiedotSahkopostiService.class);
    private static final String PALVELU = "ORGANISAATIO";
    private static final String KAYTTOOIKEUS = "VASTUUKAYTTAJAT";
    private static final Collection<String> TUETUT_KIELET = Stream.of("fi", "sv").collect(toSet());
    private static final String OLETUSKIELI = "fi";
    private static final long MAKSIMIMAARA = 20;

    private final KayttooikeusClient kayttooikeusClient;
    private final OrganisaatioViestinta organisaatioViestinta;
    private final OrganisaatioDAO organisaatioDAO;
    private final MessageSource messageSource;
    private final Configuration freemarker;
    private final OphProperties properties;

    public VanhentuneetTiedotSahkopostiService(KayttooikeusClient kayttooikeusClient,
            OrganisaatioViestinta organisaatioViestinta,
            OrganisaatioDAO organisaatioDAO,
            MessageSource messageSource,
            Configuration freemarker,
            OphProperties properties) {
        this.kayttooikeusClient = kayttooikeusClient;
        this.organisaatioViestinta = organisaatioViestinta;
        this.organisaatioDAO = organisaatioDAO;
        this.messageSource = messageSource;
        this.freemarker = freemarker;
        this.properties = properties;
    }

    public void lahetaSahkopostit() {
        haeOrganisaatiot().forEach(organisaatioOid -> {
            try {
                lahetaSahkoposti(organisaatioOid);
            } catch (Exception e) {
                LOGGER.error("Vanhentuneet tiedot -sähköpostin lähetys epäonnistui organisaatiolle {}", organisaatioOid, e);
            }
        });
    }

    private Collection<String> haeOrganisaatiot() {
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
        return organisaatioDAO.findOidByTarkastusPvm(tarkastusPvm, voimassaPvm, organisaatioOids, MAKSIMIMAARA);
    }

    private void lahetaSahkoposti(String organisaatioOid) {
        haeVirkailijat(organisaatioOid).stream()
                .filter(virkailija -> virkailija.getSahkoposti() != null)
                .collect(groupingBy(VanhentuneetTiedotSahkopostiService::getAsiointikieli,
                        mapping(VirkailijaDto::getSahkoposti, toSet())))
                .forEach((kieli, sahkopostiosoitteet) -> lahetaSahkoposti(organisaatioOid, kieli, sahkopostiosoitteet));
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

    private void lahetaSahkoposti(String organisaatioOid, String kieli, Collection<String> sahkopostiosoitteet) {
        Locale locale = new Locale(kieli);

        EmailMessage message = new EmailMessage();
        message.setCallingProcess(ViestintaUtil.CALLING_PROCESS);
        message.setLanguageCode(kieli);
        String otsikko = messageSource.getMessage("sahkopostit.vanhentuneettiedot.otsikko", null, locale);
        message.setSubject(otsikko);
        message.setBody(createBody(locale, organisaatioOid, otsikko));
        message.setHtml(true);

        EmailData data = new EmailData();
        data.setEmail(message);
        data.setRecipient(sahkopostiosoitteet.stream()
                .map(sahkopostiosoite -> new EmailRecipient(null, null, sahkopostiosoite, kieli))
                .collect(toList()));

        String sahkopostiId = organisaatioViestinta.sendEmail(data);
        LOGGER.info("Vanhentuneet tiedot -sähköpostin lähetys onnistui organisaatiolle {}: viestintäpalvelun tunniste {}", organisaatioOid, sahkopostiId);
    }

    private String createBody(Locale locale, String organisaatioOid, String otsikko) {
        try {
            Template template = freemarker.getTemplate("sahkoposti/vanhentuneettiedot.html", locale);
            Map<String, Object> model = new HashMap<>();
            model.put("otsikko", otsikko);
            model.put("linkki", properties.url("organisaatio-ui.organisaatio.byOid", organisaatioOid));
            return FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
        } catch (IOException | TemplateException ex) {
            throw new RuntimeException(ex);
        }
    }

}
