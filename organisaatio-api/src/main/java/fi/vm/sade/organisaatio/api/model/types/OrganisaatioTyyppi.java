
package fi.vm.sade.organisaatio.api.model.types;

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

}
