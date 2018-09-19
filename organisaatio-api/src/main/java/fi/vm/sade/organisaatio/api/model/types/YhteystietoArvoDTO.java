
package fi.vm.sade.organisaatio.api.model.types;

import java.io.Serializable;

public class YhteystietoArvoDTO implements Serializable {
    private final static long serialVersionUID = 100L;
    protected String yhteystietoArvoOid;
    protected Object arvo;
    protected String kieli;
    protected String kenttaOid;
    protected String organisaatioOid;

    /**
     * Gets the value of the yhteystietoArvoOid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getYhteystietoArvoOid() {
        return yhteystietoArvoOid;
    }

    /**
     * Sets the value of the yhteystietoArvoOid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setYhteystietoArvoOid(String value) {
        this.yhteystietoArvoOid = value;
    }

    /**
     * Gets the value of the arvo property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getArvo() {
        return arvo;
    }

    /**
     * Sets the value of the arvo property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setArvo(Object value) {
        this.arvo = value;
    }

    /**
     * Gets the value of the kieli property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getKieli() {
        return kieli;
    }

    /**
     * Sets the value of the kieli property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setKieli(String value) {
        this.kieli = value;
    }

    /**
     * Gets the value of the kenttaOid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getKenttaOid() {
        return kenttaOid;
    }

    /**
     * Sets the value of the kenttaOid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setKenttaOid(String value) {
        this.kenttaOid = value;
    }

    /**
     * Gets the value of the organisaatioOid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOrganisaatioOid() {
        return organisaatioOid;
    }

    /**
     * Sets the value of the organisaatioOid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOrganisaatioOid(String value) {
        this.organisaatioOid = value;
    }

}
