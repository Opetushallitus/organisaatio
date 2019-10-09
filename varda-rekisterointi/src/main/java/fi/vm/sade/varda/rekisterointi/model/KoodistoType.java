package fi.vm.sade.varda.rekisterointi.model;

public enum KoodistoType {
    ORGANISAATIOTYYPPI("organisaatiotyyppi"),
    KUNTA("kunta"),
    MAAT_JA_VALTIOT_1("maatjavaltiot1"),
    POSTI("posti"),
    VARDA_TOIMINTAMUOTO("vardatoimintamuoto");

    public final String uri;

    KoodistoType(String uri) {
        this.uri = uri;
    }
}
