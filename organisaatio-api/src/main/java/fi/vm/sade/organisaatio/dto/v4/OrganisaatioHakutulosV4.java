package fi.vm.sade.organisaatio.dto.v4;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.HashSet;
import java.util.Set;

@Schema(description = "Organisaation hakutulos v4")
public class OrganisaatioHakutulosV4 {

    @Schema(description = "Tulosjoukon koko", required = true)
    private int numHits;

    @Schema(description = "Organisaatiot", required = true)
    private Set<OrganisaatioPerustietoV4> organisaatiot = new HashSet<>();

    public int getNumHits() {
        return numHits;
    }

    public void setNumHits(int numHits) {
        this.numHits = numHits;
    }

    public Set<OrganisaatioPerustietoV4> getOrganisaatiot() {
        return organisaatiot;
    }

    public void setOrganisaatiot(Set<OrganisaatioPerustietoV4> organisaatiot) {
        this.organisaatiot = organisaatiot;
    }

}
