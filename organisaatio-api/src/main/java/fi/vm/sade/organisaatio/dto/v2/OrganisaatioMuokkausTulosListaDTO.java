package fi.vm.sade.organisaatio.dto.v2;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class OrganisaatioMuokkausTulosListaDTO {
    private Set<OrganisaatioMuokkausTulosDTO> tulokset;

    public OrganisaatioMuokkausTulosListaDTO(int alkuKoko) {
        this.tulokset = new HashSet<>(alkuKoko);
    }

    public void lisaaTulos(OrganisaatioMuokkausTulosDTO tulos) {
        tulokset.add(tulos);
    }

    @Override
    public String toString() {
        return "OrganisaatioMuokkausTulosListaDTO{" +
                ", tulokset=" + tulokset +
                '}';
    }

    public OrganisaatioMuokkausTulosListaDTO() {
        this.tulokset = new HashSet<>(10);
    }

    public Set<OrganisaatioMuokkausTulosDTO> getTulokset() {
        return Collections.unmodifiableSet(tulokset);
    }

}
