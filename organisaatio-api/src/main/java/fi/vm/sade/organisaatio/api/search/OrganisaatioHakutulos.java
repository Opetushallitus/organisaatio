package fi.vm.sade.organisaatio.api.search;

import java.util.ArrayList;
import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "Organisaation hakutulos")
public class OrganisaatioHakutulos {

    @ApiModelProperty(value = "Tulosjoukon koko", required = true)
    private int numHits;

    @ApiModelProperty(value = "Organisaatiot", required = true)
    private List<OrganisaatioPerustieto> organisaatiot = new ArrayList<OrganisaatioPerustieto>();

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

}
