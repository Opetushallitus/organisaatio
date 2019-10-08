package fi.vm.sade.varda.rekisterointi.model;

import lombok.Builder;
import lombok.Value;
import lombok.With;
import org.springframework.data.annotation.Id;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

@Value(staticConstructor = "of")
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
