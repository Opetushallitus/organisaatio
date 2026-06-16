package fi.vm.sade.organisaatio.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Varda-organisaation rekisteröinti")
public class VardaRekisterointi {
    @NotNull
    @Schema(description = "Y-tunnus", requiredMode = Schema.RequiredMode.REQUIRED)
    private String ytunnus;

    @NotNull
    @Schema(description = "Pääkäyttäjän tiedot", requiredMode = Schema.RequiredMode.REQUIRED)
    private Kayttaja paakayttaja;
}