package fi.vm.sade.organisaatio.service.converter.v4;

import fi.vm.sade.organisaatio.model.*;
import fi.vm.sade.rajapinnat.ytj.api.YTJDTO;
import fi.vm.sade.rajapinnat.ytj.api.YTJOsoiteDTO;
import fi.vm.sade.rajapinnat.ytj.service.YtjDtoMapperHelper;
import org.springframework.core.convert.converter.Converter;

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static fi.vm.sade.organisaatio.business.impl.OrganisaatioYtjServiceImpl.ORG_KIELI_KOODI_FI;
import static fi.vm.sade.organisaatio.business.impl.OrganisaatioYtjServiceImpl.ORG_KIELI_KOODI_SV;
import static fi.vm.sade.organisaatio.service.util.PredicateUtil.not;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;

public class YtjDtoToOrganisaatioConverter implements Converter<YTJDTO, Organisaatio> {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final String MAAT_JA_VALTIOT_1_FIN = "maatjavaltiot1_fin";
    private static final String KIELI_FI = "fi";
    private static final String KIELI_SV = "sv";
    private static final String KUNTA_KOODISTO = "kunta";
    private static final String POSTINUMERO_KOODISTO = "posti";

    @Override
    public Organisaatio convert(YTJDTO source) {
        if (source == null) {
            return null;
        }

        Date alkuPvm = localDateAsStringToJavaSqlDate(source.getYritysTunnus().getAlkupvm()).orElse(null);
        Date lakkautusPvm = localDateAsStringToJavaSqlDate(source.getYritysTunnus().getLoppupvm()).orElse(null);
        Date ytjPaivitysPvm = localDateToJavaSqlDate(LocalDate.now());
        String kieli = ytjKieliToOrgKieli(source.getYrityksenKieli());
        String koodistoKieli = String.format("kieli_%s#1", kieli);
        Date nimiAlkuPvm = localDateAsStringToJavaSqlDate(source.getAloitusPvm()).orElse(alkuPvm);

        Organisaatio destination = new Organisaatio();
        destination.setAlkuPvm(alkuPvm);
        destination.setLakkautusPvm(lakkautusPvm);

        destination.setKielet(singleton(oppilaitoksenOpetuskieli(kieli)));
        ofEmpty(source.getKotiPaikkaKoodi())
                .map(YtjDtoToOrganisaatioConverter::kotipaikkaKoodiToKoodisto)
                .ifPresent(destination::setKotipaikka);
        destination.setMaa(MAAT_JA_VALTIOT_1_FIN);

        Map<String, String> nimi = new LinkedHashMap<>();
        ofEmpty(source.getNimi()).ifPresent(fi -> nimi.put(KIELI_FI, fi));
        ofEmpty(source.getSvNimi()).ifPresent(sv -> nimi.put(KIELI_SV, sv));
        destination.setNimi(monikielinenTeksti(nimi));
        destination.setNimet(singletonList(organisaatioNimi(nimiAlkuPvm, nimi)));

        ofEmpty(source.getYritysmuoto()).ifPresent(destination::setYritysmuoto);
        destination.setYtjKieli(koodistoKieli);
        destination.setYtjPaivitysPvm(ytjPaivitysPvm);
        destination.setYtunnus(source.getYtunnus());

        Stream<Optional<Yhteystieto>> yhteystiedot = Stream.of(
                osoiteToYhteystieto(source.getPostiOsoite(), Osoite.TYYPPI_POSTIOSOITE, ytjPaivitysPvm),
                osoiteToYhteystieto(source.getKayntiOsoite(), Osoite.TYYPPI_KAYNTIOSOITE, ytjPaivitysPvm),
                puhelinnumeroToYhteystieto(source.getPuhelin()),
                wwwToYhteystieto(source.getWww()),
                emailToYhteystieto(source.getSahkoposti()));
        yhteystiedot
                .filter(Optional::isPresent)
                .map(Optional::get)
                .peek(yhteystieto -> yhteystieto.setKieli(koodistoKieli))
                .forEach(destination::addYhteystieto);

        return destination;
    }

