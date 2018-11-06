package fi.vm.sade.organisaatio.dto;

import java.util.Collection;
import java.util.Map;

public class VirkailijaCriteria {

    private Boolean passivoitu;
    private Boolean duplikaatti;
    private Collection<String> organisaatioOids;
    private Map<String, Collection<String>> kayttooikeudet;

    public Boolean getPassivoitu() {
        return passivoitu;
    }

    public void setPassivoitu(Boolean passivoitu) {
        this.passivoitu = passivoitu;
    }

    public Boolean getDuplikaatti() {
        return duplikaatti;
    }

    public void setDuplikaatti(Boolean duplikaatti) {
        this.duplikaatti = duplikaatti;
    }

    public Collection<String> getOrganisaatioOids() {
        return organisaatioOids;
    }

    public void setOrganisaatioOids(Collection<String> organisaatioOids) {
        this.organisaatioOids = organisaatioOids;
    }

    public Map<String, Collection<String>> getKayttooikeudet() {
        return kayttooikeudet;
    }

    public void setKayttooikeudet(Map<String, Collection<String>> kayttooikeudet) {
        this.kayttooikeudet = kayttooikeudet;
    }

}
