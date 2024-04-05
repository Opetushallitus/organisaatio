package fi.vm.sade.organisaatio.business.impl;

import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.business.OrganisaatioKoodisto;
import fi.vm.sade.organisaatio.business.OrganisaatioValidationService;
import fi.vm.sade.organisaatio.business.exception.NoVersionInKoodistoUriException;
import fi.vm.sade.organisaatio.dto.Koodi;
import fi.vm.sade.organisaatio.dto.mapping.KoodiToUriVersioMapper;
import fi.vm.sade.organisaatio.model.*;
import fi.vm.sade.organisaatio.service.util.OrganisaatioUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.validation.ValidationException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Service
public class OrganisaatioValidationServiceImpl implements OrganisaatioValidationService {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    private final String rootOrganisaatioOid;

    private final OrganisaatioKoodisto organisaatioKoodisto;

    private static final String URI_WITH_VERSION_PATTERN = "^.*#[0-9]+$";
    private static final String YTUNNUS_PATTERN = "\\d\\d\\d\\d\\d\\d\\d-\\d";
    private static final String VIRASTOTUNNUS_PATTERN = "\\d\\d\\d\\d\\d\\d.*";
    @Autowired
    OrganisaatioValidationServiceImpl(@Value("${root.organisaatio.oid}") String rootOrganisaatioOid,
                                      OrganisaatioKoodisto organisaatioKoodisto) {
        this.rootOrganisaatioOid = rootOrganisaatioOid;
        this.organisaatioKoodisto = organisaatioKoodisto;
    }

