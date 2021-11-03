package fi.vm.sade.organisaatio.resource.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;
import java.util.Set;

@Schema(description = "Yhteystiedon tyyppi")
public class YhteystietojenTyyppiRDTO {

    private Map<String, String> nimi;
    private Set<String> sovellettavatOrganisaatioTyyppis;
    private Set<String> sovellettavatOppilaitosTyyppis;
    private Set<Map<String, String>> lisatietos;

    public Map<String, String> getNimi() {
        return nimi;
    }

    public void setNimi(Map<String, String> nimi) {
        this.nimi = nimi;
    }

    public Set<String> getSovellettavatOrganisaatioTyyppis() {
        return sovellettavatOrganisaatioTyyppis;
    }

    public void setSovellettavatOrganisaatioTyyppis(Set<String> sovellettavatOrganisaatioTyyppis) {
        this.sovellettavatOrganisaatioTyyppis = sovellettavatOrganisaatioTyyppis;
    }

    public Set<String> getSovellettavatOppilaitosTyyppis() {
        return sovellettavatOppilaitosTyyppis;
    }

    public void setSovellettavatOppilaitosTyyppis(Set<String> sovellettavatOppilaitosTyyppis) {
        this.sovellettavatOppilaitosTyyppis = sovellettavatOppilaitosTyyppis;
    }

    public Set<Map<String, String>> getLisatietos() {
        return lisatietos;
    }

    public void setLisatietos(Set<Map<String, String>> lisatietos) {
        this.lisatietos = lisatietos;
    }


}
