package fi.vm.sade.organisaatio.dto.v2;

public class OsoiteDTOV2 {
    private String kieli;
    private String osoiteTyyppi;
    private String osoite;
    private String postinumero;

    /**
     * @return the kieli
     */
    public String getKieli() {
        return kieli;
    }

    /**
     * @param kieli the kieli to set
     */
    public void setKieli(String kieli) {
        this.kieli = kieli;
    }

    /**
     * @return the osoiteTyyppi
     */
    public String getOsoiteTyyppi() {
        return osoiteTyyppi;
    }

    /**
     * @param osoiteTyyppi the osoiteTyyppi to set
     */
    public void setOsoiteTyyppi(String osoiteTyyppi) {
        this.osoiteTyyppi = osoiteTyyppi;
    }

    /**
     * @return the osoite
     */
    public String getOsoite() {
        return osoite;
    }

    /**
     * @param osoite the osoite to set
     */
    public void setOsoite(String osoite) {
        this.osoite = osoite;
    }

    /**
     * @return the postinumero
     */
    public String getPostinumero() {
        return postinumero;
    }

    /**
     * @param postinumero the postinumero to set
     */
    public void setPostinumero(String postinumero) {
        this.postinumero = postinumero;
    }
}
