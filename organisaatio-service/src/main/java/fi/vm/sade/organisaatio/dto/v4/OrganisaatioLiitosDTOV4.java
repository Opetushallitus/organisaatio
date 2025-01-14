package fi.vm.sade.organisaatio.dto.v4;

import java.util.Date;


public class OrganisaatioLiitosDTOV4 {
    private OrganisaatioCoreInfoDTOV4 organisaatio;
    private OrganisaatioCoreInfoDTOV4 kohde;

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
    public OrganisaatioCoreInfoDTOV4 getOrganisaatio() {
        return organisaatio;
    }

    /**
     * @param organisaatio the organisaatio to set
     */
    public void setOrganisaatio(OrganisaatioCoreInfoDTOV4 organisaatio) {
        this.organisaatio = organisaatio;
    }

    /**
     * @return the kohde
     */
    public OrganisaatioCoreInfoDTOV4 getKohde() {
        return kohde;
    }

    /**
     * @param kohde the kohde to set
     */
    public void setKohde(OrganisaatioCoreInfoDTOV4 kohde) {
        this.kohde = kohde;
    }

}
