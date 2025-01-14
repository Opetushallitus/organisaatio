package fi.vm.sade.organisaatio.dto.v2;

import java.util.Date;


public class OrganisaatioLiitosDTOV2 {
    private OrganisaatioCoreInfoDTOV2 organisaatio;
    private OrganisaatioCoreInfoDTOV2 kohde;

    private Date alkuPvm;

    public Date getAlkuPvm() {
        return alkuPvm;
    }

    public void setAlkuPvm(Date alkuPvm) {
        this.alkuPvm = alkuPvm;
    }

    /**
     * @return the organisaatio
     */
    public OrganisaatioCoreInfoDTOV2 getOrganisaatio() {
        return organisaatio;
    }

    /**
     * @param organisaatio the organisaatio to set
     */
    public void setOrganisaatio(OrganisaatioCoreInfoDTOV2 organisaatio) {
        this.organisaatio = organisaatio;
    }

    /**
     * @return the kohde
     */
    public OrganisaatioCoreInfoDTOV2 getKohde() {
        return kohde;
    }

    /**
     * @param kohde the kohde to set
     */
    public void setKohde(OrganisaatioCoreInfoDTOV2 kohde) {
        this.kohde = kohde;
    }

}
