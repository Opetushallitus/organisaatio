package fi.vm.sade.organisaatio.dto.v2;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Collection;
import java.util.HashSet;

/**
 *
 * @author hpy
 */
@Schema(description = "Organisaation hakutulos suppea")
public class OrganisaatioHakutulosSuppeaDTOV2 {

    @Schema(description = "Tulosjoukon koko", required = true)
    private int numHits;

    @Schema(description = "Organisaatiot", required = true)
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
