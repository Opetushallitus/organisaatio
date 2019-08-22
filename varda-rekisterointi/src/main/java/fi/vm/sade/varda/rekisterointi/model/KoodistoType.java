package fi.vm.sade.varda.rekisterointi.model;

import java.util.Optional;

public enum KoodistoType {
    ORGANISAATIOTYYPPI("organisaatiotyyppi"),
    KUNTA("kunta"),
    MAAT_JA_VALTIOT_1("maatjavaltiot1"),
    POSTI("posti"),
    VARDA_TOIMINTAMUOTO("vardatoimintamuoto");

    public final String uri;
    public final Optional<Integer> versio;
    public final Optional<Boolean> onlyValid;

    KoodistoType(String uri) {
        this(uri, Optional.empty(), Optional.empty());
    }

    KoodistoType(String uri, Optional<Integer> versio, Optional<Boolean> onlyValid) {
        this.uri = uri;
        this.versio = versio;
        this.onlyValid = onlyValid;
    }
}
