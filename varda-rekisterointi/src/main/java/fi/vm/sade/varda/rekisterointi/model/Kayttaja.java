package fi.vm.sade.varda.rekisterointi.model;

import lombok.*;
import org.springframework.data.annotation.Id;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

@EqualsAndHashCode
@AllArgsConstructor(staticName = "of")
@Builder
public class Kayttaja {

    @With @Id
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

}
