package fi.vm.sade.varda.rekisterointi.model;

import lombok.AllArgsConstructor;
import lombok.With;
import org.springframework.data.relational.core.mapping.Embedded;

import jakarta.validation.constraints.NotNull;

@AllArgsConstructor(staticName = "of")
public class Yhteystiedot {

    @NotNull
    public final String puhelinnumero;
    @NotNull
    public final String sahkoposti;
    @With @Embedded(prefix = "posti_", onEmpty = Embedded.OnEmpty.USE_EMPTY)
    public final Osoite postiosoite;
    @With @Embedded(prefix = "kaynti_", onEmpty = Embedded.OnEmpty.USE_EMPTY)
    public final Osoite kayntiosoite;

}