    @Override
    public void validateOrganisation(Organisaatio model, String parentOid, Organisaatio parentOrg) {
        // Validointi: Tarkistetaan, että parent ei ole ryhmä
        if (parentOrg != null && OrganisaatioUtil.isRyhma(parentOrg)) {
            throw new ValidationException("validation.organisaatio.parent.can.not.be.ryhma");
        }

        // Validointi: Tarkistetaan, että ryhmää ei olla lisäämässä muulle kuin oph organisaatiolle
        if (OrganisaatioUtil.isRyhma(model) && !parentOid.equalsIgnoreCase(rootOrganisaatioOid)) {
            throw new ValidationException("validation.organisaatio.ryhma.parent.is.not.root");
        }

        // Validointi: Jos organisaatio on ryhmä, tarkistetaan ettei muita ryhmiä
        if (OrganisaatioUtil.isRyhma(model) && model.getTyypit().size() != 1) {
            throw new ValidationException("validation.organisaatio.ryhma.one.organisaatiotyyppi.allowed");
        }

        // Validointi: Jos y-tunnus on annettu, sen täytyy olla oikeassa muodossa
        if (model.getYtunnus() != null && model.getYtunnus().length() == 0) {
            model.setYtunnus(null);
        }
        if (model.getYtunnus() != null && !Pattern.matches(YTUNNUS_PATTERN, model.getYtunnus())) {
            throw new ValidationException("validation.organisaatio.ytunnus");
        }

        // Validointi: Jos virastotunnus on annettu, sen täytyy olla oikeassa muodossa
        if (model.getVirastoTunnus() != null && model.getVirastoTunnus().length() == 0) {
            model.setVirastoTunnus(null);
        }
        if (model.getVirastoTunnus() != null && !Pattern.matches(VIRASTOTUNNUS_PATTERN, model.getVirastoTunnus())) {
            throw new ValidationException("validation.organisaatio.virastotunnus");
        }

        List<Koodi> kieliKoodit = organisaatioKoodisto.haeKoodit(OrganisaatioKoodisto.KoodistoUri.KIELI, Optional.of(1), Optional.empty());
        KoodiPredicate kieliKoodiArvoPredicate = new KoodiPredicate(kieliKoodit, koodi -> koodi.getArvo().toLowerCase());
        KoodiPredicate kieliKoodiUriVersioPredicate = new KoodiPredicate(kieliKoodit, new KoodiToUriVersioMapper());

        // Validointi: Nimien kielien täytyy olla yksi tuetuista kielistä (muodossa "<koodiarvo>")
        if (model.getNimi() != null) {
            validateKoodi(model.getNimi(), kieliKoodiArvoPredicate, "nimi");
        }
        if (model.getNimet() != null) {
            model.getNimet().stream().filter(Objects::nonNull).forEach(nimi -> validateKoodi(nimi.getNimi(), kieliKoodiArvoPredicate, "nimet"));
        }

        // Validointi: Kuvauksen kielien täytyy olla kielikoodistosta (muodossa "<uri>#<versio>")
        if (model.getKuvaus2() != null) {
            validateKoodi(model.getKuvaus2(), kieliKoodiUriVersioPredicate, "kuvaus2");
        }

        // Validointi: Yhteystietojen kielien täytyy olla kielikoodistosta (muodossa "<uri>#<versio>")
        if (model.getYhteystiedot() != null) {
            model.getYhteystiedot().stream().filter(Objects::nonNull)
                    .forEach(yhteystieto -> validate(yhteystieto.getKieli(), kieliKoodiUriVersioPredicate, "yhteystiedot"));
        }
        if (model.getYhteystietoArvos() != null) {
            model.getYhteystietoArvos().stream().filter(Objects::nonNull)
                    .forEach(yhteystieto -> validate(yhteystieto.getKieli(), kieliKoodiUriVersioPredicate, "yhteystietoArvos"));
        }

        // This effectively blocks creating/updating VARHAISKASVATUKSEN_TOIMIPAIKKA from older apis since they don't
        // support this info
        boolean isVarhaiskasvatuksenToimipaikka = model.getTyypit().stream()
                .anyMatch(OrganisaatioTyyppi.VARHAISKASVATUKSEN_TOIMIPAIKKA.koodiValue()::equals);
        if (isVarhaiskasvatuksenToimipaikka) {
            this.validateVarhaiskasvatuksenToimipaikkaTiedot(model);
        }
        else {
            if (model.getVarhaiskasvatuksenToimipaikkaTiedot() != null) {
                throw new ValidationException("validation.Organisaatio.varhaiskasvatuksentoimipaikka.badorganisationtype");
            }
        }

        // Validointi: koodistoureissa pitää olla versiotieto
        this.checkVersionInKoodistoUris(model);
    }

    private void validateKoodi(MonikielinenTeksti monikielinenTeksti, Predicate<String> predicate, String path) {
        if (!monikielinenTeksti.getValues().keySet().stream().allMatch(predicate)) {
            throw new ValidationException(String.format("validation.Organisaatio.%s.kieli", path));
        }
    }

    private void validate(String valittuKieli, Predicate<String> predicate, String path) {
        if (!predicate.test(valittuKieli)) {
            throw new ValidationException(String.format("validation.Organisaatio.%s.kieli", path));
        }
    }

