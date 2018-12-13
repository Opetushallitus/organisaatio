package fi.vm.sade.organisaatio.api.search;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Collection;
import java.util.HashSet;

@ApiModel(value = "Organisaation hakutulos")
public class OrganisaatioHakutulos {

    @ApiModelProperty(value = "Tulosjoukon koko", required = true)
    private int numHits;

    @ApiModelProperty(value = "Organisaatiot", required = true)
    private Collection<OrganisaatioPerustieto> organisaatiot = new HashSet<>();

    public int getNumHits() {
        return numHits;
    }

    public void setNumHits(int numHits) {
        this.numHits = numHits;
    }

    public Collection<OrganisaatioPerustieto> getOrganisaatiot() {
        return organisaatiot;
    }

    public void setOrganisaatiot(Collection<OrganisaatioPerustieto> organisaatiot) {
        this.organisaatiot = organisaatiot;
    }

}
