package fi.vm.sade.tarjonta.service.types;

public enum KoulutusmoduuliTyyppi {

    TUTKINNON_OSA,
    TUTKINTO,
    TUTKINTO_OHJELMA,
    OPINTOKOKONAISUUS,
    OPINTOJAKSO;

    public String value() {
        return name();
    }

    public static KoulutusmoduuliTyyppi fromValue(String v) {
        return valueOf(v);
    }
}
