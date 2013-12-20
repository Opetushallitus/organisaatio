package fi.vm.sade.organisaatio.api.search;

import java.util.ArrayList;
import java.util.List;

public class OrganisaatioHakutulos {
    
    public int getNumHits() {
        return numHits;
    }
    public void setNumHits(int numHits) {
        this.numHits = numHits;
    }
    public List<OrganisaatioPerustieto> getOrganisaatiot() {
        return organisaatiot;
    }
    public void setOrganisaatiot(List<OrganisaatioPerustieto> organisaatiot) {
        this.organisaatiot = organisaatiot;
    }
    private int numHits;
    private List<OrganisaatioPerustieto> organisaatiot = new ArrayList<OrganisaatioPerustieto>();

}
