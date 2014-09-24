package fi.vm.sade.organisaatio.dto.v2;

import java.util.Date;

/**
 * Created by jefin on 29.8.2014.
 */
public class OrganisaatioMuokkausTiedotDTO {
    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public Date getAlkuPvm() {
        return alkuPvm;
    }

    public void setAlkuPvm(Date alkuPvm) {
        this.alkuPvm = alkuPvm;
    }

    public Date getLoppuPvm() {
        return loppuPvm;
    }

    public void setLoppuPvm(Date loppuPvm) {
        this.loppuPvm = loppuPvm;
    }

    private String oid;
    private Date alkuPvm;
    private Date loppuPvm;

    @Override
    public String toString() {
        return "OrganisaatioMuokkausTiedotDTO{" +
                "oid='" + oid + '\'' +
                ", alkuPvm=" + alkuPvm +
                ", loppuPvm=" + loppuPvm +
                '}';
    }
}
