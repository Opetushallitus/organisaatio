package fi.vm.sade.organisaatio.dto;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class HenkiloOrganisaatioCriteria {

    private String kayttajaTyyppi;
    private Boolean passivoitu;
    private Collection<String> kayttooikeudet;

    public String getKayttajaTyyppi() {
        return kayttajaTyyppi;
    }

    public void setKayttajaTyyppi(String kayttajaTyyppi) {
        this.kayttajaTyyppi = kayttajaTyyppi;
    }

    public Boolean getPassivoitu() {
        return passivoitu;
    }

    public void setPassivoitu(Boolean passivoitu) {
        this.passivoitu = passivoitu;
    }

    public Collection<String> getKayttooikeudet() {
        return kayttooikeudet;
    }

    public void setKayttooikeudet(Collection<String> kayttooikeudet) {
        this.kayttooikeudet = kayttooikeudet;
    }

    public Map<String, Object> asMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("kayttajaTyyppi", kayttajaTyyppi);
        map.put("passivoitu", passivoitu);
        map.put("kayttooikeudet", kayttooikeudet);
        return map;
    }

}
