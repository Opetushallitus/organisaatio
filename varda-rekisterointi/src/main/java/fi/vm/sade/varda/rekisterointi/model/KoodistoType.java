package fi.vm.sade.varda.rekisterointi.model;

import java.util.Optional;

public enum KoodistoType {
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
