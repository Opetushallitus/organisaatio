package fi.vm.sade.varda.rekisterointi.model;

import lombok.Value;

import jakarta.validation.constraints.NotNull;

@Value
public class PaatosDto {

    @NotNull
    public final Long rekisterointi;
    public final boolean hyvaksytty;
    public final String perustelu;

}
