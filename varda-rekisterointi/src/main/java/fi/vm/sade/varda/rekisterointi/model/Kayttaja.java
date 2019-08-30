package fi.vm.sade.varda.rekisterointi.model;

import org.springframework.data.annotation.Id;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

public class Kayttaja {

    @Id
    public Long id;
    @NotNull
    public String etunimi;
    @NotNull
    public String sukunimi;
    @NotNull
    @Email
    public String sahkoposti;
    @NotNull
    public String asiointikieli;
    public String saateteksti;

}
