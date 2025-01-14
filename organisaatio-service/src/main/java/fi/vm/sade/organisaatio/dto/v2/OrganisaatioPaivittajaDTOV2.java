package fi.vm.sade.organisaatio.dto.v2;

import java.util.Date;

public class OrganisaatioPaivittajaDTOV2 {
    private Date paivitysPvm;
    private String paivittaja;

    /**
     * @return the paivittaja
     */
    public String getPaivittaja() {
        return paivittaja;
    }

    /**
     * @param paivittaja the paivittaja to set
     */
    public void setPaivittaja(String paivittaja) {
        this.paivittaja = paivittaja;
    }

    /**
     * @return the paivitysPvm
     */
    public Date getPaivitysPvm() {
        return paivitysPvm;
    }

    /**
     * @param paivitysPvm the paivitysPvm to set
     */
    public void setPaivitysPvm(Date paivitysPvm) {
        this.paivitysPvm = paivitysPvm;
    }
}
