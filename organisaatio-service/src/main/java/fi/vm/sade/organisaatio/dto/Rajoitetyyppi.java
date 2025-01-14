package fi.vm.sade.organisaatio.dto;

public enum Rajoitetyyppi {
    OPPILAITOSTYYPPI(CONSTANTS.OPPILAITOSTYYPPI_CONSTANT),
    ORGANISAATIOTYYPPI(CONSTANTS.ORGANISAATIOTYYPPI_CONSTANT)
    ;

    private final String rajoitetyyppi;

    Rajoitetyyppi(final String rajoitetyyppi) {
        this.rajoitetyyppi = rajoitetyyppi;
    }

    public String getValue() {
        return this.rajoitetyyppi;
    }

    // This allows these values to be used in annotations.
    public static class CONSTANTS {
        public static final String OPPILAITOSTYYPPI_CONSTANT = "oppilaitostyyppi";
        public static final String ORGANISAATIOTYYPPI_CONSTANT = "organisaatiotyyppi";
    }

}
