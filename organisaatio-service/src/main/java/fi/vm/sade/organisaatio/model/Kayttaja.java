package fi.vm.sade.organisaatio.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Kayttaja {
    @NotNull
    @Schema(description = "Pääkäyttäjän etunimi", required = true)
    public String etunimi;

    @NotNull
    @Schema(description = "Pääkäyttäjän sukunimi", required = true)
    public String sukunimi;

    @NotNull
    @Schema(description = "Pääkäyttäjän sähköpostiosoite", required = true)
    public String sahkoposti;

    @NotNull
    @Schema(description = "Pääkäyttäjän asiointikieli", required = true)
    public String asiointikieli;
}