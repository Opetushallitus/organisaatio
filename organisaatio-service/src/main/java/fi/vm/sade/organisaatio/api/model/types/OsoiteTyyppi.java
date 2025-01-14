
package fi.vm.sade.organisaatio.api.model.types;

public enum OsoiteTyyppi {
    POSTI("posti"),
    KAYNTI("kaynti"),
    RUOTSI_POSTI("ruotsi_posti"),
    RUOTSI_KAYNTI("ruotsi_kaynti"),
    ULKOMAINEN_POSTI("ulkomainen_posti"),
    ULKOMAINEN_KAYNTI("ulkomainen_kaynti"),
    MUU("muu");
    
    private final String value;

    OsoiteTyyppi(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static OsoiteTyyppi fromValue(String v) {
        for (OsoiteTyyppi c: OsoiteTyyppi.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
