package fi.vm.sade.organisaatio.dto.v2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by jefin on 3.9.2014.
 */
public class OrganisaatioMuokkausTulosListaDTO {
    private List<OrganisaatioMuokkausTulosDTO> tulokset;

    public OrganisaatioMuokkausTulosListaDTO(int alkuKoko) {
        this.tulokset = new ArrayList<OrganisaatioMuokkausTulosDTO>(alkuKoko);
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
        this.tulokset = new ArrayList<OrganisaatioMuokkausTulosDTO>(10);
    }

    public List<OrganisaatioMuokkausTulosDTO> getTulokset() {
        return Collections.unmodifiableList(tulokset);
    }

}
