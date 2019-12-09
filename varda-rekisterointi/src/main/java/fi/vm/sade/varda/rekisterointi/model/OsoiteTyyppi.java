package fi.vm.sade.varda.rekisterointi.model;

public enum OsoiteTyyppi {
    POSTI("posti"),
    KAYNTI("kaynti");

    private final String tyyppi;

    OsoiteTyyppi(String tyyppi) {
        this.tyyppi = tyyppi;
    }

    public String value() {
        return tyyppi;
    }

    public static OsoiteTyyppi of(String value) {
        if (POSTI.tyyppi.equals(value)) {
            return POSTI;
        } else if (KAYNTI.tyyppi.equals(value)) {
            return KAYNTI;
        }
        throw new IllegalArgumentException("Tuntematon osoitetyyppi: " + value);
    }
}