    private void validateVarhaiskasvatuksenToimipaikkaTiedot(Organisaatio model) {
        Stream.of(
                this.entry(Objects::nonNull,
                        "validation.varhaiskasvatuksentoimipaikka.null"),
                this.entry(toimipaikka -> Objects.nonNull(toimipaikka.getPaikkojenLukumaara()),
                        "validation.varhaiskasvatuksentoimipaikka.paikkojenlkm.null"),
                this.entry(toimipaikka -> Objects.nonNull(toimipaikka.getToimintamuoto()),
                        "validation.varhaiskasvatuksentoimipaikka.toimintamuoto.null"),
                this.entry(toimipaikka -> this.organisaatioKoodisto.haeVardaToimintamuoto().stream()
                                .anyMatch(koodi -> koodi.equals(toimipaikka.getToimintamuoto())),
                        "validation.varhaiskasvatuksentoimipaikka.toimintamuoto.invalidkoodi"),
                this.entry(toimipaikka -> this.organisaatioKoodisto.haeVardaKasvatusopillinenJarjestelma().stream()
                                .anyMatch(koodi -> koodi.equals(toimipaikka.getKasvatusopillinenJarjestelma())),
                        "validation.varhaiskasvatuksentoimipaikka.jarjestelma.invalidkoodi"),
                this.entry(toimipaikka -> Objects.isNull(toimipaikka.getVarhaiskasvatuksenToiminnallinenpainotukset())
                                || toimipaikka.getVarhaiskasvatuksenToiminnallinenpainotukset().stream()
                                .map(VarhaiskasvatuksenToiminnallinenpainotus::getAlkupvm)
                                .allMatch(Objects::nonNull),
                        "validation.varhaiskasvatuksentoimipaikka.toiminnallinenpainotus.alkupvm.null"),
                this.entry(toimipaikka -> Objects.isNull(toimipaikka.getVarhaiskasvatuksenToiminnallinenpainotukset())
                                || toimipaikka.getVarhaiskasvatuksenToiminnallinenpainotukset().stream()
                                .allMatch(painotus -> painotus.getLoppupvm() == null || painotus.getLoppupvm().after(painotus.getAlkupvm())),
                        "validation.varhaiskasvatuksentoimipaikka.toiminnallinenpainotus.loppupvm.invalid"),
                this.entry(toimipaikka -> Objects.isNull(toimipaikka.getVarhaiskasvatuksenToiminnallinenpainotukset())
                                || toimipaikka.getVarhaiskasvatuksenToiminnallinenpainotukset().stream()
                                .allMatch(toiminnallinenpainotus -> this.organisaatioKoodisto.haeVardaToiminnallinenPainotus().stream()
                                        .anyMatch(koodi -> koodi.equals(toiminnallinenpainotus.getToiminnallinenpainotus()))),
                        "validation.varhaiskasvatuksentoimipaikka.toiminnallinenpainotus.invalidkoodi"),
                this.entry(toimipaikka -> Objects.nonNull(toimipaikka.getVarhaiskasvatuksenJarjestamismuodot()),
                        "validation.varhaiskasvatuksentoimipaikka.jarjestamismuodot.null"),
                this.entry(toimipaikka -> toimipaikka.getVarhaiskasvatuksenJarjestamismuodot().size() > 0,
                        "validation.varhaiskasvatuksentoimipaikka.jarjestamismuodot.empty"),
                this.entry(toimipaikka -> toimipaikka.getVarhaiskasvatuksenJarjestamismuodot().stream()
                                .allMatch(jarjestamismuoto -> this.organisaatioKoodisto.haeVardaJarjestamismuoto().stream()
                                        .anyMatch(koodi -> koodi.equals(jarjestamismuoto))),
                        "validation.varhaiskasvatuksentoimipaikka.jarjestamismuodot.invalidkoodi"),
                this.entry(toimipaikka -> Objects.isNull(toimipaikka.getVarhaiskasvatuksenKielipainotukset())
                                || toimipaikka.getVarhaiskasvatuksenKielipainotukset().stream()
                                .map(VarhaiskasvatuksenKielipainotus::getAlkupvm)
                                .allMatch(Objects::nonNull),
                        "validation.varhaiskasvatuksentoimipaikka.kielipainotukset.alkupvm.null"),
                this.entry(toimipaikka -> Objects.isNull(toimipaikka.getVarhaiskasvatuksenKielipainotukset())
                                || toimipaikka.getVarhaiskasvatuksenKielipainotukset().stream()
                                .allMatch(painotus -> painotus.getLoppupvm() == null || painotus.getLoppupvm().after(painotus.getAlkupvm())),
                        "validation.varhaiskasvatuksentoimipaikka.kielipainotukset.loppupvm.invalid"),
                this.entry(toimipaikka -> Objects.isNull(toimipaikka.getVarhaiskasvatuksenKielipainotukset())
                                || toimipaikka.getVarhaiskasvatuksenKielipainotukset().stream()
                                .allMatch(kielipainotus -> this.organisaatioKoodisto.haeKielikoodit().stream()
                                        .anyMatch(koodi -> koodi.equals(kielipainotus.getKielipainotus()))),
                        "validation.varhaiskasvatuksentoimipaikka.kielipainotukset.invalidkoodi")
        ).forEachOrdered(validatorPair -> {
            if (!validatorPair.getValue().apply(model.getVarhaiskasvatuksenToimipaikkaTiedot())) {
                throw new ValidationException(validatorPair.getKey());
            }
        });
    }

