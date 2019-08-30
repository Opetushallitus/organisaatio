package fi.vm.sade.varda.rekisterointi.model;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.data.annotation.Id;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import java.util.Set;

public class Rekisterointi {

    @Id
    public Long id;
    @NotNull
    public ObjectNode organisaatio;
    public Set<@Email String> sahkopostit;
    @NotNull
    public String toimintamuoto;
    @NotNull
    @Valid
    public Kayttaja kayttaja;

}
