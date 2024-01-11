package fi.vm.sade.organisaatio.client.viestinvalitys;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.util.List;
import java.util.Map;

@Builder
@Data
public class Viesti {
    private String lahetysTunniste;
    @NonNull
    private Lahettaja lahettaja;
    private String lahettavanVirkailijanOid;
    private String replyTo;
    @NonNull
    private List<Vastaanottaja> vastaanottajat;
    @NonNull
    private String otsikko;
    @NonNull
    private SisallonTyyppi sisallonTyyppi;
    @NonNull
    private String sisalto;
    private List<String> liitteidenTunnisteet;
    private String lahettavaPalvelu;
    @NonNull
    private Prioriteetti prioriteetti;
    @NonNull
    private Integer sailytysaika;
    private List<String> kayttooikeusRajoitukset;
    private Map<String, List<String>> metadata;
}
