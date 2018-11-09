package fi.vm.sade.organisaatio.dto.v2;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author hpy
 */
@ApiModel(value = "Organisaation hakutulos suppea")
public class OrganisaatioHakutulosSuppeaDTOV2 {

    @ApiModelProperty(value = "Tulosjoukon koko", required = true)
    private int numHits;

    @ApiModelProperty(value = "Organisaatiot", required = true)
    private Set<OrganisaatioPerustietoSuppea> organisaatiot = new HashSet<>();

    public int getNumHits() {
        return numHits;
    }

    public void setNumHits(int numHits) {
        this.numHits = numHits;
    }

    public Set<OrganisaatioPerustietoSuppea> getOrganisaatiot() {
        return organisaatiot;
    }

    public void setOrganisaatiot(Set<OrganisaatioPerustietoSuppea> organisaatiot) {
        this.organisaatiot = organisaatiot;
    }

}
