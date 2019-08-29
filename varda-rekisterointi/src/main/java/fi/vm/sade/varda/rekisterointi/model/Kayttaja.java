package fi.vm.sade.varda.rekisterointi.model;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

public class Kayttaja {

    @NotNull
    public String etunimi;
    @NotNull
    public String sukunimi;
    @NotNull
    @Email
    public String sahkoposti;
    @NotNull
    public String asiointikieli;

}
