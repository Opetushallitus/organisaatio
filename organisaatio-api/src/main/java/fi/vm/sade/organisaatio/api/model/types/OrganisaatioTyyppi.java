
package fi.vm.sade.organisaatio.api.model.types;

public enum OrganisaatioTyyppi {
    KOULUTUSTOIMIJA("Koulutustoimija"),
    OPPILAITOS("Oppilaitos"),
    TOIMIPISTE("Toimipiste"),
    OPPISOPIMUSTOIMIPISTE("Oppisopimustoimipiste"),
    MUU_ORGANISAATIO("Muu organisaatio"),
    RYHMA("Ryhma"),
    VARHAISKASVATUKSEN_JARJESTAJA("Varhaiskasvatuksen jarjestaja"),
    TYOELAMAJARJESTO("Tyoelamajarjesto");
    
    private final String value;

    OrganisaatioTyyppi(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static OrganisaatioTyyppi fromValue(String v) {
        for (OrganisaatioTyyppi c: OrganisaatioTyyppi.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
