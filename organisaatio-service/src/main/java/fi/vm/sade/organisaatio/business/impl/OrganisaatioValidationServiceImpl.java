package fi.vm.sade.organisaatio.business.impl;

import fi.vm.sade.organisaatio.api.OrganisaatioValidationConstraints;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.business.OrganisaatioKoodisto;
import fi.vm.sade.organisaatio.business.OrganisaatioValidationService;
import fi.vm.sade.organisaatio.business.exception.NoVersionInKoodistoUriException;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.model.VarhaiskasvatuksenToimipaikkaTiedot;
import fi.vm.sade.organisaatio.service.util.OrganisaatioUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.validation.ValidationException;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Service
public class OrganisaatioValidationServiceImpl implements OrganisaatioValidationService {
    private final Logger LOG = LoggerFactory.getLogger(getClass());

    private final String rootOrganisaatioOid;

    private final OrganisaatioKoodisto organisaatioKoodisto;

    private static final String uriWithVersionRegExp = "^.*#[0-9]+$";

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
            throw new ValidationException("Parent cannot be group");
        }

        // Validointi: Tarkistetaan, että ryhmää ei olla lisäämässä muulle kuin oph organisaatiolle
        if (OrganisaatioUtil.isRyhma(model) && !parentOid.equalsIgnoreCase(rootOrganisaatioOid)) {
            throw new ValidationException("Ryhmiä ei voi luoda muille kuin oph organisaatiolle");
        }

        // Validointi: Jos organisaatio on ryhmä, tarkistetaan ettei muita ryhmiä
        if (OrganisaatioUtil.isRyhma(model) && model.getTyypit().size() != 1) {
            throw new ValidationException("Rymällä ei voi olla muita tyyppejä");
        }

        // Validointi: Jos y-tunnus on annettu, sen täytyy olla oikeassa muodossa
        if (model.getYtunnus() != null && model.getYtunnus().length() == 0) {
            model.setYtunnus(null);
        }
        if (model.getYtunnus() != null && !Pattern.matches(OrganisaatioValidationConstraints.YTUNNUS_PATTERN, model.getYtunnus())) {
            throw new ValidationException("validation.Organisaatio.ytunnus");
        }

        // Validointi: Jos virastotunnus on annettu, sen täytyy olla oikeassa muodossa
        if (model.getVirastoTunnus() != null && model.getVirastoTunnus().length() == 0) {
            model.setVirastoTunnus(null);
        }
        if (model.getVirastoTunnus() != null && !Pattern.matches(OrganisaatioValidationConstraints.VIRASTOTUNNUS_PATTERN, model.getVirastoTunnus())) {
            throw new ValidationException("validation.Organisaatio.virastotunnus");
        }


        // This effectively blocks creating/updating VARHAISKASVATUKSEN_TOIMIPAIKKA from older apis since they don't
        // support this info
        boolean isVarhaiskasvatuksenToimipaikka = model.getTyypit().stream()
                .anyMatch(OrganisaatioTyyppi.VARHAISKASVATUKSEN_TOIMIPAIKKA.koodiValue()::equals);
        if (isVarhaiskasvatuksenToimipaikka) {
            Stream.of(
                    this.entry(Objects::nonNull,
                            "validation.varhaiskasvatuksentoimipaikka.null"),
                    this.entry(toimipaikka -> Objects.nonNull(toimipaikka.getPaikkojenLukumaara()),
                            "validation.varhaiskasvatuksentoimipaikka.paikkojenlkm.null"),
                    this.entry(toimipaikka -> this.organisaatioKoodisto.haeVardaJarjestamismuoto().stream()
                                    .anyMatch(koodi -> koodi.equals(toimipaikka.getJarjestamismuoto())),
                            "validation.varhaiskasvatuksentoimipaikka.jarjestamismuoto.invalidkoodi"),
                    this.entry(toimipaikka -> this.organisaatioKoodisto.haeVardaKasvatusopillinenJarjestelma().stream()
                                    .anyMatch(koodi -> koodi.equals(toimipaikka.getKasvatusopillinenJarjestelma())),
                            "validation.varhaiskasvatuksentoimipaikka.jarjestelma.invalidkoodi"),
                    this.entry(toimipaikka -> this.organisaatioKoodisto.haeVardaToiminnallinenPainotus().stream()
                                    .anyMatch(koodi -> koodi.equals(toimipaikka.getToiminnallinenPainotus())),
                            "validation.varhaiskasvatuksentoimipaikka.painotus.invalidkoodi"),
                    this.entry(toimipaikka -> Objects.nonNull(toimipaikka.getVarhaiskasvatuksenToimintamuodot()),
                            "validation.varhaiskasvatuksentoimipaikka.toimintamuodot.null"),
                    this.entry(toimipaikka -> toimipaikka.getVarhaiskasvatuksenToimintamuodot().size() > 0,
                            "validation.varhaiskasvatuksentoimipaikka.toimintamuodot.empty"),
                    this.entry(toimipaikka -> toimipaikka.getVarhaiskasvatuksenToimintamuodot().stream()
                                    .allMatch(toimintamuoto -> this.organisaatioKoodisto.haeVardaToimintamuoto().stream()
                                            .anyMatch(koodi -> koodi.equals(toimintamuoto.getToimintamuoto()))),
                            "validation.varhaiskasvatuksentoimipaikka.toimintamuodot.invalidkoodi"),
                    this.entry(toimipaikka -> Objects.nonNull(toimipaikka.getVarhaiskasvatuksenKielipainotukset()),
                            "validation.varhaiskasvatuksentoimipaikka.kielipainotukset.null"),
                    this.entry(toimipaikka -> toimipaikka.getVarhaiskasvatuksenKielipainotukset().size() > 0,
                            "validation.varhaiskasvatuksentoimipaikka.kielipainotukset.empty"),
                    this.entry(toimipaikka -> toimipaikka.getVarhaiskasvatuksenKielipainotukset().stream()
                                    .allMatch(kielipainotus -> this.organisaatioKoodisto.haeKielikoodit().stream()
                                            .anyMatch(koodi -> koodi.equals(kielipainotus.getKielipainotus()))),
                            "validation.varhaiskasvatuksentoimipaikka.kielipainotukset.invalidkoodi")
            ).forEachOrdered(validatorPair -> {
                if (!validatorPair.getValue().apply(model.getVarhaiskasvatuksenToimipaikkaTiedot())) {
                    throw new ValidationException(validatorPair.getKey());
                }
            });
        }
        else {
            if (model.getVarhaiskasvatuksenToimipaikkaTiedot() != null) {
                throw new ValidationException("validation.Organisaatio.varhaiskasvatuksentoimipaikka.badorganisationtype");
            }
        }

        // Validointi: koodistoureissa pitää olla versiotieto
        this.checkVersionInKoodistoUris(model);
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
            if (!kieli.matches(uriWithVersionRegExp)) {
                LOG.warn("Version missing from koodistouri! Organisaation kieli: " + kieli);
                throw new NoVersionInKoodistoUriException();
            }
        }

        // oppilaitostyyppi
        if (StringUtils.hasLength(entity.getOppilaitosTyyppi())) {
            if (!entity.getOppilaitosTyyppi().matches(uriWithVersionRegExp)) {
                LOG.warn("Version missing from koodistouri! Organisaation oppilaitostyyppi: " + entity.getOppilaitosTyyppi());
                throw new NoVersionInKoodistoUriException();
            }
        }

        // yhteystieto.postinumero
        // yhteystieto.kieli
        for (int i = 0; i < entity.getYhteystiedot().size(); ++i) {
            if (entity.getYhteystiedot().get(i).getKieli() != null) {
                if (!entity.getYhteystiedot().get(i).getKieli().matches(uriWithVersionRegExp)) {
                    LOG.warn("Version missing from koodistouri! Organisaation yhteystiedon kieli: " + entity.getYhteystiedot().get(i).getKieli());
                    throw new NoVersionInKoodistoUriException();
                }
            }
        }

        // ryhmätyypit
        if (entity.getRyhmatyypit() != null) {
            List<String> errors = entity.getRyhmatyypit().stream().filter(t -> !t.matches(uriWithVersionRegExp)).collect(toList());
            if (!errors.isEmpty()) {
                LOG.warn("Version missing from koodistouri! Organisaation ryhmätyypit: {}", errors);
                throw new NoVersionInKoodistoUriException();
            }
        }

        // käyttöryhmät
        if (entity.getKayttoryhmat() != null) {
            List<String> errors = entity.getKayttoryhmat().stream().filter(t -> !t.matches(uriWithVersionRegExp)).collect(toList());
            if (!errors.isEmpty()) {
                LOG.warn("Version missing from koodistouri! Organisaation käyttöryhmät: {}", errors);
                throw new NoVersionInKoodistoUriException();
            }
        }
    }

}