    private Map.Entry<String, Function<VarhaiskasvatuksenToimipaikkaTiedot, Boolean>> entry(Function<VarhaiskasvatuksenToimipaikkaTiedot, Boolean> validator, String errorKey) {
        return new AbstractMap.SimpleEntry<>(errorKey, validator);
    }

    private void checkVersionInKoodistoUris(Organisaatio entity) {
        // kotipaikka

        // maa
        // metadata.hakutoimistonNimi
        // metadata.data
        // kielet
        for (String kieli : entity.getKielet()) {
            if (!kieli.matches(URI_WITH_VERSION_PATTERN)) {
                LOG.warn("Version missing from koodistouri! Organisaation kieli: " + kieli);
                throw new NoVersionInKoodistoUriException();
            }
        }

        // oppilaitostyyppi
        if (StringUtils.hasLength(entity.getOppilaitosTyyppi())) {
            if (!entity.getOppilaitosTyyppi().matches(URI_WITH_VERSION_PATTERN)) {
                LOG.warn("Version missing from koodistouri! Organisaation oppilaitostyyppi: " + entity.getOppilaitosTyyppi());
                throw new NoVersionInKoodistoUriException();
            }
        }

        // yhteystieto.postinumero
        // yhteystieto.kieli
        entity.getYhteystiedot().stream()
                .filter(yhteystieto -> !Objects.isNull(yhteystieto.getKieli()))
                .forEach(yhteystieto -> {
                    if (!yhteystieto.getKieli().matches(URI_WITH_VERSION_PATTERN)) {
                        LOG.warn("Version missing from koodistouri! Organisaation yhteystiedon kieli: " + yhteystieto.getKieli());
                        throw new NoVersionInKoodistoUriException();
                    }
                });

        // ryhmätyypit
        if (entity.getRyhmatyypit() != null) {
            List<String> errors = entity.getRyhmatyypit().stream().filter(t -> !t.matches(URI_WITH_VERSION_PATTERN)).collect(toList());
            if (!errors.isEmpty()) {
                LOG.warn("Version missing from koodistouri! Organisaation ryhmätyypit: {}", errors);
                throw new NoVersionInKoodistoUriException();
            }
        }

        // käyttöryhmät
        if (entity.getKayttoryhmat() != null) {
            List<String> errors = entity.getKayttoryhmat().stream().filter(t -> !t.matches(URI_WITH_VERSION_PATTERN)).collect(toList());
            if (!errors.isEmpty()) {
                LOG.warn("Version missing from koodistouri! Organisaation käyttöryhmät: {}", errors);
                throw new NoVersionInKoodistoUriException();
            }
        }
    }

    private static class KoodiPredicate implements Predicate<String> {

        private final List<String> sallitut;

        public KoodiPredicate(List<Koodi> koodit, Function<Koodi, String> koodiFunction) {
            this(koodit.stream().map(koodiFunction).collect(toList()));
        }

        public KoodiPredicate(List<String> sallitut) {
            this.sallitut = sallitut;
        }

        @Override
        public boolean test(String koodi) {
            return sallitut.stream().anyMatch(koodi::equals);
        }

    }

}
