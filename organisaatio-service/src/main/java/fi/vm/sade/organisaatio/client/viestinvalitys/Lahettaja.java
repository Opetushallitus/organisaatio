package fi.vm.sade.organisaatio.client.viestinvalitys;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
public class Lahettaja {
    private String nimi;
    @NonNull
    private String sahkopostiOsoite;
}
