package fi.vm.sade.varda.rekisterointi.model;

import org.springframework.data.annotation.Id;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import java.util.Objects;

public class Kayttaja {

    @Id
    public final Long id;
    @NotNull
    public final String etunimi;
    @NotNull
    public final String sukunimi;
    @NotNull
    @Email
    public final String sahkoposti;
    @NotNull
    public final String asiointikieli;
    public final String saateteksti;

    public Kayttaja(Long id, String etunimi, String sukunimi, String sahkoposti, String asiointikieli, String saateteksti) {
        this.id = id;
        this.etunimi = etunimi;
        this.sukunimi = sukunimi;
        this.sahkoposti = sahkoposti;
        this.asiointikieli = asiointikieli;
        this.saateteksti = saateteksti;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        static final String PUUTTUVA_KENTTA_VIESTI = "Puuttuva pakollinen kenttä: %s";
        private Long id;
        private String etunimi;
        private String sukunimi;
        private String sahkoposti;
        private String asiointikieli;
        private String saateteksti;

        private Builder() {}

        public Builder etunimi(String etunimi) {
            this.etunimi = etunimi;
            return this;
        }

        public Builder sukunimi(String sukunimi) {
            this.sukunimi = sukunimi;
            return this;
        }

        public Builder sahkoposti(String sahkoposti) {
            this.sahkoposti = sahkoposti;
            return this;
        }

        public Builder asiointikieli(String asiointikieli) {
            this.asiointikieli = asiointikieli;
            return this;
        }

        public Builder saateteksti(String saateteksti) {
            this.saateteksti = saateteksti;
            return this;
        }

        public Kayttaja build() {
            return new Kayttaja(
                    id,
                    Objects.requireNonNull(etunimi, String.format(PUUTTUVA_KENTTA_VIESTI, "etunimi")),
                    Objects.requireNonNull(sukunimi, String.format(PUUTTUVA_KENTTA_VIESTI, "sukunimi")),
                    Objects.requireNonNull(sahkoposti, String.format(PUUTTUVA_KENTTA_VIESTI, "sähköposti")),
                    Objects.requireNonNull(asiointikieli, String.format(PUUTTUVA_KENTTA_VIESTI, "asiointikieli")),
                    saateteksti);
        }
    }

}
