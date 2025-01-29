package fi.vm.sade.rekisterointi.model;

public enum KoodistoType {
    YRITYSMUOTO("yritysmuoto"),
    ORGANISAATIOTYYPPI("organisaatiotyyppi"),
    KUNTA("kunta"),
    MAAT_JA_VALTIOT_1("maatjavaltiot1"),
    POSTI("posti");

    public final String uri;

    KoodistoType(String uri) {
        this.uri = uri;
    }
}
