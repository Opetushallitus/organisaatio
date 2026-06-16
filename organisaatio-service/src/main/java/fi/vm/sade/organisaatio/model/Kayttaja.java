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
    @Schema(description = "Pääkäyttäjän etunimi", requiredMode = Schema.RequiredMode.REQUIRED)
    public String etunimi;

    @NotNull
    @Schema(description = "Pääkäyttäjän sukunimi", requiredMode = Schema.RequiredMode.REQUIRED)
    public String sukunimi;

    @NotNull
    @Schema(description = "Pääkäyttäjän sähköpostiosoite", requiredMode = Schema.RequiredMode.REQUIRED)
    public String sahkoposti;

    @NotNull
    @Schema(description = "Pääkäyttäjän asiointikieli", requiredMode = Schema.RequiredMode.REQUIRED)
    public Asiointikieli asiointikieli;
}