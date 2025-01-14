package fi.vm.sade.organisaatio.dto.v4;

import java.util.Date;

public class OrganisaatioPaivittajaDTO {
    private Date paivitysPvm;
    private String paivittaja;
    private String etuNimet;
    private String sukuNimi;

    public String getPaivittaja() {
        return paivittaja;
    }

    public void setPaivittaja(String paivittaja) {
        this.paivittaja = paivittaja;
    }

    public Date getPaivitysPvm() {
        return paivitysPvm;
    }

    public void setPaivitysPvm(Date paivitysPvm) {
        this.paivitysPvm = paivitysPvm;
    }

    public String getSukuNimi() {
        return sukuNimi;
    }

    public void setSukuNimi(String sukuNimi) {
        this.sukuNimi = sukuNimi;
    }

    public String getEtuNimet() {
        return etuNimet;
    }

    public void setEtuNimet(String etuNimet) {
        this.etuNimet = etuNimet;
    }
}
