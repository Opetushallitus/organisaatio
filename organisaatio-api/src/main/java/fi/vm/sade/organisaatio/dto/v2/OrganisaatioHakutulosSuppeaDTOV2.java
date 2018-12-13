package fi.vm.sade.organisaatio.dto.v2;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Collection;
import java.util.HashSet;

/**
 *
 * @author hpy
 */
@ApiModel(value = "Organisaation hakutulos suppea")
public class OrganisaatioHakutulosSuppeaDTOV2 {

    @ApiModelProperty(value = "Tulosjoukon koko", required = true)
    private int numHits;

    @ApiModelProperty(value = "Organisaatiot", required = true)
    private Collection<OrganisaatioPerustietoSuppea> organisaatiot = new HashSet<>();

    public int getNumHits() {
        return numHits;
    }

    public void setNumHits(int numHits) {
        this.numHits = numHits;
    }

    public Collection<OrganisaatioPerustietoSuppea> getOrganisaatiot() {
        return organisaatiot;
    }

    public void setOrganisaatiot(Collection<OrganisaatioPerustietoSuppea> organisaatiot) {
        this.organisaatiot = organisaatiot;
    }

}
