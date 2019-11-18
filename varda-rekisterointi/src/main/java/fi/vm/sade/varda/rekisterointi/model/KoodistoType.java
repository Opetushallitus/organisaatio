package fi.vm.sade.varda.rekisterointi.model;

public enum KoodistoType {
    YRITYSMUOTO("yritysmuoto"),
    ORGANISAATIOTYYPPI("organisaatiotyyppi"),
    KUNTA("kunta"),
    MAAT_JA_VALTIOT_1("maatjavaltiot1"),
    POSTI("posti"),
    VARDA_TOIMINTAMUOTO("vardatoimintamuoto"),
    OPETUSKIELI("oppilaitoksenopetuskieli");

    public final String uri;

    KoodistoType(String uri) {
        this.uri = uri;
    }
}
