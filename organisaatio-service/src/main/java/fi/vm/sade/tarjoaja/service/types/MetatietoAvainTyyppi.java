
package fi.vm.sade.tarjoaja.service.types;

public enum MetatietoAvainTyyppi {
    UNKNOWN("Unknown"),
    ESTEETTOMYYS_PALVELUT("EsteettomyysPalvelut"),
    OPPIMISYMPARISTOT("Oppimisymparistot"),
    KUSTANNUKSET("Kustannukset"),
    RUOKAILU("Ruokailu"),
    TERVEYDENHUOLTO("Terveydenhuolto");
    
    private final String value;

    MetatietoAvainTyyppi(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static MetatietoAvainTyyppi fromValue(String v) {
        for (MetatietoAvainTyyppi c: MetatietoAvainTyyppi.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
