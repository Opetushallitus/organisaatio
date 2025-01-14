package fi.vm.sade.organisaatio.dto.v2;

import java.util.Date;

public class OrganisaatioMuokkausTulosDTO {
    private String oid;
    private Date alkuPvm;
    private Date loppuPvm;
    private long version;

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

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "OrganisaatioMuokkausTulosDTO{" +
                "oid='" + oid + '\'' +
                ", alkuPvm=" + alkuPvm +
                ", loppuPvm=" + loppuPvm +
                ", version=" + version +
                '}';
    }
}
