package fi.vm.sade.organisaatio.dto.v2;

import java.util.HashSet;
import java.util.Set;


public class OrganisaatioHistoriaRDTOV2 {
    private Set<OrganisaatioSuhdeDTOV2> childSuhteet = new HashSet<>();
    private Set<OrganisaatioSuhdeDTOV2> parentSuhteet = new HashSet<>();
    private Set<OrganisaatioLiitosDTOV2> liitokset = new HashSet<>();
    private Set<OrganisaatioLiitosDTOV2> liittymiset = new HashSet<>();

    /**
     * @return the childSuhteet
     */
    public Set<OrganisaatioSuhdeDTOV2> getChildSuhteet() {
        return childSuhteet;
    }

    /**
     * @param childSuhteet the childSuhteet to set
     */
    public void setChildSuhteet(Set<OrganisaatioSuhdeDTOV2> childSuhteet) {
        this.childSuhteet = childSuhteet;
    }

    /**
     * @return the parentSuhteet
     */
    public Set<OrganisaatioSuhdeDTOV2> getParentSuhteet() {
        return parentSuhteet;
    }

    /**
     * @param parentSuhteet the parentSuhteet to set
     */
    public void setParentSuhteet(Set<OrganisaatioSuhdeDTOV2> parentSuhteet) {
        this.parentSuhteet = parentSuhteet;
    }

    /**
     * @return the liitokset
     */
    public Set<OrganisaatioLiitosDTOV2> getLiitokset() {
        return liitokset;
    }

    /**
     * @param liitokset the liitokset to set
     */
    public void setLiitokset(Set<OrganisaatioLiitosDTOV2> liitokset) {
        this.liitokset = liitokset;
    }

    /**
     * @return the liittymiset
     */
    public Set<OrganisaatioLiitosDTOV2> getLiittymiset() {
        return liittymiset;
    }

    /**
     * @param liittymiset the liittymiset to set
     */
    public void setLiittymiset(Set<OrganisaatioLiitosDTOV2> liittymiset) {
        this.liittymiset = liittymiset;
    }

}
