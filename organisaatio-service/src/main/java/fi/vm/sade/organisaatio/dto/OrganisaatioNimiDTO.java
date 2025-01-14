package fi.vm.sade.organisaatio.dto;

import java.util.Date;
import java.util.Map;

public class OrganisaatioNimiDTO {
    private String oid;
    private Map<String, String> nimi;
    private Date alkuPvm;
    private String paivittaja;
    private int version;


    /**
     * @return the oid
     */
    public String getOid() {
        return oid;
    }

    /**
     * @param oid the oid to set
     */
    public void setOid(String oid) {
        this.oid = oid;
    }

    /**
     * @return the nimi
     */
    public Map<String, String> getNimi() {
        return nimi;
    }

    /**
     * @param nimi the nimi to set
     */
    public void setNimi(Map<String, String> nimi) {
        this.nimi = nimi;
    }

    /**
     * @return the alkuPvm
     */
    public Date getAlkuPvm() {
        return alkuPvm;
    }

    /**
     * @param alkuPvm the alkuPvm to set
     */
    public void setAlkuPvm(Date alkuPvm) {
        this.alkuPvm = alkuPvm;
    }

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
     * @return the version
     */
    public int getVersion() {
        return version;
    }

    /**
     * @param version the version to set
     */
    public void setVersion(int version) {
        this.version = version;
    }

}
