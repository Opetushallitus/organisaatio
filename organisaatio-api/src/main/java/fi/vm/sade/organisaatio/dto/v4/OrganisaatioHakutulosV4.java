package fi.vm.sade.organisaatio.dto.v4;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

@ApiModel(value = "Organisaation hakutulos v4")
public class OrganisaatioHakutulosV4 {

    @ApiModelProperty(value = "Tulosjoukon koko", required = true)
    private int numHits;

    @ApiModelProperty(value = "Organisaatiot", required = true)
    private List<OrganisaatioPerustietoV4> organisaatiot = new ArrayList<>();

    public int getNumHits() {
        return numHits;
    }

    public void setNumHits(int numHits) {
        this.numHits = numHits;
    }

    public List<OrganisaatioPerustietoV4> getOrganisaatiot() {
        return organisaatiot;
    }

    public void setOrganisaatiot(List<OrganisaatioPerustietoV4> organisaatiot) {
        this.organisaatiot = organisaatiot;
    }

}
