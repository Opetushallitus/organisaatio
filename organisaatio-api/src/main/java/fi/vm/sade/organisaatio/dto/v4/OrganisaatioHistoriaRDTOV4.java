package fi.vm.sade.organisaatio.dto.v4;

import java.util.HashSet;
import java.util.Set;


public class OrganisaatioHistoriaRDTOV4 {
    private Set<OrganisaatioSuhdeDTOV4> childSuhteet = new HashSet<>();
    private Set<OrganisaatioSuhdeDTOV4> parentSuhteet = new HashSet<>();
    private Set<OrganisaatioLiitosDTOV4> liitokset = new HashSet<>();
    private Set<OrganisaatioLiitosDTOV4> liittymiset = new HashSet<>();

    /**
     * @return the childSuhteet
     */
    public Set<OrganisaatioSuhdeDTOV4> getChildSuhteet() {
        return childSuhteet;
    }

    /**
     * @param childSuhteet the childSuhteet to set
     */
    public void setChildSuhteet(Set<OrganisaatioSuhdeDTOV4> childSuhteet) {
        this.childSuhteet = childSuhteet;
    }

    /**
     * @return the parentSuhteet
     */
    public Set<OrganisaatioSuhdeDTOV4> getParentSuhteet() {
        return parentSuhteet;
    }

    /**
     * @param parentSuhteet the parentSuhteet to set
     */
    public void setParentSuhteet(Set<OrganisaatioSuhdeDTOV4> parentSuhteet) {
        this.parentSuhteet = parentSuhteet;
    }

    /**
     * @return the liitokset
     */
    public Set<OrganisaatioLiitosDTOV4> getLiitokset() {
        return liitokset;
    }

    /**
     * @param liitokset the liitokset to set
     */
    public void setLiitokset(Set<OrganisaatioLiitosDTOV4> liitokset) {
        this.liitokset = liitokset;
    }

    /**
     * @return the liittymiset
     */
    public Set<OrganisaatioLiitosDTOV4> getLiittymiset() {
        return liittymiset;
    }

    /**
     * @param liittymiset the liittymiset to set
     */
    public void setLiittymiset(Set<OrganisaatioLiitosDTOV4> liittymiset) {
        this.liittymiset = liittymiset;
    }

}
