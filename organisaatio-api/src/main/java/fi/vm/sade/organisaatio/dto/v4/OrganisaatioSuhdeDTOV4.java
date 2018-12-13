package fi.vm.sade.organisaatio.dto.v4;

import java.util.Date;


public class OrganisaatioSuhdeDTOV4 {
    private OrganisaatioCoreInfoDTOV4 child;
    private OrganisaatioCoreInfoDTOV4 parent;

    private Date alkuPvm;
    private Date loppuPvm;
    private String suhdeTyyppi;

    public Date getAlkuPvm() {
        return alkuPvm;
    }

    public void setAlkuPvm(Date alkuPvm) {
        this.alkuPvm = alkuPvm;
    }

    /**
     * @return the loppuPvm
     */
    public Date getLoppuPvm() {
        return loppuPvm;
    }

    /**
     * @param loppuPvm the loppuPvm to set
     */
    public void setLoppuPvm(Date loppuPvm) {
        this.loppuPvm = loppuPvm;
    }

    /**
     * @return the suhdeTyyppi
     */
    public String getSuhdeTyyppi() {
        return suhdeTyyppi;
    }

    /**
     * @param suhdeTyyppi the suhdeTyyppi to set
     */
    public void setSuhdeTyyppi(String suhdeTyyppi) {
        this.suhdeTyyppi = suhdeTyyppi;
    }

    /**
     * @return the child
     */
    public OrganisaatioCoreInfoDTOV4 getChild() {
        return child;
    }

    /**
     * @param child the child to set
     */
    public void setChild(OrganisaatioCoreInfoDTOV4 child) {
        this.child = child;
    }

    /**
     * @return the parent
     */
    public OrganisaatioCoreInfoDTOV4 getParent() {
        return parent;
    }

    /**
     * @param parent the parent to set
     */
    public void setParent(OrganisaatioCoreInfoDTOV4 parent) {
        this.parent = parent;
    }
}
