package fi.vm.sade.tarjonta.service.types;

public enum TarjontaTila {

    POISTETTU,
    LUONNOS,
    VALMIS,
    JULKAISTU,
    PERUTTU,
    KOPIOITU,
    PUUTTEELLINEN;

    public String value() {
        return name();
    }

    public static TarjontaTila fromValue(String v) {
        return valueOf(v);
    }
}
