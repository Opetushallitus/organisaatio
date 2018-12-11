package fi.vm.sade.organisaatio.api.search;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.HashSet;
import java.util.Set;

@ApiModel(value = "Organisaation hakutulos")
public class OrganisaatioHakutulos {

    @ApiModelProperty(value = "Tulosjoukon koko", required = true)
    private int numHits;

    @ApiModelProperty(value = "Organisaatiot", required = true)
    private Set<OrganisaatioPerustieto> organisaatiot = new HashSet<>();

    public int getNumHits() {
        return numHits;
    }

    public void setNumHits(int numHits) {
        this.numHits = numHits;
    }

    public Set<OrganisaatioPerustieto> getOrganisaatiot() {
        return organisaatiot;
    }

    public void setOrganisaatiot(Set<OrganisaatioPerustieto> organisaatiot) {
        this.organisaatiot = organisaatiot;
    }

}
