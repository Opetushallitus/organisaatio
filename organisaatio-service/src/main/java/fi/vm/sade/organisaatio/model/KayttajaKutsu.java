package fi.vm.sade.organisaatio.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;
import java.util.Set;

@AllArgsConstructor(staticName = "of")
@Builder
public class KayttajaKutsu {
    public final String kutsujaOid;
    public final String kutsujaForEmail;
    @NotEmpty
    public final String etunimi;
    @NotEmpty
    public final String sukunimi;
    @NotNull
    public final String sahkoposti;
    @NotNull
    public final String asiointikieli;
    public final String saate;
    @Valid @NotNull
    public final Set<KutsuOrganisaatio> organisaatiot;

    @AllArgsConstructor(staticName = "of")
    public static class KutsuOrganisaatio {
        @NotNull
        public final String organisaatioOid;
        @Valid
        @NotNull
        public final Set<KutsuKayttooikeusRyhma> kayttoOikeusRyhmat;
        @FutureOrPresent
        public final LocalDate voimassaLoppuPvm;
    }

    @AllArgsConstructor(staticName = "of")
    public static class KutsuKayttooikeusRyhma {
        @NotNull
        public final Long id;
    }
}
