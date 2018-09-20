
package fi.vm.sade.organisaatio.api.model.types;

import java.util.List;
import java.util.stream.Collectors;

public enum OrganisaatioTyyppi {
    KOULUTUSTOIMIJA("Koulutustoimija", "organisaatiotyyppi_01"),
    OPPILAITOS("Oppilaitos", "organisaatiotyyppi_02"),
    TOIMIPISTE("Toimipiste", "organisaatiotyyppi_03"),
    OPPISOPIMUSTOIMIPISTE("Oppisopimustoimipiste", "organisaatiotyyppi_04"),
    MUU_ORGANISAATIO("Muu organisaatio", "organisaatiotyyppi_05"),
    RYHMA("Ryhma", "Ryhma"),
    VARHAISKASVATUKSEN_JARJESTAJA("Varhaiskasvatuksen jarjestaja", "organisaatiotyyppi_07"),
    TYOELAMAJARJESTO("Tyoelamajarjesto", "organisaatiotyyppi_06")
    ;

    private final String value;
    private final String koodiValue;

    OrganisaatioTyyppi(String value, String koodiValue) {
        this.value = value;
        this.koodiValue = koodiValue;
    }

    public String value() {
        return this.value;
    }

    public String koodiValue() {
        return this.koodiValue;
    }

    public static OrganisaatioTyyppi fromValue(String value) {
        for (OrganisaatioTyyppi tyyppi: OrganisaatioTyyppi.values()) {
            if (tyyppi.value.equals(value)) {
                return tyyppi;
            }
        }
        throw new IllegalArgumentException(value);
    }

    public static OrganisaatioTyyppi fromKoodiValue(String value) {
        for (OrganisaatioTyyppi tyyppi: OrganisaatioTyyppi.values()) {
            if (tyyppi.koodiValue.equals(value)) {
                return tyyppi;
            }
        }
        throw new IllegalArgumentException(value);
    }

    /**
     * Kääntää organisaatiotyypin koodiarvon vanhaan organisaatiotyypin muotoon.
     * @param source Organisaatiotyyppilista koodiarvoja
     * @return Organisaatiotyyppilista vanhassa muodossa.
     */
    public static List<String> fromKoodiToValue(List<String> source) {
        return source == null
                ? null
                : source.stream()
                .map(OrganisaatioTyyppi::fromKoodiValue)
                .map(OrganisaatioTyyppi::value)
                .collect(Collectors.toList());
    }

    /**
     * Kääntää organisaatiotyypin vanhasta organisaatiotyypin muodosta koodiarvoon.
     * @param source Organisaatiotyyppilista vanhassa muodossa.
     * @return Organisaatiotyyppilista koodiarvoja
     */
    public static List<String> fromValueToKoodi(List<String> source) {
        return source == null
                ? null
                : source.stream()
                .map(OrganisaatioTyyppi::fromValue)
                .map(OrganisaatioTyyppi::koodiValue)
                .collect(Collectors.toList());
    }

    /**
     * Muuttaa organisaation tyypit koodistoarvoista organisaatiopalvelun vanhaan tyyppiin.
     * @param tyypitAsKoodi Organisaatiotyypit koodiarvoina
     * @return Organisaatiotyypit organisaatiopalvelun vanhassa muodossa
     */
    public static List<String> tyypitFromKoodis(List<String> tyypitAsKoodi) {
        return tyypitAsKoodi.stream()
                .map(tyyppi -> OrganisaatioTyyppi.fromKoodiValue(tyyppi).value())
                .collect(Collectors.toList());
    }

    /**
     * Muuttaa organisaation tyypit organisaatiopalvelun vanhasta tyypistä koodistoarvoiksi.
     * @param tyypit Organisaatiotyypit organisaatiopalvelun vanhassa muodossa
     * @return Organisaatiotyypit koodiarvoina
     */
    public static List<String> tyypitToKoodis(List<String> tyypit) {
        return tyypit.stream()
                .map(tyyppi -> OrganisaatioTyyppi.fromValue(tyyppi).koodiValue())
                .collect(Collectors.toList());
    }

}
