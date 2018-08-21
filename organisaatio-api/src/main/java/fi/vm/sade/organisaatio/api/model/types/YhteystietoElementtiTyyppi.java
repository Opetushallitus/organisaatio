
package fi.vm.sade.organisaatio.api.model.types;

public enum YhteystietoElementtiTyyppi {
    TEKSTI("Teksti"),
    PUHELIN("Puhelin"),
    NIMI("Nimi"),
    NIMIKE("Nimike"),
    OSOITE("Osoite"),
    OSOITE_ULKOMAA("Osoite ulkomaa"),
    WWW("Www"),
    EMAIL("Email");
    
    private final String value;

    YhteystietoElementtiTyyppi(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static YhteystietoElementtiTyyppi fromValue(String v) {
        for (YhteystietoElementtiTyyppi c: YhteystietoElementtiTyyppi.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
