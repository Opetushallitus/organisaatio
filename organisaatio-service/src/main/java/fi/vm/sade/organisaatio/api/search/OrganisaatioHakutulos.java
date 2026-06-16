package fi.vm.sade.organisaatio.api.search;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Collection;
import java.util.HashSet;

@Schema(description = "Organisaation hakutulos")
public class OrganisaatioHakutulos {

    @Schema(description = "Tulosjoukon koko", requiredMode = Schema.RequiredMode.REQUIRED)
    private int numHits;

    @Schema(description = "Organisaatiot", requiredMode = Schema.RequiredMode.REQUIRED)
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
