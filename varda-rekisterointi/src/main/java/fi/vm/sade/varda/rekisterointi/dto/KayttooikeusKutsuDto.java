package fi.vm.sade.varda.rekisterointi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Set;

@EqualsAndHashCode
@AllArgsConstructor(staticName = "of")
@Builder
public class KayttooikeusKutsuDto {

    public final String kutsujaOid;

    public final String kutsujaForEmail;

    @NotEmpty
    public final String etunimi;
    @NotEmpty
    public final String sukunimi;
    @NotNull @Email
    public final String sahkoposti;
    @NotNull
    public final String asiointikieli;

    public final String saate;
    @Valid @NotNull
    public final Set<KutsuOrganisaatioDto> organisaatiot;

    @EqualsAndHashCode
    @AllArgsConstructor(staticName = "of")
    public static class KutsuOrganisaatioDto {
        @NotNull
        public final String organisaatioOid;
        @Valid
        @NotNull
        public final Set<KutsuKayttooikeusRyhmaDto> kayttoOikeusRyhmat;
        @FutureOrPresent
        public final LocalDate voimassaLoppuPvm;
    }

    @EqualsAndHashCode
    @AllArgsConstructor(staticName = "of")
    public static class KutsuKayttooikeusRyhmaDto {
        @NotNull
        public final Long id;
    }
}
