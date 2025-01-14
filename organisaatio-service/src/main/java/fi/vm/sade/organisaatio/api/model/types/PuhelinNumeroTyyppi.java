
package fi.vm.sade.organisaatio.api.model.types;

public enum PuhelinNumeroTyyppi {
    PUHELIN("puhelin");

    private final String value;

    PuhelinNumeroTyyppi(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static PuhelinNumeroTyyppi fromValue(String v) {
        for (PuhelinNumeroTyyppi c: PuhelinNumeroTyyppi.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
