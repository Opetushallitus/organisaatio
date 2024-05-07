package fi.vm.sade.organisaatio.client.viestinvalitys;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Builder
@Data
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
}