    private static Optional<Date> localDateAsStringToJavaSqlDate(String dateAsString) {
        return ofEmpty(dateAsString)
                .map(date -> LocalDate.parse(date, DATE_FORMATTER))
                .map(YtjDtoToOrganisaatioConverter::localDateToJavaSqlDate);
    }

    private static Date localDateToJavaSqlDate(LocalDate date) {
        return Date.valueOf(date);
    }

    private static OrganisaatioNimi organisaatioNimi(Date alkuPvm, Map<String, String> nimi) {
        OrganisaatioNimi dto = new OrganisaatioNimi();
        dto.setAlkuPvm(alkuPvm);
        dto.setNimi(monikielinenTeksti(nimi));
        return dto;
    }

    private static String ytjKieliToOrgKieli(String source) {
        return YtjDtoMapperHelper.KIELI_SV.equals(source) ? KIELI_SV : KIELI_FI;
    }

    private static String oppilaitoksenOpetuskieli(String kieli) {
        switch (kieli) {
            case KIELI_FI:
                return ORG_KIELI_KOODI_FI;
            case KIELI_SV:
                return ORG_KIELI_KOODI_SV;
            default:
                throw new IllegalArgumentException("Tuntematon kieli: " + kieli);
        }
    }

    private static MonikielinenTeksti monikielinenTeksti(Map<String, String> map) {
        MonikielinenTeksti monikielinenTeksti = new MonikielinenTeksti();
        map.forEach((key, value) -> monikielinenTeksti.addString(key, value));
        return monikielinenTeksti;
    }

    private static Optional<Yhteystieto> osoiteToYhteystieto(YTJOsoiteDTO ytjOsoite, String tyyppi, Date ytjPaivitysPvm) {
        return Optional.ofNullable(ytjOsoite).flatMap(source -> ofEmpty(source.getKatu()).map(katu -> {
            Osoite destination = new Osoite();
            destination.setOsoiteTyyppi(tyyppi);
            destination.setOsoite(katu);
            ofEmpty(source.getPostinumero()).map(YtjDtoToOrganisaatioConverter::postinumeroToKoodisto)
                    .ifPresent(destination::setPostinumero);
            ofEmpty(source.getToimipaikka()).ifPresent(destination::setPostitoimipaikka);
            destination.setYtjPaivitysPvm(ytjPaivitysPvm);
            return destination;
        }));
    }

    private static Optional<Yhteystieto> puhelinnumeroToYhteystieto(String ytjPuhelinnumero) {
        return Optional.ofNullable(ytjPuhelinnumero).map(source -> {
            Puhelinnumero puhelinnumero = new Puhelinnumero();
            puhelinnumero.setTyyppi(Puhelinnumero.TYYPPI_PUHELIN);
            puhelinnumero.setPuhelinnumero(source);
            return puhelinnumero;
        });
    }

    private static Optional<Yhteystieto> wwwToYhteystieto(String ytjWww) {
        return ofEmpty(ytjWww).map(source -> {
            Www www = new Www();
            www.setWwwOsoite(source);
            return www;
        });
    }

    private static Optional<Yhteystieto> emailToYhteystieto(String ytjEmail) {
        return ofEmpty(ytjEmail).map(source -> {
            Email email = new Email();
            email.setEmail(source);
            return email;
        });
    }

    private static String kotipaikkaKoodiToKoodisto(String kotipaikkaKoodi) {
        return String.format("%s_%s", KUNTA_KOODISTO, kotipaikkaKoodi);
    }

    private static String postinumeroToKoodisto(String postinumero) {
        return String.format("%s_%s", POSTINUMERO_KOODISTO, postinumero);
    }

    private static Optional<String> ofEmpty(String str) {
        return STRING_CONVERTER.apply(str);
    }

    private static Function<String, Optional<String>> STRING_CONVERTER = (str -> Optional.ofNullable(str)
            .map(String::trim).filter(not(String::isEmpty)));

}
