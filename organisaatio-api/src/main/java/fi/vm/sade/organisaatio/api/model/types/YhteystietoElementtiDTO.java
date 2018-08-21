
package fi.vm.sade.organisaatio.api.model.types;

import java.io.Serializable;


public class YhteystietoElementtiDTO implements Serializable {
    private final static long serialVersionUID = 100L;
    protected long version;
    protected String oid;
    protected String nimi;
    protected String nimiSv;
    protected String nimiEn;
    protected YhteystietoElementtiTyyppi tyyppi;
    protected boolean kaytossa;
    protected boolean pakollinen;

    /**
     * Gets the koodiValue of the version property.
     * 
     */
    public long getVersion() {
        return version;
    }

    /**
     * Sets the koodiValue of the version property.
     * 
     */
    public void setVersion(long value) {
        this.version = value;
    }

    /**
     * Gets the koodiValue of the oid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOid() {
        return oid;
    }

    /**
     * Sets the koodiValue of the oid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOid(String value) {
        this.oid = value;
    }

    /**
     * Gets the koodiValue of the nimi property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNimi() {
        return nimi;
    }

    /**
     * Sets the koodiValue of the nimi property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNimi(String value) {
        this.nimi = value;
    }

    /**
     * Gets the koodiValue of the nimiSv property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNimiSv() {
        return nimiSv;
    }

    /**
     * Sets the koodiValue of the nimiSv property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNimiSv(String value) {
        this.nimiSv = value;
    }

    /**
     * Gets the koodiValue of the nimiEn property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNimiEn() {
        return nimiEn;
    }

    /**
     * Sets the koodiValue of the nimiEn property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNimiEn(String value) {
        this.nimiEn = value;
    }

    /**
     * Gets the koodiValue of the tyyppi property.
     * 
     * @return
     *     possible object is
     *     {@link YhteystietoElementtiTyyppi }
     *     
     */
    public YhteystietoElementtiTyyppi getTyyppi() {
        return tyyppi;
    }

    /**
     * Sets the koodiValue of the tyyppi property.
     * 
     * @param value
     *     allowed object is
     *     {@link YhteystietoElementtiTyyppi }
     *     
     */
    public void setTyyppi(YhteystietoElementtiTyyppi value) {
        this.tyyppi = value;
    }

    /**
     * Gets the koodiValue of the kaytossa property.
     * 
     */
    public boolean isKaytossa() {
        return kaytossa;
    }

    /**
     * Sets the koodiValue of the kaytossa property.
     * 
     */
    public void setKaytossa(boolean value) {
        this.kaytossa = value;
    }

    /**
     * Gets the koodiValue of the pakollinen property.
     * 
     */
    public boolean isPakollinen() {
        return pakollinen;
    }

    /**
     * Sets the koodiValue of the pakollinen property.
     * 
     */
    public void setPakollinen(boolean value) {
        this.pakollinen = value;
    }

}
