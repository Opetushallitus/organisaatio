package fi.vm.sade.organisaatio.dto.v4;

import java.util.ArrayList;
import java.util.List;


public class OrganisaatioHistoriaRDTOV4 {
    private List<OrganisaatioSuhdeDTOV4> childSuhteet = new ArrayList<OrganisaatioSuhdeDTOV4>();
    private List<OrganisaatioSuhdeDTOV4> parentSuhteet = new ArrayList<OrganisaatioSuhdeDTOV4>();
    private List<OrganisaatioLiitosDTOV4> liitokset = new ArrayList<OrganisaatioLiitosDTOV4>();
    private List<OrganisaatioLiitosDTOV4> liittymiset = new ArrayList<OrganisaatioLiitosDTOV4>();

    /**
     * @return the childSuhteet
     */
    public List<OrganisaatioSuhdeDTOV4> getChildSuhteet() {
        return childSuhteet;
    }

    /**
     * @param childSuhteet the childSuhteet to set
     */
    public void setChildSuhteet(List<OrganisaatioSuhdeDTOV4> childSuhteet) {
        this.childSuhteet = childSuhteet;
    }

    /**
     * @return the parentSuhteet
     */
    public List<OrganisaatioSuhdeDTOV4> getParentSuhteet() {
        return parentSuhteet;
    }

    /**
     * @param parentSuhteet the parentSuhteet to set
     */
    public void setParentSuhteet(List<OrganisaatioSuhdeDTOV4> parentSuhteet) {
        this.parentSuhteet = parentSuhteet;
    }

    /**
     * @return the liitokset
     */
    public List<OrganisaatioLiitosDTOV4> getLiitokset() {
        return liitokset;
    }

    /**
     * @param liitokset the liitokset to set
     */
    public void setLiitokset(List<OrganisaatioLiitosDTOV4> liitokset) {
        this.liitokset = liitokset;
    }

    /**
     * @return the liittymiset
     */
    public List<OrganisaatioLiitosDTOV4> getLiittymiset() {
        return liittymiset;
    }

    /**
     * @param liittymiset the liittymiset to set
     */
    public void setLiittymiset(List<OrganisaatioLiitosDTOV4> liittymiset) {
        this.liittymiset = liittymiset;
    }

}
