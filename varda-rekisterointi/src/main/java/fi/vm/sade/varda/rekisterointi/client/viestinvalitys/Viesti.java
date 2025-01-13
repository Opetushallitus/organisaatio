package fi.vm.sade.varda.rekisterointi.client.viestinvalitys;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.List;
import java.util.Map;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Viesti {
    private String lahetysTunniste;
    private Lahettaja lahettaja;
    private String lahettavanVirkailijanOid;
    private String replyTo;
    @NonNull
    private List<Vastaanottaja> vastaanottajat;
    private String otsikko;
    @NonNull
    private SisallonTyyppi sisallonTyyppi;
    @NonNull
    private String sisalto;
    private List<String> liitteidenTunnisteet;
    private String lahettavaPalvelu;
    private Prioriteetti prioriteetti;
    private Integer sailytysaika;
    private List<KayttooikeusRajoitukset> kayttooikeusRajoitukset;
    private Map<String, List<String>> metadata;
    private String idempotencyKey;
}
