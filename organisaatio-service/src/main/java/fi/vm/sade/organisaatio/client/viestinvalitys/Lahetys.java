package fi.vm.sade.organisaatio.client.viestinvalitys;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Lahetys {
    @NonNull
    private String otsikko;
    @NonNull
    private String lahettavaPalvelu;
    private String lahettavanVirkailijanOid;
    @NonNull
    private Lahettaja lahettaja;
    private String replyTo;
    @NonNull
    private Prioriteetti prioriteetti;
    @NonNull
    private Integer sailytysaika;
    private List<KayttooikeusRajoitukset> kayttooikeusRajoitukset;
}